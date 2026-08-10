package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import java.util.UUID

object StrayLootWatcher {

    private const val CHECK_INTERVAL_TICKS = 1L
    private const val CLEANUP_RADIUS = 3.0

    private val watchedBosses = mutableMapOf<UUID, ServerLevel>()

    fun startWatching(bossEntityUuid: UUID, level: ServerLevel) {
        watchedBosses[bossEntityUuid] = level
    }

    fun stopWatching(bossEntityUuid: UUID) {
        watchedBosses.remove(bossEntityUuid)
    }

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register {
            if (watchedBosses.isEmpty()) return@register

            for ((bossUuid, level) in watchedBosses) {
                val entity = level.getEntity(bossUuid) as? PokemonEntity ?: continue
                val pos = entity.position()

                val strayItems = level.getEntitiesOfClass(
                    ItemEntity::class.java,
                    AABB(
                        pos.x - CLEANUP_RADIUS, pos.y - CLEANUP_RADIUS, pos.z - CLEANUP_RADIUS,
                        pos.x + CLEANUP_RADIUS, pos.y + CLEANUP_RADIUS, pos.z + CLEANUP_RADIUS
                    )
                )
                if (strayItems.isNotEmpty()) {
                    strayItems.forEach { it.discard() }
                    WildBosses.logger.info("[WildBosses] Discarded ${strayItems.size} stray item(s) near boss mid-battle")
                }
            }
        }
    }
}
