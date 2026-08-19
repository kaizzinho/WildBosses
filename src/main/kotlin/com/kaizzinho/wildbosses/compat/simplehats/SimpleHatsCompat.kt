package com.kaizzinho.wildbosses.compat.simplehats

import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.lang.reflect.Method

object SimpleHatsCompat {

    private const val MOD_ID = "simplehats"

    private data class TierHatRule(
        val rarityWeights: Map<HatRarity, Int>
    )

    private enum class HatRarity {
        UNCOMMON,
        RARE,
        EPIC
    }

    private data class HatCandidate(
        val item: Item,
        val rarity: HatRarity,
        val weight: Int
    )

    private data class Bridge(
        val registry: Any,
        val getHatList: Method,
        val getHatEntry: Method,
        val getHatRarity: Method,
        val getHatWeight: Method,
        val getHatSeason: Method
    )

    private val rules = mapOf(
        BossTier.UNCOMMON to TierHatRule(
            mapOf(HatRarity.UNCOMMON to 85, HatRarity.RARE to 14, HatRarity.EPIC to 1)
        ),
        BossTier.RARE to TierHatRule(
            mapOf(HatRarity.UNCOMMON to 70, HatRarity.RARE to 26, HatRarity.EPIC to 4)
        ),
        BossTier.EPIC to TierHatRule(
            mapOf(HatRarity.UNCOMMON to 50, HatRarity.RARE to 40, HatRarity.EPIC to 10)
        ),
        BossTier.LEGENDARY to TierHatRule(
            mapOf(HatRarity.UNCOMMON to 25, HatRarity.RARE to 55, HatRarity.EPIC to 20)
        ),
        BossTier.MYTHIC to TierHatRule(
            mapOf(HatRarity.UNCOMMON to 10, HatRarity.RARE to 45, HatRarity.EPIC to 45)
        )
    )

    private val modLoaded by lazy {
        FabricLoader.getInstance().isModLoaded(MOD_ID)
    }

    private val bridge by lazy {
        if (!modLoaded) null else createBridge()
    }

    private val candidates by lazy {
        bridge?.let(::loadCandidates).orEmpty()
    }

    fun rollHat(tier: BossTier, random: RandomSource): ItemStack? {
        if (!modLoaded) return null

        val rule = rules[tier] ?: return null
        val pool = candidates
        if (pool.isEmpty()) return null
        val rarity = rollRarity(rule, pool, random) ?: return null
        val rarityPool = pool.filter { it.rarity == rarity }
        val picked = weightedPick(rarityPool, random) ?: return null
        return ItemStack(picked.item)
    }

    private fun createBridge(): Bridge? {
        return try {
            val commonClass = Class.forName("fonnymunkey.simplehats.SimpleHatsCommon")
            val registry = commonClass.getField("MOD_REGISTRY").get(null)

            val getHatList = registry.javaClass.getMethod("getHatList")
            val hatList = getHatList.invoke(registry) as? List<*> ?: return null
            val firstHat = hatList.firstOrNull() ?: return null
            val getHatEntry = firstHat.javaClass.getMethod("getHatEntry")
            val firstEntry = getHatEntry.invoke(firstHat) ?: return null
            val entryClass = firstEntry.javaClass

            Bridge(
                registry = registry,
                getHatList = getHatList,
                getHatEntry = getHatEntry,
                getHatRarity = entryClass.getMethod("getHatRarity"),
                getHatWeight = entryClass.getMethod("getHatWeight"),
                getHatSeason = entryClass.getMethod("getHatSeason")
            )
        } catch (e: Exception) {
            WildBosses.logger.warn("[WildBosses] Simple Hats integration could not initialize", e)
            null
        }
    }

    private fun loadCandidates(bridge: Bridge): List<HatCandidate> {
        return try {
            val hats = bridge.getHatList.invoke(bridge.registry) as? List<*> ?: return emptyList()
            val loaded = hats.mapNotNull { hat ->
                val item = hat as? Item ?: return@mapNotNull null
                val entry = bridge.getHatEntry.invoke(hat) ?: return@mapNotNull null
                val weight = (bridge.getHatWeight.invoke(entry) as? Number)?.toInt() ?: return@mapNotNull null
                if (weight <= 0) return@mapNotNull null

                val season = enumName(bridge.getHatSeason.invoke(entry))
                if (season != "NONE") return@mapNotNull null

                val rarity = runCatching {
                    HatRarity.valueOf(enumName(bridge.getHatRarity.invoke(entry)))
                }.getOrNull() ?: return@mapNotNull null

                HatCandidate(item, rarity, weight)
            }

            if (loaded.isNotEmpty()) {
                val counts = HatRarity.entries.joinToString(" ") { rarity ->
                    "${rarity.name.lowercase()}=${loaded.count { it.rarity == rarity }}"
                }
                WildBosses.logger.info("[WildBosses] Simple Hats integration ready ${loaded.size} hats $counts")
            } else {
                WildBosses.logger.warn("[WildBosses] Simple Hats integration found no eligible uncommon rare or epic non seasonal hats")
            }
            loaded
        } catch (e: Exception) {
            WildBosses.logger.warn("[WildBosses] Simple Hats reward pool could not be read", e)
            emptyList()
        }
    }

    private fun rollRarity(
        rule: TierHatRule,
        pool: List<HatCandidate>,
        random: RandomSource
    ): HatRarity? {
        val available = pool.asSequence().map { it.rarity }.toSet()
        val weighted = rule.rarityWeights.filter { (rarity, weight) -> rarity in available && weight > 0 }
        val total = weighted.values.sum()
        if (total <= 0) return null

        var roll = random.nextInt(total)
        for ((rarity, weight) in weighted) {
            if (roll < weight) return rarity
            roll -= weight
        }
        return null
    }

    private fun weightedPick(pool: List<HatCandidate>, random: RandomSource): HatCandidate? {
        val total = pool.sumOf { it.weight }
        if (total <= 0) return null

        var roll = random.nextInt(total)
        for (candidate in pool) {
            if (roll < candidate.weight) return candidate
            roll -= candidate.weight
        }
        return null
    }

    private fun enumName(value: Any?): String = when (value) {
        is Enum<*> -> value.name
        null -> ""
        else -> value.toString().uppercase()
    }
}
