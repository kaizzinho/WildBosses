package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import java.util.UUID

data class BossInstance(
    val entityUuid: UUID,
    val pokemonUuid: UUID,
    val tier: BossTier,
    val spawnedAtTick: Long,
    var currentLevelOverride: Int? = null,
    var lastKnownPos: Vec3? = null,
    var lastKnownLevel: ServerLevel? = null,
    var speciesName: String? = null,
    var megaStoneItemId: ResourceLocation? = null,
    var defeated: Boolean = false
)

object BossRegistry {
    private val activeBosses = mutableMapOf<UUID, BossInstance>()
    private val byPokemonUuid = mutableMapOf<UUID, BossInstance>()

    fun register(entity: PokemonEntity, tier: BossTier, currentTick: Long) {
        val instance = BossInstance(
            entityUuid = entity.uuid,
            pokemonUuid = entity.pokemon.uuid,
            tier = tier,
            spawnedAtTick = currentTick,
            speciesName = entity.pokemon.species.name
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

    fun clear() {
        activeBosses.clear()
        byPokemonUuid.clear()
    }
}
