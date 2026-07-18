package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object BossLifecycleTicker {

    private const val BOSS_LIFETIME_TICKS = 12000L // 10 minutes
    private const val CHECK_INTERVAL_TICKS = 20L   // throttle to once per second, not every tick

    private var tickCounter = 0L

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            tickCounter++
            if (tickCounter % CHECK_INTERVAL_TICKS != 0L) return@register

            val currentTick = server.overworld().gameTime
            val expired = BossRegistry.allBosses().filter { instance ->
                currentTick - instance.spawnedAtTick >= BOSS_LIFETIME_TICKS
            }

            for (instance in expired) {
                // BossInstance only stores the UUID, not a live entity reference,
                // so we have to look it up across dimensions - a boss's exact
                // level isn't tracked, so we check every loaded level.
                var found: PokemonEntity? = null
                for (level in server.allLevels) {
                    val entity = level.getEntity(instance.entityUuid)
                    if (entity is PokemonEntity) {
                        found = entity
                        break
                    }
                }

                if (found != null && !found.isRemoved) {
                    WildBosses.logger.info(
                        "[WildBosses] Boss lifetime expired, despawned ${instance.tier.name} ${found.pokemon.species.name} at ${found.blockPosition()}"
                    )
                    found.discard()
                } else {
                    WildBosses.logger.info(
                        "[WildBosses] Boss lifetime expired for ${instance.entityUuid} (entity already gone, cleaning up registry only)"
                    )
                }

                BossRegistry.unregister(instance.entityUuid)
            }
        }
    }
}