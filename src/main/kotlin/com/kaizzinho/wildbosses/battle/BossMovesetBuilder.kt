package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.ai.strongBattleAI.AIUtility
import com.cobblemon.mod.common.pokemon.Pokemon
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import kotlin.math.pow
import kotlin.random.Random

object BossMovesetBuilder {
    private val SELF_DESTRUCT_MOVES = setOf(
        "explosion", "selfdestruct", "mindblown", "mistyexplosion", "chloroblast", "steelbeam"
    )

    private val RECHARGE_MOVES = setOf(
        "hyperbeam", "gigaimpact", "blastburn", "hydrocannon", "frenzyplant",
        "rockwrecker", "roaroftime", "eternabeam"
    )

    private val CHARGE_TURN_MOVES = setOf(
        "solarbeam", "solarblade", "skyattack", "skullbash", "razorwind",
        "dig", "fly", "bounce", "dive", "shadowforce", "phantomforce",
        "freezeshock", "iceburn", "meteorbeam", "electroshot", "skydrop"
    )

    private val LOCKED_IN_MOVES = setOf(
        "uproar", "thrash", "petaldance", "outrage", "rollout", "iceball"
    )

    private val CONDITIONAL_FAIL_MOVES = setOf(
        "dreameater", "lastresort", "synchronoise", "focuspunch",
        "counter", "mirrorcoat", "metalburst", "bide", "hiddenpower", "upperhand"
    )

    val DELAYED_ATTACK_MOVES = setOf(
        "futuresight", "doomdesire"
    )

    val RISKY_SELF_NERF_MOVES = setOf(
        "dracometeor", "leafstorm", "overheat", "psychoboost", "fleurcannon",
        "superpower", "closecombat", "vcreate", "makeitrain", "headlongrush"
    )

    val AGGRESSIVE_SETUP_MOVES = setOf(
        "swordsdance", "dragondance", "bulkup", "coil", "honeclaws",
        "shiftgear", "victorydance", "shellsmash", "noretreat",
        "nastyplot", "calmmind", "quiverdance", "tailglow", "takeheart"
    )

    val RECOVERY_MOVES = setOf(
        "recover", "roost", "slackoff", "softboiled", "milkdrink",
        "healorder", "moonlight", "synthesis", "shoreup"
    )

    private val PHYSICAL_SETUP_ORDER = listOf(
        "shellsmash", "shiftgear", "dragondance", "swordsdance", "victorydance",
        "noretreat", "coil", "bulkup", "honeclaws"
    )

    private val SPECIAL_SETUP_ORDER = listOf(
        "quiverdance", "tailglow", "nastyplot", "shellsmash", "noretreat", "calmmind", "takeheart"
    )

    private val BLACKLISTED_MOVESET_SPECIES = setOf("ditto", "smeargle")

