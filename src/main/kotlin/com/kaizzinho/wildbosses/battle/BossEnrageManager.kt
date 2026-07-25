package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.util.UUID

object BossEnrageManager {

    private const val ENRAGE_INTERVAL = 5 // enrages at turn 5, 10, 15, 20... warns one turn before each

    private data class TrackedBattle(
        val battle: PokemonBattle,
        val wildActor: PokemonBattleActor,
        val tier: BossTier,
        var lastProcessedTurn: Int = -1
    )
    private val trackedBattles = mutableMapOf<UUID, TrackedBattle>()

    fun startTracking(bossEntityUuid: UUID, battle: PokemonBattle, wildActor: PokemonBattleActor, tier: BossTier) {
        trackedBattles[bossEntityUuid] = TrackedBattle(battle, wildActor, tier)
    }

    fun stopTracking(bossEntityUuid: UUID) {
        trackedBattles.remove(bossEntityUuid)
    }

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register {
            if (trackedBattles.isEmpty()) return@register

            for ((_, tracked) in trackedBattles) {
                val currentTurn = tracked.battle.turn
                if (currentTurn == tracked.lastProcessedTurn || currentTurn <= 0) continue
                tracked.lastProcessedTurn = currentTurn

                when (currentTurn % ENRAGE_INTERVAL) {
                    ENRAGE_INTERVAL - 1 -> sendEnrageWarning(tracked)
                    0 -> triggerEnrage(tracked, currentTurn)
                }
            }
        }
    }

    private fun sendEnrageWarning(tracked: TrackedBattle) {
        val displayName = tracked.wildActor.pokemon.effectedPokemon.species.name
        tracked.battle.broadcastChatMessage(
            Component.literal("$displayName is gathering power...")
                .withStyle(tracked.tier.color, ChatFormatting.ITALIC)
        )
    }

    private fun triggerEnrage(tracked: TrackedBattle, currentTurn: Int) {
        val slot = "a"
        val pokemonUuid = tracked.wildActor.pokemon.effectedPokemon.uuid
        val identifier = "${tracked.wildActor.showdownId}$slot: $pokemonUuid"

        val displayName = tracked.wildActor.pokemon.effectedPokemon.species.name

        tracked.battle.broadcastChatMessage(
            Component.literal("$displayName is enraging! It is overflowing with power!")
                .withStyle(tracked.tier.color, ChatFormatting.BOLD)
        )

        val rawMessage = buildString {
            append("update\n")
            append("|-boost|$identifier|atk|1\n")
            append("|-boost|$identifier|def|1\n")
            append("|-boost|$identifier|spa|1\n")
            append("|-boost|$identifier|spd|1\n")
            append("|-boost|$identifier|spe|1")
        }

        try {
            ShowdownInterpreter.interpret(tracked.battle, rawMessage)
            WildBosses.logger.info("[WildBosses] Boss $displayName enraged at turn $currentTurn (identifier: $identifier)")
        } catch (e: Exception) {
            WildBosses.logger.error("[WildBosses] Failed to trigger enrage for $displayName at turn $currentTurn (identifier: $identifier)", e)
        }
    }
}