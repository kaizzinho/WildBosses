package com.kaizzinho.wildbosses.advancement

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object WildBossCriteria {

    lateinit var BOSS_DEFEATED: BossDefeatedTrigger
        private set
    lateinit var BOSS_FLED: BossFledTrigger
        private set

    fun register() {
        BOSS_DEFEATED = Registry.register(
            BuiltInRegistries.TRIGGER_TYPES,
            ResourceLocation.fromNamespaceAndPath("wildbosses", "boss_defeated"),
            BossDefeatedTrigger()
        )
        BOSS_FLED = Registry.register(
            BuiltInRegistries.TRIGGER_TYPES,
            ResourceLocation.fromNamespaceAndPath("wildbosses", "boss_fled"),
            BossFledTrigger()
        )
    }
}