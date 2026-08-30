package com.kaizzinho.wildbosses.spawn

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossEvolutionResolver
import com.kaizzinho.wildbosses.boss.BossMessageFormat
import com.kaizzinho.wildbosses.boss.BossPersistence
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossSpawnCooldownData
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import kotlin.random.Random

object BossSpawnListener {

    private const val PENDING_PROMOTION_TIMEOUT_TICKS = 100L

    private data class PendingPromotion(
        val entityUuid: UUID,
        val targetPlayerUuid: UUID,
        val tier: BossTier,
        val queuedAtTick: Long
    )

    private val pendingPromotions = linkedMapOf<UUID, PendingPromotion>()
    private val reservedPlayers = linkedSetOf<UUID>()

    fun register() {
        ServerLifecycleEvents.SERVER_STOPPED.register {
            pendingPromotions.clear()
            reservedPlayers.clear()
        }

        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            queueNaturalPromotion(event.entity)
        }

        ServerEntityEvents.ENTITY_LOAD.register entityLoad@{ loadedEntity, world ->
            val entity = loadedEntity as? PokemonEntity ?: return@entityLoad
            val pending = pendingPromotions.remove(entity.uuid) ?: return@entityLoad
            reservedPlayers.remove(pending.targetPlayerUuid)

            if (entity.isRemoved || entity.tags.contains("wildbosses:is_boss")) return@entityLoad

            val targetPlayer = world.server.playerList.getPlayer(pending.targetPlayerUuid)
                ?: return@entityLoad

            val speciesLabels = entity.pokemon.species.labels
            if (speciesLabels.any { it in WildBossesConfig.data.excludedSpeciesLabels }) return@entityLoad

            val overworld = world.server.overworld()
            val currentTick = overworld.gameTime
            val cooldownData = BossSpawnCooldownData.get(overworld)
            if (cooldownData.isOnCooldown(targetPlayer.uuid, currentTick)) return@entityLoad

            WildBosses.logger.info(
                "[BossPersistence-DEBUG] NATURAL_PROMOTION_FINALIZED species=${entity.pokemon.species.name} " +
                    "uuid=${entity.uuid} tier=${pending.tier.name}"
            )

            applyBossPromotion(
                entity,
                pending.tier,
                targetPlayer,
                cooldownData,
                currentTick
            )
        }

        ServerTickEvents.END_SERVER_TICK.register cleanupTick@{ server ->
            if (pendingPromotions.isEmpty()) return@cleanupTick

            val currentTick = server.overworld().gameTime
            val stale = pendingPromotions.values.filter {
                currentTick - it.queuedAtTick >= PENDING_PROMOTION_TIMEOUT_TICKS
            }
            if (stale.isEmpty()) return@cleanupTick

            for (pending in stale) {
                pendingPromotions.remove(pending.entityUuid)
                reservedPlayers.remove(pending.targetPlayerUuid)
                WildBosses.logger.info(
                    "[BossPersistence-DEBUG] NATURAL_PROMOTION_EXPIRED uuid=${pending.entityUuid} " +
                        "tier=${pending.tier.name}"
                )
            }
        }
    }

    private fun queueNaturalPromotion(entity: PokemonEntity) {
        if (entity.tags.contains("wildbosses:is_boss")) return
        if (pendingPromotions.containsKey(entity.uuid)) return
        if (Random.nextDouble() >= WildBossesConfig.data.bossSpawnChance) return

        val speciesLabels = entity.pokemon.species.labels
        if (speciesLabels.any { it in WildBossesConfig.data.excludedSpeciesLabels }) return

        val level = entity.level() as? ServerLevel ?: return
        val nearestPlayer = level.players().minByOrNull { it.distanceToSqr(entity) } ?: return
        if (nearestPlayer.uuid in reservedPlayers) return

        val overworld = level.server.overworld()
        val currentTick = overworld.gameTime
        val cooldownData = BossSpawnCooldownData.get(overworld)
        if (cooldownData.isOnCooldown(nearestPlayer.uuid, currentTick)) return

        val tier = BossTier.rollRandomTier()
        pendingPromotions[entity.uuid] = PendingPromotion(
            entityUuid = entity.uuid,
            targetPlayerUuid = nearestPlayer.uuid,
            tier = tier,
            queuedAtTick = currentTick
        )
        reservedPlayers += nearestPlayer.uuid

        WildBosses.logger.info(
            "[BossPersistence-DEBUG] NATURAL_PROMOTION_QUEUED species=${entity.pokemon.species.name} " +
                "uuid=${entity.uuid} tier=${tier.name} player=${nearestPlayer.name.string}"
        )
    }

    private fun announceSpawn(entity: PokemonEntity, tier: BossTier, targetPlayer: ServerPlayer) {
        val server = entity.server ?: return
        val message = BossMessageFormat.build(
            BossMessageFormat.bossName(tier, entity.pokemon.species.name)
                .append(BossMessageFormat.plainKey("wildbosses.message.spawned_near", targetPlayer.name.string))
        )
        server.playerList.broadcastSystemMessage(message, false)
    }

    private fun applyBossStats(entity: PokemonEntity, tier: BossTier) {
        val properties = PokemonProperties()
        val tierConfig = WildBossesConfig.tier(tier.name)

        if (tierConfig.guaranteedPerfectIVs) {
            properties.minPerfectIVs = 6
        }

        if (Random.nextDouble() < tierConfig.shinyChance) {
            properties.shiny = true
        }

        properties.apply(entity.pokemon)
    }

    fun applyTierGlow(entity: PokemonEntity, tier: BossTier) {
        val scoreboard = entity.server?.scoreboard ?: return
        val teamName = "wildbosses_${tier.name.lowercase()}"

        val team = scoreboard.getPlayerTeam(teamName) ?: scoreboard.addPlayerTeam(teamName).apply {
            setColor(tier.color)
        }

        scoreboard.addPlayerToTeam(entity.scoreboardName, team)
        entity.setGlowingTag(true)
    }

    fun forcePromote(
        entity: PokemonEntity,
        tier: BossTier,
        targetPlayer: ServerPlayer,
        respectCooldown: Boolean = false
    ) {
        val server = entity.server ?: return
        val overworld = server.overworld()
        val currentTick = overworld.gameTime
        val cooldownData = BossSpawnCooldownData.get(overworld)

        if (respectCooldown && cooldownData.isOnCooldown(targetPlayer.uuid, currentTick)) {
            return
        }

        applyBossPromotion(entity, tier, targetPlayer, cooldownData, currentTick)
    }

    private fun applyBossPromotion(
        entity: PokemonEntity,
        tier: BossTier,
        targetPlayer: ServerPlayer,
        cooldownData: BossSpawnCooldownData,
        currentTick: Long
    ) {
        if (tier == BossTier.EPIC || tier == BossTier.LEGENDARY || tier == BossTier.MYTHIC) {
            val resolved = BossEvolutionResolver.resolveFinalForm(entity.pokemon.species, entity.pokemon.form)
            entity.pokemon.species = resolved.species
            entity.pokemon.form = resolved.form
            entity.pokemon.updateAspects()
        }

        applyBossStats(entity, tier)

        UncatchableProperty.uncatchable().apply(entity.pokemon)
        BossPersistence.restoreRuntimeState(entity, tier, currentTick)
        BossRegistry.register(entity, tier, currentTick)
        BossPersistence.requestCheckpoint(entity)
        announceSpawn(entity, tier, targetPlayer)

        cooldownData.startCooldown(targetPlayer.uuid, currentTick, WildBossesConfig.data.spawnCooldownTicks)
    }
}
