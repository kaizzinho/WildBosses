package com.kaizzinho.wildbosses.spawn


import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossSpawnCooldownData
import com.kaizzinho.wildbosses.boss.BossTier
import kotlin.random.Random
import com.kaizzinho.wildbosses.boss.WildBossEntityData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import com.kaizzinho.wildbosses.boss.BossEvolutionResolver
import net.minecraft.server.level.ServerLevel



object BossSpawnListener {
    private const val BOSS_SPAWN_CHANCE = 1.0/20
    private const val SPAWN_COOLDOWN_TICKS = 6000L // 5 minutes - adjust as desired

    private val EXCLUDED_SPECIES_LABELS = setOf("legendary", "mythical", "ultra_beast")
    private val PERFECT_IV_TIERS = setOf(BossTier.EPIC, BossTier.LEGENDARY, BossTier.MYTHIC)
    private val SHINY_CHANCE = mapOf(
        BossTier.LEGENDARY to 0.25,
        BossTier.MYTHIC to 0.45
    )

    fun register() {
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            val entity: PokemonEntity = event.entity
            if (Random.nextDouble() < BOSS_SPAWN_CHANCE) {
                promoteToBoss(entity)
            }
        }
    }

    private fun announceSpawn(entity: PokemonEntity, tier: BossTier, targetPlayer: ServerPlayer) {
        val server = entity.server ?: return

        val tierLabel = tier.name.lowercase().replaceFirstChar { it.uppercase() }
        val message = Component.literal(tierLabel).withStyle(tier.color)
            .append(Component.literal(" Boss ${entity.pokemon.species.name} spawned near ${targetPlayer.name.string}!"))

        server.playerList.broadcastSystemMessage(message, false)
    }

    private fun applyBossStats(entity: PokemonEntity, tier: BossTier) {
        val properties = PokemonProperties()

        if (tier in PERFECT_IV_TIERS) {
            properties.minPerfectIVs = 6
        }

        val shinyChance = SHINY_CHANCE[tier]
        if (shinyChance != null && Random.nextDouble() < shinyChance) {
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
        if (speciesLabels.any { it in EXCLUDED_SPECIES_LABELS }) return

        val level = entity.level() as? ServerLevel ?: return

        // Determine the target player BEFORE deciding whether to promote at all - the
        // cooldown gates whether a boss spawns targeting this specific player, not whether
        // any boss can exist near them (a friend's boss can still be engaged freely).
        val nearestPlayer = level.players().minByOrNull { it.distanceToSqr(entity) }
            ?: return // no player nearby at all, nothing to target

        val server = entity.server ?: return
        val overworld = server.overworld()
        val currentTick = overworld.gameTime

        val cooldownData = BossSpawnCooldownData.get(overworld)
        if (cooldownData.isOnCooldown(nearestPlayer.uuid, currentTick)) {
            return // this player is on cooldown - skip promotion, stays an ordinary wild spawn
        }

        val tier = BossTier.rollRandomTier()
        applyBossPromotion(entity, tier, nearestPlayer, cooldownData, currentTick)
    }

    /**
     * Applies full boss-promotion logic (evolution, stats, tags, glow, registry, announcement,
     * cooldown) to an entity for a GIVEN tier, targeting a GIVEN player, bypassing the random
     * roll and species blacklist. Used by /wildbosses spawn for testing - note this still
     * respects and starts the target player's cooldown like a natural spawn would.
     */
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
        entity.tags.add("wildbosses:is_boss")
        entity.tags.add("wildbosses:tier_${tier.name}")
        entity.entityData.set(WildBossEntityData.IS_BOSS, true)
        entity.entityData.set(WildBossEntityData.TIER, tier.name)
        applyTierGlow(entity, tier)
        BossRegistry.register(entity, tier, currentTick)
        announceSpawn(entity, tier, targetPlayer)

        cooldownData.startCooldown(targetPlayer.uuid, currentTick, SPAWN_COOLDOWN_TICKS)

        WildBosses.logger.info(
            "[WildBosses] Spawned ${tier.name} boss: ${entity.pokemon.species.name} at ${entity.blockPosition()} " +
                    "(target: ${targetPlayer.name.string}, cooldown starts)"
        )
    }
}