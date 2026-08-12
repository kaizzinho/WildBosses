package com.kaizzinho.wildbosses.spawn


import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossSpawnCooldownData
import com.kaizzinho.wildbosses.boss.BossTier
import kotlin.random.Random
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import com.kaizzinho.wildbosses.boss.BossEvolutionResolver
import com.kaizzinho.wildbosses.boss.BossMessageFormat
import com.kaizzinho.wildbosses.boss.BossPersistence
import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.minecraft.server.level.ServerLevel



object BossSpawnListener {

    fun register() {
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            val entity: PokemonEntity = event.entity
            if (Random.nextDouble() < WildBossesConfig.data.bossSpawnChance) {
                promoteToBoss(entity)
            }
        }
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

    private fun promoteToBoss(entity: PokemonEntity) {
        if (entity.tags.contains("wildbosses:is_boss")) return

        val speciesLabels = entity.pokemon.species.labels
        if (speciesLabels.any { it in WildBossesConfig.data.excludedSpeciesLabels }) return

        val level = entity.level() as? ServerLevel ?: return

        val nearestPlayer = level.players().minByOrNull { it.distanceToSqr(entity) }
            ?: return

        val server = entity.server ?: return
        val overworld = server.overworld()
        val currentTick = overworld.gameTime

        val cooldownData = BossSpawnCooldownData.get(overworld)
        if (cooldownData.isOnCooldown(nearestPlayer.uuid, currentTick)) {
            return
        }

        val tier = BossTier.rollRandomTier()
        applyBossPromotion(entity, tier, nearestPlayer, cooldownData, currentTick)
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
