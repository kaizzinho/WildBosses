package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import java.util.UUID

object BossPersistence {

    private const val SPAWN_TICK_TAG_PREFIX = "wildbosses:spawned_at_"
    private val pendingCheckpoints = linkedMapOf<UUID, PendingCheckpoint>()
    private val pendingLevelCheckpoints = linkedSetOf<ServerLevel>()
    private val pendingClientResyncs = linkedMapOf<UUID, PendingClientResync>()

    private data class PendingCheckpoint(
        val entityUuid: UUID,
        val level: ServerLevel
    )

    private data class PendingClientResync(
        val entityUuid: UUID,
        val level: ServerLevel,
        val tier: BossTier,
        val spawnedAtTick: Long
    )

    fun register() {
        WildBosses.logger.warn("[WildBosses-PERSISTENCE-PROBE] BossPersistence.register build=${WildBosses.PERSISTENCE_PROBE_BUILD}")

        ServerLifecycleEvents.SERVER_STOPPED.register {
            pendingCheckpoints.clear()
            pendingLevelCheckpoints.clear()
            pendingClientResyncs.clear()
            BossRegistry.clear()
        }

        ServerTickEvents.END_SERVER_TICK.register checkpointTick@{ _ ->
            if (
                pendingCheckpoints.isEmpty() &&
                pendingLevelCheckpoints.isEmpty() &&
                pendingClientResyncs.isEmpty()
            ) return@checkpointTick

            val resyncs = pendingClientResyncs.values.toList()
            pendingClientResyncs.clear()
            for (resync in resyncs) {
                val entity = resync.level.getEntity(resync.entityUuid) as? PokemonEntity ?: continue
                if (entity.isRemoved) continue

                restoreRuntimeState(entity, resync.tier, resync.spawnedAtTick)
                entity.entityData.set(WildBossEntityData.IS_BOSS, true, true)
                entity.entityData.set(WildBossEntityData.TIER, resync.tier.name, true)
                entity.entityData.set(PokemonEntity.LABEL_LEVEL, -1, true)
                entity.setGlowingTag(false)
                entity.setGlowingTag(true)

                logState("POST_TRACKING_RESYNC", entity, "tier=${resync.tier.name}")
            }

            val pending = pendingCheckpoints.values.toList()
            pendingCheckpoints.clear()

            val levelsToSave = linkedSetOf<ServerLevel>()
            levelsToSave += pendingLevelCheckpoints
            pendingLevelCheckpoints.clear()
            for (checkpoint in pending) {
                val entity = checkpoint.level.getEntity(checkpoint.entityUuid) as? PokemonEntity
                if (entity == null || entity.isRemoved || !isBossState(entity)) {
                    WildBosses.logger.info(
                        "[BossPersistence-DEBUG] CHECKPOINT_SKIP uuid=${checkpoint.entityUuid} " +
                            "entityPresent=${entity != null} removed=${entity?.isRemoved} " +
                            "bossTag=${entity?.tags?.contains("wildbosses:is_boss")} registry=${BossRegistry.isBoss(checkpoint.entityUuid)}"
                    )
                    continue
                }

                checkpoint.level.getChunkAt(entity.blockPosition()).setUnsaved(true)
                levelsToSave += checkpoint.level
                logState("CHECKPOINT_READY", entity)
            }

            for (level in levelsToSave) {
                try {
                    level.save(null, true, false)
                    WildBosses.logger.info(
                        "[BossPersistence-DEBUG] LEVEL_CHECKPOINT_SAVED dimension=${level.dimension().location()}"
                    )
                } catch (e: Exception) {
                    WildBosses.logger.error("[WildBosses] Failed to checkpoint active Bosses", e)
                }
            }
        }
    }

    fun mark(entity: PokemonEntity, spawnedAtTick: Long) {
        entity.setPersistenceRequired()
        entity.tags.removeIf { it.startsWith(SPAWN_TICK_TAG_PREFIX) }
        entity.tags.add("$SPAWN_TICK_TAG_PREFIX$spawnedAtTick")
    }

    fun restoreRuntimeState(entity: PokemonEntity, tier: BossTier, spawnedAtTick: Long) {
        mark(entity, spawnedAtTick)

        entity.tags.add("wildbosses:is_boss")
        entity.tags.removeIf { it.startsWith("wildbosses:tier_") }
        entity.tags.add("wildbosses:tier_${tier.name}")

        entity.entityData.set(WildBossEntityData.IS_BOSS, true)
        entity.entityData.set(WildBossEntityData.TIER, tier.name)
        entity.entityData.set(PokemonEntity.LABEL_LEVEL, -1)

        val scoreboard = entity.server?.scoreboard
        if (scoreboard != null) {
            val teamName = "wildbosses_${tier.name.lowercase()}"
            val team = scoreboard.getPlayerTeam(teamName) ?: scoreboard.addPlayerTeam(teamName)
            team.setColor(tier.color)
            scoreboard.addPlayerToTeam(entity.scoreboardName, team)
        }

        entity.setGlowingTag(true)
    }

