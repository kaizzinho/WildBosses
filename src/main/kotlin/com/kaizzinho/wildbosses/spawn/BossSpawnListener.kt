package com.kaizzinho.wildbosses.spawn


import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import kotlin.random.Random
import com.kaizzinho.wildbosses.boss.WildBossEntityData
import net.minecraft.network.chat.Component
import com.kaizzinho.wildbosses.boss.BossEvolutionResolver

object BossSpawnListener {
    private const val BOSS_SPAWN_CHANCE = 1.0/20

    fun register() {
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            val entity: PokemonEntity = event.entity
            if (Random.nextDouble() < BOSS_SPAWN_CHANCE) {
                promoteToBoss(entity)
            }
        }
    }

    private fun announceSpawn(entity: PokemonEntity, tier: BossTier) {
        val server = entity.server ?: return
        val nearestPlayer = entity.level().players().minByOrNull { it.distanceToSqr(entity) }
        val nearestName = nearestPlayer?.name?.string ?: "someone"

        val tierLabel = tier.name.lowercase().replaceFirstChar { it.uppercase() }
        val message = Component.literal(tierLabel).withStyle(tier.color)
            .append(Component.literal(" Boss ${entity.pokemon.species.name} spawned near $nearestName!"))

        server.playerList.broadcastSystemMessage(message, false)
    }

    private fun promoteToBoss(entity: PokemonEntity) {
        if (entity.tags.contains("wildbosses:is_boss")) return
        val tier = BossTier.rollRandomTier()
        val currentTick = entity.level().gameTime

        if (tier == BossTier.EPIC || tier == BossTier.LEGENDARY || tier == BossTier.MYTHIC) {
            val resolved = BossEvolutionResolver.resolveFinalForm(entity.pokemon.species, entity.pokemon.form)
            entity.pokemon.species = resolved.species
            entity.pokemon.form = resolved.form
            entity.pokemon.updateAspects()
        }

        UncatchableProperty.uncatchable().apply(entity.pokemon)
        entity.tags.add("wildbosses:is_boss")
        entity.tags.add("wildbosses:tier_${tier.name}")
        entity.entityData.set(WildBossEntityData.IS_BOSS, true)
        entity.entityData.set(WildBossEntityData.TIER, tier.name)
        BossRegistry.register(entity, tier, currentTick)
        announceSpawn(entity, tier)

        WildBosses.logger.info("[WildBosses] Spawned ${tier.name} boss: ${entity.pokemon.species.name} at ${entity.blockPosition()}")
    }
}