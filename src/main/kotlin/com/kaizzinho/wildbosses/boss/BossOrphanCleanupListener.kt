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
                entity.entityData.set(WildBossEntityData.IS_BOSS, true)
                entity.entityData.set(WildBossEntityData.TIER, existing.tier.name)
                BossDespawnAwareDespawner.install(entity)
                return@register
            }

            // Restart? Rebuild from the saved tier tag.
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
            BossDespawnAwareDespawner.install(entity)

        }
    }
}
