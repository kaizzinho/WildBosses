package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData

object WildBossEntityData {
    @JvmField
    val IS_BOSS: EntityDataAccessor<Boolean> =
        SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.BOOLEAN)

    @JvmField
    val TIER: EntityDataAccessor<String> =
        SynchedEntityData.defineId(PokemonEntity::class.java, EntityDataSerializers.STRING)
}