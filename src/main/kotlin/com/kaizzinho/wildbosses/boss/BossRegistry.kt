package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.UUID

data class BossInstance(
    val entityUuid: UUID,
    val pokemonUuid: UUID,
    val tier: BossTier,
    val spawnedAtTick: Long,
    var currentLevelOverride: Int? = null,
    // Captured at battle start (BattleTierScalingListener), while the entity still exists.
    // The boss's PokemonEntity gets removed from the world once it faints, so by the time
    // BATTLE_VICTORY fires, wildLoser.entity is already null - we need these stashed ahead
    // of time to know where/how to award loot.
    var lastKnownPos: Vec3? = null,
    var lastKnownLevel: ServerLevel? = null,
    var speciesName: String? = null
)

object BossRegistry {
    private val activeBosses = mutableMapOf<UUID, BossInstance>()          // keyed by entity UUID
    private val byPokemonUuid = mutableMapOf<UUID, BossInstance>()          // keyed by Pokémon's own internal UUID

    fun register(entity: PokemonEntity, tier: BossTier, currentTick: Long) {
        val instance = BossInstance(
            entityUuid = entity.uuid,
            pokemonUuid = entity.pokemon.uuid,
            tier = tier,
            spawnedAtTick = currentTick
        )
        activeBosses[entity.uuid] = instance
        byPokemonUuid[entity.pokemon.uuid] = instance
    }

    fun get(uuid: UUID): BossInstance? = activeBosses[uuid]
    fun getByPokemonUuid(pokemonUuid: UUID): BossInstance? = byPokemonUuid[pokemonUuid]
    fun isBoss(uuid: UUID): Boolean = activeBosses.containsKey(uuid)

    fun unregister(uuid: UUID) {
        val instance = activeBosses.remove(uuid)
        if (instance != null) byPokemonUuid.remove(instance.pokemonUuid)
    }

    fun allBosses(): Collection<BossInstance> = activeBosses.values
}