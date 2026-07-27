package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.ShowdownMoveset
import java.util.UUID

/**
 * Wraps a real BattleAI (StrongBattleAI) and delegates everything to it unchanged, EXCEPT:
 * if this boss has a pending Mega Evolution trigger queued (see MegaEvolutionManager) and the
 * delegate's own, normally-chosen move comes back as a MoveActionResponse, stamps the "mega"
 * gimmick flag onto that same response object before it's submitted. This makes the boss
 * Mega Evolve using whatever attacking move the real AI already picked on its own, rather than
 * us overriding move selection - StrongBattleAI itself has zero gimmick awareness at all.
 */
class MegaTriggeringAI(
    private val delegate: BattleAI,
    private val bossEntityUuid: UUID
) : BattleAI by delegate {

    override fun choose(
        activeBattlePokemon: ActiveBattlePokemon,
        battle: PokemonBattle,
        aiSide: BattleSide,
        moveset: ShowdownMoveset?,
        forceSwitch: Boolean
    ): ShowdownActionResponse {
        val response = delegate.choose(activeBattlePokemon, battle, aiSide, moveset, forceSwitch)

        if (!forceSwitch && response is MoveActionResponse && MegaEvolutionManager.consumePendingMegaTrigger(bossEntityUuid)) {
            response.gimmickID = "mega"
        }

        return response
    }
}