    fun apply(bossPokemon: Pokemon, tier: BossTier) {
        if (bossPokemon.species.showdownId() in BLACKLISTED_MOVESET_SPECIES) {
            WildBosses.logger.info("[BossAI] Keeping ${bossPokemon.species.name}'s natural moveset; custom movesets don't fit this species")
            return
        }

        val attack = Cobblemon.statProvider.getStatForPokemon(bossPokemon, Stats.ATTACK).coerceAtLeast(1)
        val specialAttack = Cobblemon.statProvider.getStatForPokemon(bossPokemon, Stats.SPECIAL_ATTACK).coerceAtLeast(1)
        val favorsPhysical = attack >= specialAttack
        val bossTypes = bossPokemon.types.toSet()
        val severeWeaknesses = severeWeaknesses(bossTypes)
        val minimumAccuracy = minimumAccuracyFor(tier.aiSkill)

        val legalMoves = bossPokemon.species.moves.getAllLegalMoves().distinctBy { it.name }
        val attackCandidates = legalMoves
            .asSequence()
            .filter { it.damageCategory != DamageCategories.STATUS }
            .filter { it.power > 0.0 }
            .filter { it.accuracy <= 0.0 || it.accuracy >= minimumAccuracy }
            .filter { it.name !in SELF_DESTRUCT_MOVES }
            .filter { it.name !in RECHARGE_MOVES }
            .filter { it.name !in CHARGE_TURN_MOVES }
            .filter { it.name !in LOCKED_IN_MOVES }
            .filter { it.name !in CONDITIONAL_FAIL_MOVES }
            .toList()

        if (attackCandidates.isEmpty()) {
            WildBosses.logger.warn("[BossAI] No safe attacks found for ${bossPokemon.species.name}; keeping its natural moveset")
            return
        }

        val instinctMove = chooseInstinctMove(legalMoves, tier.aiSkill, favorsPhysical)
        val attackSlots = if (instinctMove == null) 4 else 3
        val selectedAttacks = chooseAttacks(
            candidates = attackCandidates,
            slots = attackSlots,
            bossTypes = bossTypes,
            attack = attack,
            specialAttack = specialAttack,
            aiSkill = tier.aiSkill,
            tier = tier,
            severeWeaknesses = severeWeaknesses
        )

        if (selectedAttacks.isEmpty()) {
            WildBosses.logger.warn("[BossAI] Couldn't build an attack set for ${bossPokemon.species.name}; keeping its natural moveset")
            return
        }

        val finalMoves = buildList {
            instinctMove?.let { add(it.name) }
            addAll(selectedAttacks.map { it.name })
        }.distinct().take(4)

        val properties = PokemonProperties()
        properties.moves = finalMoves
        properties.apply(bossPokemon)

        val style = offensiveStyle(attack, specialAttack)
        val instinct = instinctMove?.let { " instinct=${it.name}" } ?: ""
        val coverageMove = selectedAttacks.firstOrNull { move ->
            severeWeaknesses.any { threat -> AIUtility.getDamageMultiplier(move.elementalType, threat) > 1.0 }
        }
        val caution = if (coverageMove != null && severeWeaknesses.isNotEmpty()) {
            " caution=${severeWeaknesses.joinToString("/") { it.name }}->${coverageMove.name}"
        } else {
            ""
        }
        WildBosses.logger.info(
            "[BossAI] ${tier.name} ${bossPokemon.species.name} moveset style=$style skill=${tier.aiSkill}$instinct$caution -> ${finalMoves.joinToString(" | ")}"
        )
    }

    private fun chooseInstinctMove(
        legalMoves: List<MoveTemplate>,
        aiSkill: Int,
        favorsPhysical: Boolean
    ): MoveTemplate? {
        if (aiSkill < 4) return null

        val byName = legalMoves.associateBy { it.name }
        val preferredSetup = if (favorsPhysical) PHYSICAL_SETUP_ORDER else SPECIAL_SETUP_ORDER
        preferredSetup.firstNotNullOfOrNull { byName[it] }?.let { return it }

        if (aiSkill >= 5) {
            RECOVERY_MOVES.firstNotNullOfOrNull { byName[it] }?.let { return it }
        }

        return null
    }

    private fun chooseAttacks(
        candidates: List<MoveTemplate>,
        slots: Int,
        bossTypes: Set<com.cobblemon.mod.common.api.types.ElementalType>,
        attack: Int,
        specialAttack: Int,
        aiSkill: Int,
        tier: BossTier,
        severeWeaknesses: List<ElementalType>
    ): List<MoveTemplate> {
        val ranked = candidates.sortedByDescending { movesetScore(it, bossTypes, attack, specialAttack) }
        val selected = mutableListOf<MoveTemplate>()
        val reservedCoverage = chooseThreatCoverageMove(
            ranked, severeWeaknesses, bossTypes, attack, specialAttack, tier
        )?.takeIf { Random.nextDouble() < coverageReserveChance(tier) }
        reservedCoverage?.let(selected::add)

        for (type in bossTypes) {
            ranked.firstOrNull { it.elementalType == type && it !in selected }?.let(selected::add)
            if (selected.size >= slots) return selected
        }

        for (move in ranked) {
            if (selected.size >= slots) break
            if (move in selected) continue
            if (selected.none { it.elementalType == move.elementalType }) selected.add(move)
        }

        for (move in ranked) {
            if (selected.size >= slots) break
            if (move !in selected) selected.add(move)
        }

        if (aiSkill >= 4 && selected.none { it.priority > 0 }) {
            val priorityMove = ranked.firstOrNull { it.priority > 0 && it.power >= 40.0 }
            if (priorityMove != null && priorityMove !in selected) {
                if (selected.size < slots) {
                    selected.add(priorityMove)
                } else {
                    val replaceable = selected
                        .filter { it !== reservedCoverage && it.elementalType !in bossTypes }
                        .minByOrNull { movesetScore(it, bossTypes, attack, specialAttack) }
                        ?: selected
                            .filter { it !== reservedCoverage }
                            .minByOrNull { movesetScore(it, bossTypes, attack, specialAttack) }
                    if (replaceable != null) {
                        val priorityScore = movesetScore(priorityMove, bossTypes, attack, specialAttack)
                        val replaceableScore = movesetScore(replaceable, bossTypes, attack, specialAttack)
                        if (priorityScore >= replaceableScore * 0.55) {
                            selected.remove(replaceable)
                            selected.add(priorityMove)
                        }
                    }
                }
            }
        }

        return selected.sortedByDescending { movesetScore(it, bossTypes, attack, specialAttack) }.take(slots)
    }