    fun requestCheckpoint(entity: PokemonEntity) {
        val level = entity.level() as? ServerLevel ?: return
        pendingCheckpoints[entity.uuid] = PendingCheckpoint(entity.uuid, level)
        WildBosses.logger.warn(
            "[WildBosses-PERSISTENCE-PROBE] CHECKPOINT_REQUESTED build=${WildBosses.PERSISTENCE_PROBE_BUILD} " +
                "species=${entity.pokemon.species.name} uuid=${entity.uuid}"
        )
        logState("CHECKPOINT_REQUESTED", entity)
    }

    fun requestLevelCheckpoint(level: ServerLevel) {
        pendingLevelCheckpoints += level
        WildBosses.logger.info(
            "[BossPersistence-DEBUG] LEVEL_CHECKPOINT_REQUESTED dimension=${level.dimension().location()}"
        )
    }

    fun requestClientResync(entity: PokemonEntity, tier: BossTier, spawnedAtTick: Long) {
        val level = entity.level() as? ServerLevel ?: return
        pendingClientResyncs[entity.uuid] = PendingClientResync(entity.uuid, level, tier, spawnedAtTick)
        logState("TRACKING_RESYNC_QUEUED", entity, "tier=${tier.name}")
    }

    fun readSpawnedAtTick(entity: PokemonEntity): Long? =
        entity.tags.firstOrNull { it.startsWith(SPAWN_TICK_TAG_PREFIX) }
            ?.removePrefix(SPAWN_TICK_TAG_PREFIX)
            ?.toLongOrNull()

    fun probeState(phase: String, entity: PokemonEntity, extra: String = "") {
        val syncedBoss = runCatching { entity.entityData.get(WildBossEntityData.IS_BOSS) }.getOrNull()
        val syncedTier = runCatching { entity.entityData.get(WildBossEntityData.TIER) }.getOrNull()
        val labelLevel = runCatching { entity.entityData.get(PokemonEntity.LABEL_LEVEL) }.getOrNull()
        val teamName = entity.team?.name ?: "<none>"
        val suffix = if (extra.isBlank()) "" else " $extra"
        WildBosses.logger.warn(
            "[WildBosses-PERSISTENCE-PROBE] $phase build=${WildBosses.PERSISTENCE_PROBE_BUILD} " +
                "species=${entity.pokemon.species.name} entityUuid=${entity.uuid} pokemonUuid=${entity.pokemon.uuid} " +
                "registry=${BossRegistry.isBoss(entity.uuid)} bossTag=${entity.tags.contains("wildbosses:is_boss")} " +
                "syncedBoss=$syncedBoss syncedTier=$syncedTier pokemonLevel=${entity.pokemon.level} " +
                "labelLevel=$labelLevel team=$teamName glowing=${entity.isCurrentlyGlowing()} " +
                "persistenceRequired=${entity.isPersistenceRequired()} removed=${entity.isRemoved}$suffix"
        )
    }

    fun logState(phase: String, entity: PokemonEntity, extra: String = "") {
        val syncedBoss = runCatching { entity.entityData.get(WildBossEntityData.IS_BOSS) }.getOrNull()
        val syncedTier = runCatching { entity.entityData.get(WildBossEntityData.TIER) }.getOrNull()
        val labelLevel = runCatching { entity.entityData.get(PokemonEntity.LABEL_LEVEL) }.getOrNull()
        val tierTag = entity.tags.firstOrNull { it.startsWith("wildbosses:tier_") }
        val spawnTag = entity.tags.firstOrNull { it.startsWith(SPAWN_TICK_TAG_PREFIX) }
        val suffix = if (extra.isBlank()) "" else " $extra"

        WildBosses.logger.info(
            "[BossPersistence-DEBUG] $phase species=${entity.pokemon.species.name} " +
                "entityUuid=${entity.uuid} pokemonUuid=${entity.pokemon.uuid} " +
                "registry=${BossRegistry.isBoss(entity.uuid)} bossTag=${entity.tags.contains("wildbosses:is_boss")} " +
                "tierTag=$tierTag spawnTag=$spawnTag syncedBoss=$syncedBoss syncedTier=$syncedTier " +
                "pokemonLevel=${entity.pokemon.level} labelLevel=$labelLevel glowing=${entity.isCurrentlyGlowing()} " +
                "persistenceRequired=${entity.isPersistenceRequired()} removed=${entity.isRemoved} " +
                "dimension=${(entity.level() as? ServerLevel)?.dimension()?.location()} pos=${entity.blockPosition()}$suffix"
        )
    }

    private fun isBossState(entity: PokemonEntity): Boolean =
        entity.tags.contains("wildbosses:is_boss") ||
            BossRegistry.isBoss(entity.uuid) ||
            runCatching { entity.entityData.get(WildBossEntityData.IS_BOSS) }.getOrDefault(false)
}
