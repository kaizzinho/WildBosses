package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.api.entity.Despawner
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

class BossDespawnAwareDespawner(
    private val delegate: Despawner<PokemonEntity>
) : Despawner<PokemonEntity> {

    override fun beginTracking(entity: PokemonEntity) {
        delegate.beginTracking(entity)
    }

    override fun shouldDespawn(entity: PokemonEntity): Boolean {
        val shouldDespawn = delegate.shouldDespawn(entity)
        if (shouldDespawn) {
            BossDepartureMessages.announceNaturalDespawn(entity)
        }
        return shouldDespawn
    }

    companion object {
        fun install(entity: PokemonEntity) {
            if (entity.despawner is BossDespawnAwareDespawner) return
            entity.despawner = BossDespawnAwareDespawner(entity.despawner)
        }
    }
}
