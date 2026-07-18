package com.kaizzinho.wildbosses.boss

enum class BossTier(val levelBonus: Int) {
    UNCOMMON(15),
    RARE(levelBonus = 25),
    EPIC(35),
    LEGENDARY(40),
    MYTHIC(50);

    companion object {
        fun rollRandomTier(): BossTier = entries.random()
    }
}