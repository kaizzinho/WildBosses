package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object BossLifecycleTicker {

    private const val BOSS_LIFETIME_TICKS = 12000L // 10 minutos
    private const val CHECK_INTERVAL_TICKS = 20L

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
                var found: PokemonEntity? = null
                for (level in server.allLevels) {
                    val entity = level.getEntity(instance.entityUuid)
                    if (entity is PokemonEntity) {
                        found = entity
                        break
                    }
                }

                if (found != null && !found.isRemoved) {
                    found.discard()
                }

                // Remove the tier's glow-team membership. This works purely off the string
                // UUID, so it's safe even in the "entity already gone" branch above - team
                // membership isn't tied to a live entity reference.
                server.scoreboard.removePlayerFromTeam(instance.entityUuid.toString())

                BossRegistry.unregister(instance.entityUuid)
            }
        }
    }
}
