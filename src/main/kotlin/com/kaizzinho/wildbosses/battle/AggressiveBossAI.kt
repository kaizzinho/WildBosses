package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.InBattleMove
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.ShowdownMoveset
import com.cobblemon.mod.common.battles.ai.strongBattleAI.AIUtility
import com.cobblemon.mod.common.pokemon.Pokemon
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

class AggressiveBossAI(
    private val delegate: BattleAI,
    private val tier: BossTier,
    private val bossEntityUuid: UUID,
    private val bossName: String
) : BattleAI by delegate {
    private var setupUses = 0
    private var recoveryUses = 0
    private var delayedAttackLastUseTurn: Int? = null

    private val typeImmuneAbilities = mapOf(
        "lightningrod" to ElementalTypes.ELECTRIC,
        "flashfire" to ElementalTypes.FIRE,
        "levitate" to ElementalTypes.GROUND,
        "sapsipper" to ElementalTypes.GRASS,
        "motordrive" to ElementalTypes.ELECTRIC,
        "stormdrain" to ElementalTypes.WATER,
        "voltabsorb" to ElementalTypes.ELECTRIC,
        "waterabsorb" to ElementalTypes.WATER,
        "dryskin" to ElementalTypes.WATER,
        "wellbakedbody" to ElementalTypes.FIRE,
        "eartheater" to ElementalTypes.GROUND
    )

    override fun choose(
        activeBattlePokemon: ActiveBattlePokemon,
        battle: PokemonBattle,
        aiSide: BattleSide,
        moveset: ShowdownMoveset?,
        forceSwitch: Boolean
    ): ShowdownActionResponse {
        if (forceSwitch || moveset == null || activeBattlePokemon.isGone()) {
            return delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)
        }

        moveset.moves.firstOrNull { it.mustBeUsed() }?.let { forced ->
            debug(battle, "forced move=${forced.id}")
            return chooseMove(forced, activeBattlePokemon)
        }

        val attacker = activeBattlePokemon.battlePokemon?.effectedPokemon
            ?: return delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)
        val target = battle.activePokemon.firstOrNull { !it.isAllied(activeBattlePokemon) && it.isAlive() }
            ?: return delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)
        val defender = target.battlePokemon?.effectedPokemon
            ?: return delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)

        val usableMoves = moveset.moves.filter { it.canBeUsed() }
        val delayedAttackPending = isDelayedAttackPending(battle)
        val blockedDelayedMoves = if (delayedAttackPending) {
            usableMoves.filter { it.id in BossMovesetBuilder.DELAYED_ATTACK_MOVES }
        } else {
            emptyList()
        }
        if (blockedDelayedMoves.isNotEmpty()) {
            debug(
                battle,
                "blocked=${blockedDelayedMoves.joinToString { it.id }} reason=pending_delayed_attack lastTurn=$delayedAttackLastUseTurn"
            )
        }

        val attackChoices = usableMoves.mapNotNull { inBattleMove ->
            if (delayedAttackPending && inBattleMove.id in BossMovesetBuilder.DELAYED_ATTACK_MOVES) {
                return@mapNotNull null
            }
            val template = Moves.getByName(inBattleMove.id) ?: return@mapNotNull null
            if (template.damageCategory == DamageCategories.STATUS || template.power <= 0.0) return@mapNotNull null
            AttackChoice(inBattleMove, scoreAttack(template, attacker, defender))
        }.filter { it.score.typeMultiplier > 0.0 }

        if (attackChoices.isEmpty()) {
            debug(battle, "no usable attacks; falling back to StrongBattleAI")
            return delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)
        }

        val ranked = attackChoices.sortedWith(
            compareByDescending<AttackChoice> { if (it.score.koOnHit) 1 else 0 }
                .thenByDescending { it.score.score }
        )
        val bestAttack = ranked.first()
        val bossHp = hpRatio(attacker)
        val targetHp = hpRatio(defender)
        val incomingThreat = incomingChargeThreat(battle, target, defender, attacker)
        val respectsThreat = incomingThreat?.let { shouldRespectThreat() } ?: false

        if (incomingThreat != null) {
            val awareness = if (respectsThreat) "react" else "miss"
            debug(
                battle,
                "incoming=${incomingThreat.move.name} x${format(incomingThreat.multiplier)} awareness=$awareness"
            )
        }

        if (respectsThreat && incomingThreat != null && incomingThreat.multiplier >= 3.5) {
            val chosen = ranked.firstOrNull { !it.score.delayed } ?: ranked.first()
            if (chosen.move.id in BossMovesetBuilder.DELAYED_ATTACK_MOVES) {
                delayedAttackLastUseTurn = battle.turn
            }
            debug(
                battle,
                "choice=${chosen.move.id} reason=charge_danger incoming=${incomingThreat.move.name} x${format(incomingThreat.multiplier)}"
            )
            debugDecision(battle, attacker, defender, ranked, chosen)
            return chooseMove(chosen.move, activeBattlePokemon, target)
        }

        chooseRecovery(usableMoves, bestAttack, bossHp, battle)?.let { return it }
        if (!respectsThreat) {
            chooseSetup(usableMoves, bestAttack, bossHp, targetHp, battle)?.let { return it }
        } else if (incomingThreat != null) {
            debug(battle, "setup_blocked reason=charge_caution incoming=${incomingThreat.move.name}")
        }

        val chosen = chooseBySkill(ranked, cautious = respectsThreat)
        if (chosen.move.id in BossMovesetBuilder.DELAYED_ATTACK_MOVES) {
            delayedAttackLastUseTurn = battle.turn
        }
        debugDecision(battle, attacker, defender, ranked, chosen)
        return chooseMove(chosen.move, activeBattlePokemon, target)
    }

    private fun incomingChargeThreat(
        battle: PokemonBattle,
        target: ActiveBattlePokemon,
        chargingPokemon: Pokemon,
        bossPokemon: Pokemon
    ): IncomingThreat? {
        val targetUuid = target.battlePokemon?.uuid ?: return null
        val prepare = previousTurnMessages(battle)
            .asReversed()
            .firstNotNullOfOrNull { raw ->
                runCatching { BattleMessage(raw) }.getOrNull()?.takeIf { message ->
                    message.id == "-prepare" && message.battlePokemon(0, battle)?.uuid == targetUuid
                }
            } ?: return null

        val move = prepare.moveAt(1) ?: return null
        if (move.damageCategory == DamageCategories.STATUS || move.power <= 0.0) return null

        val moveType = move.getEffectiveElementalType(chargingPokemon)
        val baseMultiplier = bossPokemon.types.fold(1.0) { multiplier, type ->
            multiplier * AIUtility.getDamageMultiplier(moveType, type)
        }
        val abilityImmunity = typeImmuneAbilities[bossPokemon.ability.name] == moveType ||
            (bossPokemon.ability.name == "wonderguard" && baseMultiplier <= 1.0)
        val multiplier = if (abilityImmunity) 0.0 else baseMultiplier
        if (multiplier < 2.0) return null

        return IncomingThreat(move, multiplier)
    }

    private fun previousTurnMessages(battle: PokemonBattle): List<String> {
        val messages = battle.showdownMessages
        val currentTurnIndex = messages.indexOfLast { showdownTurn(it) == battle.turn }
        if (currentTurnIndex <= 0) return emptyList()

        var previousTurnIndex = -1
        for (index in currentTurnIndex - 1 downTo 0) {
            if (showdownTurn(messages[index]) == battle.turn - 1) {
                previousTurnIndex = index
                break
            }
        }
        if (previousTurnIndex < 0) return emptyList()

        return messages.subList(previousTurnIndex + 1, currentTurnIndex)
    }

    private fun showdownTurn(message: String): Int? {
        if (!message.startsWith("|turn|")) return null
        return message.substringAfter("|turn|").substringBefore('|').toIntOrNull()
    }

    private fun shouldRespectThreat(): Boolean {
        val chance = when (tier) {
            BossTier.UNCOMMON -> 0.75
            BossTier.RARE -> 0.88
            BossTier.EPIC -> 0.97
            BossTier.LEGENDARY, BossTier.MYTHIC -> 1.0
        }
        return chance >= 1.0 || Random.nextDouble() < chance
    }

    private fun chooseSetup(
        usableMoves: List<InBattleMove>,
        bestAttack: AttackChoice,
        bossHp: Double,
        targetHp: Double,
        battle: PokemonBattle
    ): ShowdownActionResponse? {
        if (tier.aiSkill < 4 || setupUses > 0) return null
        if (battle.turn > 3 || bossHp < 0.70 || targetHp < 0.55 || bestAttack.score.koOnHit) return null

        val setup = usableMoves.firstOrNull { it.id in BossMovesetBuilder.AGGRESSIVE_SETUP_MOVES } ?: return null
        val chance = when (tier.aiSkill) {
            4 -> 0.20
            else -> 0.30
        }
        if (Random.nextDouble() >= chance) return null

        setupUses++
        debug(battle, "choice=${setup.id} reason=aggressive_setup hp=${percent(bossHp)} targetHp=${percent(targetHp)}")
        return MoveActionResponse(setup.id)
    }

    private fun chooseRecovery(
        usableMoves: List<InBattleMove>,
        bestAttack: AttackChoice,
        bossHp: Double,
        battle: PokemonBattle
    ): ShowdownActionResponse? {
        if (tier.aiSkill < 5 || recoveryUses > 0 || bossHp > 0.25 || bestAttack.score.koOnHit) return null

        val recovery = usableMoves.firstOrNull { it.id in BossMovesetBuilder.RECOVERY_MOVES } ?: return null
        if (Random.nextDouble() >= 0.65) return null

        recoveryUses++
        debug(battle, "choice=${recovery.id} reason=desperate_recovery hp=${percent(bossHp)} no_immediate_ko=true")
        return MoveActionResponse(recovery.id)
    }

    private fun chooseBySkill(ranked: List<AttackChoice>, cautious: Boolean = false): AttackChoice {
        if (ranked.size == 1) return ranked.first()

        val koMoves = ranked.filter { it.score.koOnHit }
        if (koMoves.isNotEmpty() && tier.aiSkill >= 3) return koMoves.maxBy { it.score.score }

        val skill = tier.aiSkill.coerceIn(0, 5)
        val bestMoveChance = if (cautious) {
            when (tier) {
                BossTier.UNCOMMON -> 0.88
                BossTier.RARE -> 0.94
                BossTier.EPIC -> 0.99
                BossTier.LEGENDARY, BossTier.MYTHIC -> 1.0
            }
        } else {
            when (skill) {
                0 -> 0.55
                1 -> 0.65
                2 -> 0.75
                3 -> 0.85
                4 -> 0.93
                else -> 0.98
            }
        }
        if (Random.nextDouble() < bestMoveChance) return ranked.first()

        val minimumScoreRatio = when (skill) {
            0 -> 0.35
            1 -> 0.45
            2 -> 0.50
            3 -> 0.65
            4 -> 0.80
            else -> 0.90
        }
        val bestScore = ranked.first().score.score
        val reasonableAlternatives = ranked.drop(1)
            .filter { it.score.score >= bestScore * minimumScoreRatio }
            .take(2)

        return reasonableAlternatives.randomOrNull() ?: ranked.first()
    }

    private fun scoreAttack(move: MoveTemplate, attacker: Pokemon, defender: Pokemon): AttackScore {
        val moveType = move.getEffectiveElementalType(attacker)
        val baseTypeMultiplier = defender.types.fold(1.0) { multiplier, type ->
            multiplier * AIUtility.getDamageMultiplier(moveType, type)
        }
        val abilityImmunity = typeImmuneAbilities[defender.ability.name] == moveType ||
            (defender.ability.name == "wonderguard" && baseTypeMultiplier <= 1.0)
        val typeMultiplier = if (abilityImmunity) 0.0 else baseTypeMultiplier

        val attackStat = when (move.damageCategory) {
            DamageCategories.PHYSICAL -> Cobblemon.statProvider.getStatForPokemon(attacker, Stats.ATTACK)
            DamageCategories.SPECIAL -> Cobblemon.statProvider.getStatForPokemon(attacker, Stats.SPECIAL_ATTACK)
            else -> 1
        }.coerceAtLeast(1)

        val defenceStat = when (move.damageCategory) {
            DamageCategories.PHYSICAL -> Cobblemon.statProvider.getStatForPokemon(defender, Stats.DEFENCE)
            DamageCategories.SPECIAL -> Cobblemon.statProvider.getStatForPokemon(defender, Stats.SPECIAL_DEFENCE)
            else -> 1
        }.coerceAtLeast(1)

        val stab = when {
            moveType !in attacker.types -> 1.0
            attacker.ability.name == "adaptability" -> 2.0
            else -> 1.5
        }
        val hits = expectedHits(move)
        val rawDamage = (((((2.0 * attacker.level) / 5.0) + 2.0) * move.power * attackStat / defenceStat) / 50.0 + 2.0) *
            stab * typeMultiplier * hits
        val accuracy = if (move.accuracy <= 0.0) 1.0 else (move.accuracy / 100.0).coerceIn(0.0, 1.0)
        val expectedDamage = rawDamage * accuracy
        val delayed = move.name in BossMovesetBuilder.DELAYED_ATTACK_MOVES
        val koOnHit = !delayed && rawDamage * 0.85 >= defender.currentHealth.coerceAtLeast(1)

        var score = expectedDamage
        if (koOnHit) score += 10_000.0
        if (move.priority > 0 && koOnHit) score += 500.0 * move.priority
        if (move.priority > 0 && !koOnHit && defender.currentHealth <= rawDamage * 1.15) score += 30.0 * move.priority
        if (move.name in BossMovesetBuilder.RISKY_SELF_NERF_MOVES && !koOnHit) score *= 0.82
        if (delayed) score *= 0.55

        return AttackScore(
            score = score,
            estimatedDamage = rawDamage,
            typeMultiplier = typeMultiplier,
            koOnHit = koOnHit,
            accuracy = accuracy,
            priority = move.priority,
            risky = move.name in BossMovesetBuilder.RISKY_SELF_NERF_MOVES,
            delayed = delayed
        )
    }

    private fun isDelayedAttackPending(battle: PokemonBattle): Boolean {
        val lastUseTurn = delayedAttackLastUseTurn ?: return false
        return battle.turn - lastUseTurn < 3
    }

    private fun expectedHits(move: MoveTemplate): Double {
        if (move.name == "triplekick" || move.name == "tripleaxel") return 2.4
        if (move.name == "populationbomb") return 7.0
        val range = AIUtility.multiHitMoves[move.name] ?: return 1.0
        return if (range.first == range.second) range.first.toDouble() else 3.0
    }

    private fun chooseMove(
        move: InBattleMove,
        activeBattlePokemon: ActiveBattlePokemon,
        target: ActiveBattlePokemon? = null
    ): MoveActionResponse {
        if (move.mustBeUsed()) return MoveActionResponse(move.id)

        val targets = move.target.targetList(activeBattlePokemon)
        if (targets == null) return MoveActionResponse(move.id)

        val chosenTarget = target?.takeIf { it in targets }
            ?: targets.filterIsInstance<ActiveBattlePokemon>().firstOrNull { !it.isAllied(activeBattlePokemon) }
            ?: targets.filterIsInstance<ActiveBattlePokemon>().firstOrNull()

        return if (chosenTarget == null) MoveActionResponse(move.id) else MoveActionResponse(move.id, chosenTarget.getPNX())
    }

    private fun debugDecision(
        battle: PokemonBattle,
        attacker: Pokemon,
        defender: Pokemon,
        ranked: List<AttackChoice>,
        chosen: AttackChoice
    ) {
        if (!WildBossesConfig.data.bossAiDebugLogging) return

        val candidates = ranked.joinToString(" | ") { choice ->
            val s = choice.score
            "${choice.move.id}:score=${format(s.score)},dmg~${s.estimatedDamage.roundToInt()},x${format(s.typeMultiplier)},acc=${percent(s.accuracy)},prio=${s.priority},ko=${s.koOnHit},risky=${s.risky},delayed=${s.delayed}"
        }
        val best = ranked.first()
        val variance = if (chosen !== best) {
            val ratio = if (best.score.score <= 0.0) 1.0 else chosen.score.score / best.score.score
            " variance=skill choiceRatio=${percent(ratio)}"
        } else {
            ""
        }
        WildBosses.logger.info(
            "[BossAI][DEBUG] uuid=$bossEntityUuid turn=${battle.turn} tier=${tier.name} boss=$bossName " +
                "hp=${percent(hpRatio(attacker))} target=${defender.species.name} targetHp=${percent(hpRatio(defender))} " +
                "choice=${chosen.move.id}$variance candidates=[$candidates]"
        )
    }

    private fun debug(battle: PokemonBattle, message: String) {
        if (!WildBossesConfig.data.bossAiDebugLogging) return
        WildBosses.logger.info("[BossAI][DEBUG] uuid=$bossEntityUuid turn=${battle.turn} tier=${tier.name} boss=$bossName $message")
    }

    private fun hpRatio(pokemon: Pokemon): Double =
        pokemon.currentHealth.toDouble() / pokemon.maxHealth.coerceAtLeast(1).toDouble()

    private fun percent(value: Double): String = "${(value.coerceIn(0.0, 1.0) * 100.0).roundToInt()}%"

    private fun format(value: Double): String = "%.2f".format(java.util.Locale.ROOT, value)

    private data class IncomingThreat(
        val move: MoveTemplate,
        val multiplier: Double
    )

    private data class AttackChoice(
        val move: InBattleMove,
        val score: AttackScore
    )

    private data class AttackScore(
        val score: Double,
        val estimatedDamage: Double,
        val typeMultiplier: Double,
        val koOnHit: Boolean,
        val accuracy: Double,
        val priority: Int,
        val risky: Boolean,
        val delayed: Boolean
    )
}
