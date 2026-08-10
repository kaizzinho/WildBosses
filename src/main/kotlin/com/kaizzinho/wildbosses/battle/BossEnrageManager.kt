package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.util.UUID

object BossEnrageManager {

    private data class TrackedBattle(
        val battle: PokemonBattle,
        val wildActor: PokemonBattleActor,
        val tier: BossTier,
        var lastProcessedTurn: Int = -1,
        var hasEnragedAtLeastOnce: Boolean = false
    )

    private val trackedBattles = mutableMapOf<UUID, TrackedBattle>()

    fun startTracking(bossEntityUuid: UUID, battle: PokemonBattle, wildActor: PokemonBattleActor, tier: BossTier) {
        if (tier == BossTier.UNCOMMON) {
            if (WildBossesConfig.data.bossAiDebugLogging) {
                WildBosses.logger.info("[BossEnrage][DEBUG] Disabled for ${wildActor.pokemon.effectedPokemon.species.name} tier=UNCOMMON")
            }
            return
        }

        trackedBattles[bossEntityUuid] = TrackedBattle(battle, wildActor, tier)
        WildBosses.logger.info(
            "[BossEnrage] Active for ${wildActor.pokemon.effectedPokemon.species.name} tier=${tier.name} " +
                "interval=${WildBossesConfig.data.enrageIntervalTurns} stages=+${WildBossesConfig.data.enrageStatStages}"
        )
    }

    fun stopTracking(bossEntityUuid: UUID) {
        trackedBattles.remove(bossEntityUuid)
    }

    fun hasEnraged(bossEntityUuid: UUID): Boolean =
        trackedBattles[bossEntityUuid]?.hasEnragedAtLeastOnce == true

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register {
            if (trackedBattles.isEmpty()) return@register

            val interval = WildBossesConfig.data.enrageIntervalTurns

            for ((_, tracked) in trackedBattles) {
                val currentTurn = tracked.battle.turn
                if (currentTurn == tracked.lastProcessedTurn || currentTurn <= 0) continue
                tracked.lastProcessedTurn = currentTurn

                when (currentTurn % interval) {
                    interval - 1 -> sendEnrageWarning(tracked)
                    0 -> triggerEnrage(tracked, currentTurn)
                }
            }
        }
    }

    private fun sendEnrageWarning(tracked: TrackedBattle) {
        val displayName = tracked.wildActor.pokemon.effectedPokemon.species.name
        tracked.battle.broadcastChatMessage(
            Component.translatable("wildbosses.enrage.warning", displayName)
                .withStyle(tracked.tier.color, ChatFormatting.ITALIC)
        )
    }

    private fun triggerEnrage(tracked: TrackedBattle, currentTurn: Int) {
        val slot = "a"
        val pokemonUuid = tracked.wildActor.pokemon.effectedPokemon.uuid
        val identifier = "${tracked.wildActor.showdownId}$slot: $pokemonUuid"
        val displayName = tracked.wildActor.pokemon.effectedPokemon.species.name

        tracked.battle.broadcastChatMessage(
            Component.translatable("wildbosses.enrage.trigger", displayName)
                .withStyle(tracked.tier.color, ChatFormatting.BOLD)
        )

        val stages = WildBossesConfig.data.enrageStatStages
        val rawMessage = buildString {
            append("update\n")
            append("|-boost|$identifier|atk|$stages\n")
            append("|-boost|$identifier|def|$stages\n")
            append("|-boost|$identifier|spa|$stages\n")
            append("|-boost|$identifier|spd|$stages\n")
            append("|-boost|$identifier|spe|$stages")
        }

        try {
            ShowdownInterpreter.interpret(tracked.battle, rawMessage)
            tracked.hasEnragedAtLeastOnce = true
            WildBosses.logger.info(
                "[BossEnrage] Triggered for $displayName tier=${tracked.tier.name} turn=$currentTurn stages=+$stages"
            )
        } catch (e: Exception) {
            WildBosses.logger.warn("[BossEnrage] Failed to apply Enrage to $displayName", e)
        }
    }
}
