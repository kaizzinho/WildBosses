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

    // Read live from config on every access, rather than fixed once at enum construction -
    // this is what makes /wb reloadconfig actually take effect without a server restart.
    val levelBonus: Int get() = WildBossesConfig.tier(name).levelBonus
    val weight: Int get() = WildBossesConfig.tier(name).weight
    val aiSkill: Int get() = WildBossesConfig.tier(name).aiSkill

    companion object {
        fun rollRandomTier(): BossTier {
            val totalWeight = entries.sumOf { it.weight } // recomputed each roll - reflects a live reload
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