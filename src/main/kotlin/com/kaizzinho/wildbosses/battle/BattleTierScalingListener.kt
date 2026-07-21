package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import net.minecraft.network.chat.Component


object BattleTierScalingListener {

    private const val MAX_LEVEL = 100

    fun register() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            val battle = event.battle

            // Identify the wild actor and confirm it's actually one of our bosses.
            val wildActor = battle.actors.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe
            val bossEntity = wildActor.entity ?: return@subscribe

            val bossInstance = BossRegistry.get(bossEntity.uuid) ?: return@subscribe // not one of our bosses, ignore

            // Identify the player actor.
            val playerActor = battle.actors.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                ?: return@subscribe
            val player = playerActor.entity ?: return@subscribe

            // Read the player's whole party (not just the sent-out Pokémon) for the highest level.
            val party = Cobblemon.storage.getParty(player)
            val highestPlayerLevel = party.mapNotNull { it?.level }.maxOrNull()

            if (highestPlayerLevel == null) {
                WildBosses.logger.warn("[WildBosses] Player ${player.name.string} has an empty party during boss battle - skipping scaling")
                return@subscribe
            }

            val tier = bossInstance.tier
            val rawScaledLevel = highestPlayerLevel + tier.levelBonus
            val cappedLevel = rawScaledLevel.coerceAtMost(MAX_LEVEL)
            val overflow = (rawScaledLevel - MAX_LEVEL).coerceAtLeast(0)

            val bossPokemon = bossEntity.pokemon
            bossPokemon.level = cappedLevel
            bossInstance.currentLevelOverride = cappedLevel

// Reveal the true scaled level to the player right as the battle begins —
// the overworld nameplate stays "??" forever, this is the one moment it matters.
            val chatMessage = Component.literal(
                "${tier.name.lowercase().replaceFirstChar { it.uppercase() }} Boss ${bossPokemon.species.name} " +
                        "scaled to level $cappedLevel (your strongest: Lv.$highestPlayerLevel +${tier.levelBonus})"
            ).withStyle(tier.color)

            player.sendSystemMessage(chatMessage)

            WildBosses.logger.info(
                "[WildBosses] Scaled ${tier.name} boss ${bossPokemon.species.name} to level $cappedLevel " +
                        "(player highest: $highestPlayerLevel, tier bonus: +${tier.levelBonus}, overflow: $overflow)"
            )


            WildBosses.logger.info(
                "[WildBosses] Scaled ${tier.name} boss ${bossPokemon.species.name} to level $cappedLevel " +
                        "(player highest: $highestPlayerLevel, tier bonus: +${tier.levelBonus}, overflow: $overflow)"
            )

            if (overflow > 0) {
                // TODO: stat multiplier for overflow beyond level 100 - needs its own
                // investigation into how Cobblemon exposes stat overrides (IVs/EVs are
                // fixed at spawn, so this is likely a separate multiplier field/system,
                // not a re-roll of IVs). Not yet implemented.
                WildBosses.logger.info("[WildBosses] TODO: overflow of $overflow not yet applied as stat multiplier")
            }
        }
    }
}
