package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.UUID

data class BossInstance(
    val entityUuid: UUID,
    val tier: BossTier,
    val spawnedAtTick: Long,
    var currentLevelOverride: Int? = null
)

object BossRegistry {
    private val activeBosses = mutableMapOf<UUID, BossInstance>()

    fun register(entity: PokemonEntity, tier: BossTier, currentTick: Long) {
        activeBosses[entity.uuid] = BossInstance(
            entityUuid = entity.uuid,
            tier = tier,
            spawnedAtTick = currentTick
        )
    }

    fun get(uuid: UUID): BossInstance? = activeBosses[uuid]
    fun isBoss(uuid: UUID): Boolean = activeBosses.containsKey(uuid)
    fun unregister(uuid: UUID) { activeBosses.remove(uuid) }
    fun allBosses(): Collection<BossInstance> = activeBosses.values
}