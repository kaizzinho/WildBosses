package com.kaizzinho.wildbosses.api

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import java.util.UUID

object WildBossIntegrationApi {
    private const val BOSS_TAG = "wildbosses:is_boss"
    private const val TIER_TAG_PREFIX = "wildbosses:tier_"
    private const val TEAM_PREFIX = "wildbosses_"

    @JvmStatic
    fun isBoss(entity: PokemonEntity): Boolean {
        if (BossRegistry.isBoss(entity.uuid)) return true
        if (entity.tags.contains(BOSS_TAG)) return true
        return entity.team?.name?.startsWith(TEAM_PREFIX) == true
    }

    @JvmStatic
    fun getTierName(entity: PokemonEntity): String? {
        BossRegistry.get(entity.uuid)?.tier?.name?.let { return it }

        entity.tags.firstOrNull { it.startsWith(TIER_TAG_PREFIX) }
            ?.removePrefix(TIER_TAG_PREFIX)
            ?.let(::normalizeTierName)
            ?.let { return it }

        return entity.team?.name
            ?.takeIf { it.startsWith(TEAM_PREFIX) }
            ?.removePrefix(TEAM_PREFIX)
            ?.let(::normalizeTierName)
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

    private fun normalizeTierName(name: String): String? =
        BossTier.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }?.name
}
