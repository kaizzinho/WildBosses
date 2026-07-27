package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import java.util.UUID

/**
 * Watches a boss for the ENTIRE duration of a battle, discarding any stray ItemEntity that
 * appears near it the instant it spawns. Cobblemon appears to grant a fainted wild Pokémon's
 * species-level default loot the moment its HP hits 0 mid-battle - well before BATTLE_VICTORY
 * fires (that only happens after the full recall/fog animation completes) - so reacting at
 * battle-end left the item visible for the whole animation. Actively polling every tick
 * catches it essentially the instant it appears, regardless of exactly when mid-fight it happens.
 */
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