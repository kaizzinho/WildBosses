package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossInstance
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets


object BossBattleResultListener {

    // Awarding loot right at BATTLE_VICTORY can crash Cobblemon's own Showdown JS bridge -
    // BattleRegistry ticks ALL active battles in one parallel pass (ForkJoinTask-based), and
    // our loot logic touching LootParams/registries synchronously inside that dispatch chain
    // caused an uncaught exception to corrupt the battle's teardown. Delaying by a handful of
    // real ticks moves this fully outside Cobblemon's own event-dispatch call stack.
    private const val LOOT_AWARD_DELAY_TICKS = 5

    private data class PendingLootAward(val bossInstance: BossInstance, val readyAtTick: Long)
    private val pendingAwards = mutableListOf<PendingLootAward>()

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
            if (wildWinner != null) {
                val bossEntity = wildWinner.entity ?: return@subscribe
                if (BossRegistry.isBoss(bossEntity.uuid)) {
                    resetBossHp(bossEntity)
                    WildBosses.logger.info("[WildBosses] Boss ${bossEntity.pokemon.species.name} HP reset after defeating the player")
                }
                return@subscribe
            }

            val playerWinner = event.winners.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                ?: return@subscribe

            val wildLoser = event.losers.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe

            val pokemonUuid = wildLoser.pokemon.effectedPokemon.uuid
            val bossInstance = BossRegistry.getByPokemonUuid(pokemonUuid) ?: run {
                WildBosses.logger.warn("[WildBosses] Player-victory branch: no BossInstance found for pokemonUuid=$pokemonUuid, aborting")
                return@subscribe
            }

            val currentTick = bossInstance.lastKnownLevel?.server?.overworld()?.gameTime ?: 0L
            pendingAwards.add(PendingLootAward(bossInstance, currentTick + LOOT_AWARD_DELAY_TICKS))
            WildBosses.logger.info("[WildBosses] Queued loot award for ${bossInstance.speciesName} (${bossInstance.tier.name}), ready at tick ${currentTick + LOOT_AWARD_DELAY_TICKS}")
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (pendingAwards.isEmpty()) return@register

            val currentTick = server.overworld().gameTime
            val ready = pendingAwards.filter { it.readyAtTick <= currentTick }
            if (ready.isEmpty()) return@register

            pendingAwards.removeAll(ready)
            ready.forEach { awardBossLoot(it.bossInstance) }
        }
    }

    private fun resetBossHp(bossEntity: PokemonEntity) {
        bossEntity.pokemon.currentHealth = bossEntity.pokemon.maxHealth
    }

    private fun awardBossLoot(bossInstance: BossInstance) {
        val level = bossInstance.lastKnownLevel ?: run {
            WildBosses.logger.warn("[WildBosses] awardBossLoot: no lastKnownLevel stashed for this boss, aborting")
            return
        }
        val pos = bossInstance.lastKnownPos ?: run {
            WildBosses.logger.warn("[WildBosses] awardBossLoot: no lastKnownPos stashed for this boss, aborting")
            return
        }

        val server = level.server
        val tierName = bossInstance.tier.name.lowercase()

        val lootTableKey = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("wildbosses", "boss/$tierName")
        )
        val lootTable = server.reloadableRegistries().getLootTable(lootTableKey)

        // LootContextParamSets.EMPTY permits ZERO parameters - not "no requirements," but
        // "none allowed at all." Our loot tables only use random_chance/set_count, neither of
        // which needs ORIGIN, so we build LootParams with no parameters and use `pos` directly
        // when spawning items below instead.
        val lootParams = LootParams.Builder(level)
            .create(LootContextParamSets.EMPTY)

        val items = lootTable.getRandomItems(lootParams)
        WildBosses.logger.info("[WildBosses] Loot table boss/$tierName resolved ${items.size} item(s)")

        items.forEach { stack ->
            val itemEntity = ItemEntity(level, pos.x, pos.y, pos.z, stack)
            level.addFreshEntity(itemEntity)
        }

        WildBosses.logger.info(
            "[WildBosses] Awarded ${items.size} item(s) from boss/$tierName loot table for ${bossInstance.speciesName}"
        )

        // Clean up immediately on confirmed defeat, rather than waiting for
        // BossLifecycleTicker's periodic sweep to notice the entity is gone (up to 10 minutes
        // later otherwise). Removes both the registry entry and the glow-team membership.
        server.scoreboard.removePlayerFromTeam(bossInstance.entityUuid.toString())
        BossRegistry.unregister(bossInstance.entityUuid)
    }
}



