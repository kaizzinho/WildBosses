package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.kaizzinho.wildbosses.boss.BossRegistry
import net.minecraft.network.chat.Component

object BossBattleRestrictions {
    val PLAYER_BLOCKED_MOVES = setOf(
        "fissure",
        "guillotine",
        "horndrill",
        "sheercold",
        "perishsong",
        "destinybond",
        "endeavor",
        "painsplit",
        "superfang",
        "naturesmadness",
        "ruination",
        "toxic",
        "leechseed",
        "saltcure",
        "curse",
        "haze",
        "clearsmog",
        "topsyturvy",
        "trick",
        "switcheroo"
    )

    val BOSS_AI_FORBIDDEN_MOVES = setOf(
        "belch"
    )

    @JvmStatic
    fun filterActionResponses(
        actor: BattleActor,
        responses: List<out ShowdownActionResponse>
    ): List<ShowdownActionResponse> {
        if (actor.type != ActorType.PLAYER) return responses.toList()
        if (!hasActiveWildBoss(actor)) return responses.toList()

        val requestMovesets = actor.request?.active ?: return responses.toList()

        return responses.mapIndexed { index, response ->
            val moveResponse = response as? MoveActionResponse ?: return@mapIndexed response
            val activePokemon = actor.activePokemon.getOrNull(index) ?: return@mapIndexed response
            val moveset = requestMovesets.getOrNull(index) ?: return@mapIndexed response
            val inBattleMove = moveset.moves.firstOrNull { it.id == moveResponse.moveName }
                ?: return@mapIndexed response
            val moveId = normalizeMoveId(inBattleMove.id)

            if (!isBlockedPlayerMove(moveId, activePokemon.battlePokemon)) {
                return@mapIndexed response
            }

            consumePp(activePokemon.battlePokemon, moveId, inBattleMove.pp)
            inBattleMove.pp = (inBattleMove.pp - 1).coerceAtLeast(0)

            val moveName = Moves.getByName(moveId)?.displayName
                ?: Component.literal(inBattleMove.move.ifBlank { moveId })
            actor.battle.broadcastChatMessage(
                Component.translatable("wildbosses.message.move_blocked", moveName)
            )

            PassActionResponse
        }
    }

    private fun hasActiveWildBoss(actor: BattleActor): Boolean {
        return actor.battle.actors
            .filterIsInstance<PokemonBattleActor>()
            .any { battleActor ->
                if (battleActor.type != ActorType.WILD) return@any false
                val entity = battleActor.entity
                if (entity != null && BossRegistry.get(entity.uuid) != null) return@any true
                BossRegistry.getByPokemonUuid(battleActor.pokemon.effectedPokemon.uuid) != null
            }
    }

    private fun isBlockedPlayerMove(moveId: String, battlePokemon: BattlePokemon?): Boolean {
        if (moveId !in PLAYER_BLOCKED_MOVES) return false
        if (moveId != "curse") return true

        val pokemon = battlePokemon?.effectedPokemon ?: return false
        return ElementalTypes.GHOST in pokemon.types
    }

    private fun consumePp(battlePokemon: BattlePokemon?, moveId: String, showdownPp: Int) {
        if (showdownPp <= 0 || battlePokemon == null) return

        decrementPokemonMovePp(battlePokemon.effectedPokemon.moveSet.getMoves(), moveId)
        if (battlePokemon.originalPokemon !== battlePokemon.effectedPokemon) {
            decrementPokemonMovePp(battlePokemon.originalPokemon.moveSet.getMoves(), moveId)
        }
    }

    private fun decrementPokemonMovePp(
        moves: List<com.cobblemon.mod.common.api.moves.Move>,
        moveId: String
    ) {
        val move = moves.firstOrNull { normalizeMoveId(it.name) == moveId } ?: return
        move.currentPp = (move.currentPp - 1).coerceAtLeast(0)
    }

    private fun normalizeMoveId(value: String): String {
        return value.lowercase().filter { it.isLetterOrDigit() }
    }
}
