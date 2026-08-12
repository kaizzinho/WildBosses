package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents

object BossOrphanCleanupListener {

    fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (entity !is PokemonEntity) return@register

            val existing = BossRegistry.get(entity.uuid)
            val hasBossTag = entity.tags.contains("wildbosses:is_boss")
            val hasSyncedBossState = runCatching {
                entity.entityData.get(WildBossEntityData.IS_BOSS)
            }.getOrDefault(false)
            val teamTier = recoverTierFromTeam(entity)
            val hasBossTeam = teamTier != null

            if (existing != null || hasBossTag || hasSyncedBossState || hasBossTeam || entity.isPersistenceRequired()) {
                BossPersistence.probeState(
                    "ENTITY_LOAD_SEEN",
                    entity,
                    "existingRegistry=${existing != null} hasBossTag=$hasBossTag hasSyncedBossState=$hasSyncedBossState hasBossTeam=$hasBossTeam"
                )
            }

            if (existing == null && !hasBossTag && !hasSyncedBossState && !hasBossTeam) return@register

            BossPersistence.logState(
                "ENTITY_LOAD_PRE",
                entity,
                "existingRegistry=${existing != null} hasBossTag=$hasBossTag hasSyncedBossState=$hasSyncedBossState"
            )

            val tier = existing?.tier ?: recoverTier(entity) ?: teamTier

            if (tier == null) {
                WildBosses.logger.warn(
                    "[WildBosses] Found boss-marked entity ${entity.pokemon.species.name} (uuid=${entity.uuid}) " +
                        "with no recoverable tier - discarding as unrecoverable"
                )
                world.scoreboard.removePlayerFromTeam(entity.uuid.toString())
                entity.tags.remove("wildbosses:is_boss")
                entity.discard()
                BossPersistence.requestLevelCheckpoint(world)
                return@register
            }

            val spawnedAtTick = existing?.spawnedAtTick
                ?: BossPersistence.readSpawnedAtTick(entity)
                ?: world.gameTime

            val instance = existing ?: run {
                BossRegistry.register(entity, tier, spawnedAtTick)
                BossRegistry.get(entity.uuid)!!
            }

            if (!instance.defeated && world.gameTime - instance.spawnedAtTick >= WildBossesConfig.data.bossLifetimeTicks) {
                BossDepartureMessages.announceLifetimeExpired(
                    world.server,
                    instance,
                    entity.pokemon.species.name
                )
                world.scoreboard.removePlayerFromTeam(entity.uuid.toString())
                BossRegistry.unregister(entity.uuid)
                entity.discard()
                BossPersistence.requestLevelCheckpoint(world)
                return@register
            }

            BossPersistence.restoreRuntimeState(entity, tier, spawnedAtTick)
            BossPersistence.logState("ENTITY_LOAD_POST", entity, "tier=${tier.name}")
            BossPersistence.requestCheckpoint(entity)
        }

        ServerEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            if (entity !is PokemonEntity) return@register
            if (!BossRegistry.isBoss(entity.uuid) && !entity.tags.contains("wildbosses:is_boss")) return@register
            BossPersistence.logState("ENTITY_UNLOAD", entity)
        }

        EntityTrackingEvents.START_TRACKING.register { trackedEntity, player ->
            val entity = trackedEntity as? PokemonEntity ?: return@register
            val existing = BossRegistry.get(entity.uuid)
            val teamTier = recoverTierFromTeam(entity)
            if (existing == null && !entity.tags.contains("wildbosses:is_boss") && teamTier == null) return@register

            val tier = existing?.tier ?: recoverTier(entity) ?: teamTier
            if (tier == null) {
                WildBosses.logger.warn(
                    "[BossPersistence-DEBUG] START_TRACKING could not recover tier for " +
                        "species=${entity.pokemon.species.name} uuid=${entity.uuid} player=${player.name.string}"
                )
                return@register
            }

            val spawnedAtTick = existing?.spawnedAtTick
                ?: BossPersistence.readSpawnedAtTick(entity)
                ?: player.serverLevel().server.overworld().gameTime

            if (existing == null) {
                BossRegistry.register(entity, tier, spawnedAtTick)
            }

            BossPersistence.restoreRuntimeState(entity, tier, spawnedAtTick)
            BossPersistence.logState(
                "START_TRACKING",
                entity,
                "player=${player.name.string} tier=${tier.name}"
            )
            BossPersistence.requestClientResync(entity, tier, spawnedAtTick)
        }

        EntityTrackingEvents.STOP_TRACKING.register { trackedEntity, player ->
            val entity = trackedEntity as? PokemonEntity ?: return@register
            val teamTier = recoverTierFromTeam(entity)
            if (!BossRegistry.isBoss(entity.uuid) && !entity.tags.contains("wildbosses:is_boss") && teamTier == null) return@register
            BossPersistence.probeState(
                "STOP_TRACKING",
                entity,
                "player=${player.name.string} teamTier=${teamTier?.name ?: "<none>"}"
            )
        }
    }

    private fun recoverTier(entity: PokemonEntity): BossTier? {
        val syncedTier = runCatching { entity.entityData.get(WildBossEntityData.TIER) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { name -> BossTier.entries.firstOrNull { it.name == name } }
        if (syncedTier != null) return syncedTier

        val tierTag = entity.tags.firstOrNull { it.startsWith("wildbosses:tier_") }
        return tierTag?.removePrefix("wildbosses:tier_")?.let { name ->
            BossTier.entries.firstOrNull { it.name == name }
        }
    }


    private fun recoverTierFromTeam(entity: PokemonEntity): BossTier? {
        val teamName = entity.team?.name ?: return null
        if (!teamName.startsWith("wildbosses_")) return null
        val tierName = teamName.removePrefix("wildbosses_")
        return BossTier.entries.firstOrNull { it.name.equals(tierName, ignoreCase = true) }
    }
}
