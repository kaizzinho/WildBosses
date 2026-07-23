package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.ai.StrongBattleAI
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel


object BattleTierScalingListener {

    private const val MAX_LEVEL = 200

    private val battleAIField = AIBattleActor::class.java.getDeclaredField("battleAI").apply { isAccessible = true }

    fun register() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            val battle = event.battle

            val wildActor = battle.actors.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe
            val bossEntity = wildActor.entity ?: return@subscribe

            val bossInstance = BossRegistry.get(bossEntity.uuid) ?: return@subscribe

            // Stash everything BossBattleResultListener will need to award loot after this
            // entity is gone (it gets removed from the world once it faints).
            bossInstance.lastKnownPos = bossEntity.position()
            bossInstance.lastKnownLevel = bossEntity.level() as? ServerLevel
            bossInstance.speciesName = bossEntity.pokemon.species.name

            try {
                battleAIField.set(wildActor, StrongBattleAI(bossInstance.tier.aiSkill))
            } catch (e: Exception) {
                WildBosses.logger.error("[WildBosses] Failed to apply StrongBattleAI to boss ${bossEntity.pokemon.species.name} - falling back to default AI", e)
            }


            val playerActor = battle.actors.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                ?: return@subscribe
            val player = playerActor.entity ?: return@subscribe

            val party = Cobblemon.storage.getParty(player)
            val highestPlayerLevel = party.mapNotNull { it?.level }.maxOrNull()

            if (highestPlayerLevel == null) {
                WildBosses.logger.warn("[WildBosses] Player ${player.name.string} has an empty party during boss battle - skipping scaling")
                return@subscribe
            }

            val tier = bossInstance.tier
            val scaledLevel = (highestPlayerLevel + tier.levelBonus).coerceAtMost(MAX_LEVEL)

            val bossPokemon = bossEntity.pokemon
            bossPokemon.level = scaledLevel
            bossInstance.currentLevelOverride = scaledLevel

            val chatMessage = Component.literal(
                "${tier.name.lowercase().replaceFirstChar { it.uppercase() }} Boss ${bossPokemon.species.name} " +
                        "scaled to level $scaledLevel (your strongest: Lv.$highestPlayerLevel +${tier.levelBonus})"
            ).withStyle(tier.color)

            player.sendSystemMessage(chatMessage)

            WildBosses.logger.info(
                "[WildBosses] Scaled ${tier.name} boss ${bossPokemon.species.name} to level $scaledLevel " +
                        "(player highest: $highestPlayerLevel, tier bonus: +${tier.levelBonus}, AI skill: ${tier.aiSkill})"
            )
        }
    }
}