package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.kaizzinho.wildbosses.WildBosses

/**
 * Enforces a level 100 ceiling on player-owned Pokémon, independent of the mod's global
 * Cobblemon.config.maxPokemonLevel (raised to 200 elsewhere so wild bosses can scale past
 * 100 in battle - see BattleTierScalingListener). Wild/boss entities have no owner and are
 * therefore untouched by this listener.
 */
object PlayerLevelCapListener {
    private const val PLAYER_MAX_LEVEL = 100

    fun register() {
        // Stops any further experience gain outright once a player's Pokémon is already at the cap.
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe { event ->
            if (event.pokemon.getOwnerPlayer() != null && event.pokemon.level >= PLAYER_MAX_LEVEL) {
                event.cancel()
            }
        }

        // Handles the boundary-crossing case (e.g. a lump-sum candy taking a Pokémon from 99
        // straight to 100 in one gain, overshooting the exact experience threshold for level 100).
        //
        // Pokemon.level's setter only re-syncs the underlying experience value when either (a) the
        // experience-to-level mapping is already mismatched, or (b) the assigned value equals
        // Cobblemon.config.maxPokemonLevel (200 in our setup, not our separate player cap of 100) -
        // confirmed directly from the setter's source. Neither condition is true when we reassign
        // level = 100 while it's already 100, so a same-value reassignment silently no-ops and any
        // experience overshoot from the candy is left sitting in the XP bar.
        //
        // Dropping to 99 first forces a genuine mismatch (the overshot experience still maps to 100,
        // not 99), triggering a real resync down to the exact level-99 threshold. Setting back to 100
        // then triggers a second genuine mismatch/resync, landing exactly on the level-100 threshold
        // with the overshoot cleared. Both steps do real corrective work, not a wasted round-trip.
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_POST.subscribe { event ->
            if (event.pokemon.getOwnerPlayer() != null && event.currentLevel >= PLAYER_MAX_LEVEL) {
                event.pokemon.level = PLAYER_MAX_LEVEL - 1
                event.pokemon.level = PLAYER_MAX_LEVEL
            }
        }
    }
}