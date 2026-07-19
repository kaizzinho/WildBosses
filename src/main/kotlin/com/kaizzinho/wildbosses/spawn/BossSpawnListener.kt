package com.kaizzinho.wildbosses.spawn

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import kotlin.random.Random

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

    private fun promoteToBoss(entity: PokemonEntity) {
        //se já tem a tag de boss, interrompe a função
        if (entity.tags.contains("wildbosses:is_boss")) {
            return
        }

        val tier = BossTier.rollRandomTier()
        val currentTick = entity.level().gameTime

        //aplica a propriedade que impede a captura
        UncatchableProperty.uncatchable().apply(entity.pokemon)

        //substituímos o persistentData pelas tags do vanilla
        entity.tags.add("wildbosses:is_boss")
        entity.tags.add("wildbosses:tier_${tier.name}") // Salva o tier na tag também!

        //Registra no mapa em memória do mod
        BossRegistry.register(entity, tier, currentTick)

        WildBosses.logger.info(
            "[WildBosses] Spawned ${tier.name} boss: ${entity.pokemon.species.name} at ${entity.blockPosition()}"
        )
    }
}