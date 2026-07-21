package com.kaizzinho.wildbosses.boss

import net.minecraft.ChatFormatting
import kotlin.random.Random

enum class BossTier(val levelBonus: Int, val color: ChatFormatting, val weight: Int) {
    UNCOMMON(10, ChatFormatting.GREEN, 45),
    RARE(20, ChatFormatting.AQUA, 25),
    EPIC(30, ChatFormatting.LIGHT_PURPLE, 15),
    LEGENDARY(40, ChatFormatting.GOLD, 10),
    MYTHIC(50, ChatFormatting.YELLOW, 5);

    companion object {
        private val totalWeight = entries.sumOf { it.weight }

        fun rollRandomTier(): BossTier {
            val roll = Random.nextInt(totalWeight)
            var cumulative = 0
            for (tier in entries) {
                cumulative += tier.weight
                if (roll < cumulative) return tier
            }
            return entries.last() // unreachable in practice, satisfies the compiler
        }
    }
}