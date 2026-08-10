package com.kaizzinho.wildbosses.boss

import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.minecraft.ChatFormatting
import kotlin.random.Random

enum class BossTier(val color: ChatFormatting) {
    UNCOMMON(ChatFormatting.GREEN),
    RARE(ChatFormatting.AQUA),
    EPIC(ChatFormatting.LIGHT_PURPLE),
    LEGENDARY(ChatFormatting.GOLD),
    MYTHIC(ChatFormatting.YELLOW);

    val levelBonus: Int get() = WildBossesConfig.tier(name).levelBonus
    val weight: Int get() = WildBossesConfig.tier(name).weight
    val aiSkill: Int get() = WildBossesConfig.tier(name).aiSkill

    companion object {
        fun rollRandomTier(): BossTier {
            val totalWeight = entries.sumOf { it.weight }
            val roll = Random.nextInt(totalWeight)
            var cumulative = 0
            for (tier in entries) {
                cumulative += tier.weight
                if (roll < cumulative) return tier
            }
            return entries.last()
        }
    }
}
