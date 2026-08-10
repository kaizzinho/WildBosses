package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.MoveActionResponse
import com.cobblemon.mod.common.battles.ShowdownActionResponse
import com.cobblemon.mod.common.battles.ShowdownMoveset
import java.util.UUID

// Pick first, tag Mega after.

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
