package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents

object BossOrphanCleanupListener {

    fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (entity !is PokemonEntity) return@register
            if (!entity.tags.contains("wildbosses:is_boss")) return@register

            val existing = BossRegistry.get(entity.uuid)
            if (existing != null) {
                // Already tracked in-memory (e.g. "Save and Quit to Title" then reloading the
                // same world WITHOUT a full game restart - BossRegistry is a JVM-singleton
                // object and survives that, but this PokemonEntity is a brand-new object
                // reconstructed from saved NBT, so its synced entity data still resets to
                // defaults regardless). Just restore the visible fields, leave the existing
                // registration/timer alone.
                entity.entityData.set(WildBossEntityData.IS_BOSS, true)
                entity.entityData.set(WildBossEntityData.TIER, existing.tier.name)
                return@register
            }

            // Not tracked at all - genuine full restart, BossRegistry itself was wiped.
            // Recover the tier from the surviving vanilla tag instead.
            val tierTag = entity.tags.firstOrNull { it.startsWith("wildbosses:tier_") }
            val tier = tierTag?.removePrefix("wildbosses:tier_")?.let { name ->
                BossTier.entries.firstOrNull { it.name == name }
            }

            if (tier == null) {
                WildBosses.logger.warn(
                    "[WildBosses] Found boss-tagged entity ${entity.pokemon.species.name} (uuid=${entity.uuid}) " +
                            "with no recoverable tier tag - discarding as unrecoverable"
                )
                world.scoreboard.removePlayerFromTeam(entity.uuid.toString())
                entity.tags.remove("wildbosses:is_boss")
                entity.discard()
                return@register
            }

            entity.entityData.set(WildBossEntityData.IS_BOSS, true)
            entity.entityData.set(WildBossEntityData.TIER, tier.name)

            BossRegistry.register(entity, tier, world.gameTime)

            //WildBosses.logger.info(
             //   "[WildBosses] Recovered ${tier.name} boss ${entity.pokemon.species.name} (uuid=${entity.uuid}) " +
             //           "after server restart - re-registered with a fresh lifetime timer"
            //)
        }
    }
}