    private fun movesetScore(
        move: MoveTemplate,
        bossTypes: Set<com.cobblemon.mod.common.api.types.ElementalType>,
        attack: Int,
        specialAttack: Int
    ): Double {
        val bestAttackStat = maxOf(attack, specialAttack).toDouble()
        val usedAttackStat = when (move.damageCategory) {
            DamageCategories.PHYSICAL -> attack.toDouble()
            DamageCategories.SPECIAL -> specialAttack.toDouble()
            else -> bestAttackStat
        }

        val categoryFit = (usedAttackStat / bestAttackStat).coerceIn(0.55, 1.0)
        val accuracy = if (move.accuracy <= 0.0) 1.0 else (move.accuracy / 100.0).coerceIn(0.0, 1.0)
        val stab = if (move.elementalType in bossTypes) 1.25 else 1.0
        val risky = if (move.name in RISKY_SELF_NERF_MOVES) 0.86 else 1.0
        val delayed = if (move.name in DELAYED_ATTACK_MOVES) 0.60 else 1.0
        val priority = if (move.priority > 0) 1.05 else 1.0

        return move.power * accuracy.pow(0.75) * categoryFit * stab * risky * delayed * priority
    }

    private fun chooseThreatCoverageMove(
        ranked: List<MoveTemplate>,
        severeWeaknesses: List<ElementalType>,
        bossTypes: Set<ElementalType>,
        attack: Int,
        specialAttack: Int,
        tier: BossTier
    ): MoveTemplate? {
        if (severeWeaknesses.isEmpty() || ranked.isEmpty()) return null

        val bestScore = movesetScore(ranked.first(), bossTypes, attack, specialAttack)
        val minimumScore = bestScore * coverageQualityThreshold(tier)

        return ranked
            .asSequence()
            .map { move ->
                val score = movesetScore(move, bossTypes, attack, specialAttack)
                val coverage = severeWeaknesses.maxOfOrNull { threat ->
                    AIUtility.getDamageMultiplier(move.elementalType, threat)
                } ?: 1.0
                Triple(move, score, coverage)
            }
            .filter { (_, score, coverage) -> score >= minimumScore && coverage > 1.0 }
            .maxByOrNull { (_, score, coverage) -> score * coverage }
            ?.first
    }

    private fun severeWeaknesses(bossTypes: Set<ElementalType>): List<ElementalType> =
        ElementalTypes.all().filter { incomingType ->
            bossTypes.fold(1.0) { multiplier, bossType ->
                multiplier * AIUtility.getDamageMultiplier(incomingType, bossType)
            } >= 3.5
        }

    private fun coverageReserveChance(tier: BossTier): Double = when (tier) {
        BossTier.UNCOMMON -> 0.60
        BossTier.RARE -> 0.80
        BossTier.EPIC -> 0.96
        BossTier.LEGENDARY, BossTier.MYTHIC -> 1.0
    }

    private fun coverageQualityThreshold(tier: BossTier): Double = when (tier) {
        BossTier.UNCOMMON -> 0.70
        BossTier.RARE -> 0.60
        BossTier.EPIC -> 0.50
        BossTier.LEGENDARY, BossTier.MYTHIC -> 0.45
    }


    private fun offensiveStyle(attack: Int, specialAttack: Int): String {
        val stronger = maxOf(attack, specialAttack).toDouble().coerceAtLeast(1.0)
        val gap = kotlin.math.abs(attack - specialAttack) / stronger
        return when {
            gap <= 0.10 -> "MIXED"
            attack > specialAttack -> "PHYSICAL"
            else -> "SPECIAL"
        }
    }

    private fun minimumAccuracyFor(aiSkill: Int): Double = when (aiSkill.coerceIn(0, 5)) {
        0, 1, 2 -> 90.0
        3 -> 85.0
        4 -> 80.0
        else -> 70.0
    }
}
