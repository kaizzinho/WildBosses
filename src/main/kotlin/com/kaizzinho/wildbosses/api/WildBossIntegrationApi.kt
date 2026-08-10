package com.kaizzinho.wildbosses.api

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.WildBossEntityData
import java.util.UUID

object WildBossIntegrationApi {
    @JvmStatic
    fun isBoss(entity: PokemonEntity): Boolean {
        if (BossRegistry.isBoss(entity.uuid)) return true
        return runCatching { entity.entityData.get(WildBossEntityData.IS_BOSS) }.getOrDefault(false)
    }

    @JvmStatic
    fun getTierName(entity: PokemonEntity): String? {
        BossRegistry.get(entity.uuid)?.tier?.name?.let { return it }
        return runCatching { entity.entityData.get(WildBossEntityData.TIER) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    @JvmStatic
    fun getTierName(entityUuid: UUID): String? = BossRegistry.get(entityUuid)?.tier?.name

    @JvmStatic
    fun getTierNameByPokemonUuid(pokemonUuid: UUID): String? =
        BossRegistry.getByPokemonUuid(pokemonUuid)?.tier?.name

    @JvmStatic
    fun getScaledLevel(entity: PokemonEntity): Int = entity.pokemon.level

    @JvmStatic
    fun getScaledLevel(entityUuid: UUID): Int? = BossRegistry.get(entityUuid)?.currentLevelOverride
}
