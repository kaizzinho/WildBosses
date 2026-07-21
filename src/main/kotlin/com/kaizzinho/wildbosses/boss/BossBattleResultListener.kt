package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity


object BossBattleResultListener {

    fun register() {
        CobblemonEvents.BATTLE_FLED.subscribe { event ->
            val wildActor = event.battle.actors.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe
            val bossEntity = wildActor.entity ?: return@subscribe

            if (BossRegistry.isBoss(bossEntity.uuid)) {
                resetBossHp(bossEntity)
                WildBosses.logger.info("[WildBosses] Boss ${bossEntity.pokemon.species.name} HP reset after player fled")
            }
        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val wildWinner = event.winners.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe // wild didn't win - player won or it was a different matchup, ignore
            val bossEntity = wildWinner.entity ?: return@subscribe

            if (BossRegistry.isBoss(bossEntity.uuid)) {
                resetBossHp(bossEntity)
                WildBosses.logger.info("[WildBosses] Boss ${bossEntity.pokemon.species.name} HP reset after defeating the player")
            }
        }
    }

    private fun resetBossHp(bossEntity: PokemonEntity) {
        bossEntity.pokemon.currentHealth = bossEntity.pokemon.maxHealth
    }
}