package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.kaizzinho.wildbosses.WildBosses

object PlayerLevelCapListener {
    private const val PLAYER_MAX_LEVEL = 100

    fun register() {
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe { event ->
            if (event.pokemon.getOwnerPlayer() != null && event.pokemon.level >= PLAYER_MAX_LEVEL) {
                event.cancel()
            }
        }

        // Weird hop, but candy XP can leave overflow without it.
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_POST.subscribe { event ->
            if (event.pokemon.getOwnerPlayer() != null && event.currentLevel >= PLAYER_MAX_LEVEL) {
                event.pokemon.level = PLAYER_MAX_LEVEL - 1
                event.pokemon.level = PLAYER_MAX_LEVEL
            }
        }
    }
}
