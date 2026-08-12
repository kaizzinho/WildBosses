package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.advancement.BossDefeatedContext
import com.kaizzinho.wildbosses.advancement.WildBossCriteria
import com.kaizzinho.wildbosses.api.WildBossLootAwardEvent
import com.kaizzinho.wildbosses.api.WildBossLootEvents
import com.kaizzinho.wildbosses.boss.BossInstance
import com.kaizzinho.wildbosses.boss.BossKillLeaderboardData
import com.kaizzinho.wildbosses.boss.BossDepartureMessages
import com.kaizzinho.wildbosses.boss.BossPersistence
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.MegaStoneGrantData
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.boss.BossMessageFormat
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.phys.AABB
import java.util.UUID
import net.minecraft.network.chat.Component

object BossBattleResultListener {

    private const val LOOT_AWARD_DELAY_TICKS = 5

    private data class PendingLootAward(
        val bossInstance: BossInstance,
        val readyAtTick: Long,
        val playerUuid: UUID,
        val wasShiny: Boolean,
        val megaEvolved: Boolean,
        val enraged: Boolean
    )
    private val pendingAwards = mutableListOf<PendingLootAward>()

    fun register() {
        CobblemonEvents.BATTLE_FLED.subscribe { event ->
            val wildActor = event.battle.actors.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe
            val bossEntity = wildActor.entity ?: return@subscribe

            val bossInstance = BossRegistry.get(bossEntity.uuid)
            if (bossInstance != null) {
                resetBossHp(bossEntity)
                revertMegaEvolution(bossEntity)
                bossInstance.currentLevelOverride = null
                BossHealthBarManager.end(bossEntity.uuid)
                BossEnrageManager.stopTracking(bossEntity.uuid)
                BossPersistence.restoreRuntimeState(bossEntity, bossInstance.tier, bossInstance.spawnedAtTick)
                BossPersistence.requestCheckpoint(bossEntity)

                val fleeingPlayer = event.battle.actors
                    .firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                fleeingPlayer?.entity?.let { WildBossCriteria.BOSS_FLED.trigger(it) }
            }
        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val wildWinner = event.winners.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
            if (wildWinner != null) {
                val bossEntity = wildWinner.entity ?: return@subscribe
                val bossInstance = BossRegistry.get(bossEntity.uuid)
                if (bossInstance != null) {
                    resetBossHp(bossEntity)
                    revertMegaEvolution(bossEntity)
                    bossInstance.currentLevelOverride = null
                    BossHealthBarManager.end(bossEntity.uuid)
                    BossEnrageManager.stopTracking(bossEntity.uuid)
                    BossPersistence.restoreRuntimeState(bossEntity, bossInstance.tier, bossInstance.spawnedAtTick)
                    BossPersistence.requestCheckpoint(bossEntity)
                }
                return@subscribe
            }

            val playerWinner = event.winners.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                ?: return@subscribe
            val player = playerWinner.entity ?: return@subscribe

            val wildLoser = event.losers.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe

            val pokemonUuid = wildLoser.pokemon.effectedPokemon.uuid
            val bossInstance = BossRegistry.getByPokemonUuid(pokemonUuid) ?: run {
                WildBosses.logger.warn("[WildBosses] Player-victory branch: no BossInstance found for pokemonUuid=$pokemonUuid, aborting")
                return@subscribe
            }

            val didEnrage = BossEnrageManager.hasEnraged(bossInstance.entityUuid)
            val wasShiny = wildLoser.pokemon.effectedPokemon.shiny
            val didMegaEvolve = bossInstance.megaStoneItemId != null

            bossInstance.defeated = true
            BossDepartureMessages.announceDefeated(bossInstance, player)

            wildLoser.pokemon.effectedPokemon.swapHeldItem(ItemStack.EMPTY, decrement = false, aiCanDrop = false)

            val overworld = player.serverLevel().server.overworld()
            BossKillLeaderboardData.get(overworld)
                .recordKill(player.uuid, player.name.string, bossInstance.tier)

            BossHealthBarManager.end(bossInstance.entityUuid)
            BossEnrageManager.stopTracking(bossInstance.entityUuid)

            try {
                cleanupStrayLoot(bossInstance)
            } catch (e: Exception) {
                WildBosses.logger.warn("[WildBosses] Immediate stray-loot cleanup failed, will be skipped this fight", e)
            }

            val currentTick = bossInstance.lastKnownLevel?.server?.overworld()?.gameTime ?: 0L
            pendingAwards.add(
                PendingLootAward(
                    bossInstance,
                    currentTick + LOOT_AWARD_DELAY_TICKS,
                    player.uuid,
                    wasShiny,
                    didMegaEvolve,
                    didEnrage
                )
            )
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (pendingAwards.isEmpty()) return@register

            val currentTick = server.overworld().gameTime
            val ready = pendingAwards.filter { it.readyAtTick <= currentTick }
            if (ready.isEmpty()) return@register

            pendingAwards.removeAll(ready)
            ready.forEach {
                awardBossLoot(it.bossInstance, it.playerUuid, it.wasShiny, it.megaEvolved, it.enraged)
            }
        }
    }

    private fun resetBossHp(bossEntity: PokemonEntity) {
        bossEntity.pokemon.currentHealth = bossEntity.pokemon.maxHealth
    }

    private fun revertMegaEvolution(bossEntity: PokemonEntity) {
        val pokemon = bossEntity.pokemon

        StringSpeciesFeature("mega_evolution", "none").apply(pokemon)
        pokemon.swapHeldItem(ItemStack.EMPTY, decrement = false, aiCanDrop = false)
        pokemon.heldItemVisible = true
    }

    private fun cleanupStrayLoot(bossInstance: BossInstance) {
        val level = bossInstance.lastKnownLevel ?: return
        val pos = bossInstance.lastKnownPos ?: return

        val cleanupRadius = 3.0
        val strayItems = level.getEntitiesOfClass(
            ItemEntity::class.java,
            AABB(
                pos.x - cleanupRadius, pos.y - cleanupRadius, pos.z - cleanupRadius,
                pos.x + cleanupRadius, pos.y + cleanupRadius, pos.z + cleanupRadius
            )
        )
        if (strayItems.isNotEmpty()) {
            WildBosses.logger.info("[WildBosses] Removing ${strayItems.size} stray item(s) near boss death position immediately")
            strayItems.forEach { it.discard() }
        }
    }

    private fun awardBossLoot(
        bossInstance: BossInstance,
        playerUuid: UUID,
        wasShiny: Boolean,
        megaEvolved: Boolean,
        enraged: Boolean
    ) {
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

        val lootParams = LootParams.Builder(level)
            .create(LootContextParamSets.EMPTY)

        val items = lootTable.getRandomItems(lootParams)

        val recipient = server.playerList.getPlayer(playerUuid)

        val lootEvent = WildBossLootAwardEvent(
            entityUuid = bossInstance.entityUuid,
            player = recipient,
            level = level,
            position = pos,
            speciesName = bossInstance.speciesName ?: "?",
            tierName = tierName,
            stacks = items.toList()
        )
        WildBossLootEvents.fire(lootEvent)

        if (!lootEvent.claimed) {
            var lootLine: Component = BossMessageFormat.plain("")
            items.forEachIndexed { index, stack ->
                if (index > 0) lootLine = lootLine.copy()
                    .append(BossMessageFormat.plainKey("wildbosses.message.loot_separator"))
                lootLine = lootLine.copy().append(BossMessageFormat.lootItem(stack))
            }

            items.forEach { stack ->
                if (recipient != null) {
                    recipient.inventory.add(stack)
                } else {
                    level.addFreshEntity(ItemEntity(level, pos.x, pos.y, pos.z, stack))
                }
            }

            if (recipient != null && items.isNotEmpty()) {
                val message = BossMessageFormat.build(
                    BossMessageFormat.bossName(bossInstance.tier, bossInstance.speciesName ?: "?")
                        .append(BossMessageFormat.plainKey("wildbosses.message.defeated_received"))
                        .append(lootLine)
                )
                recipient.sendSystemMessage(message)
            }
        }

        val megaStoneItemId = bossInstance.megaStoneItemId
        if (megaStoneItemId != null) {
            val speciesKey = bossInstance.speciesName?.lowercase() ?: ""
            val grantData = MegaStoneGrantData.get(level)
            if (!grantData.hasBeenGranted(playerUuid, speciesKey)) {
                val item = BuiltInRegistries.ITEM.get(megaStoneItemId)
                if (item != Items.AIR) {
                    val stoneStack = ItemStack(item)
                    if (recipient != null) {
                        recipient.inventory.add(stoneStack)
                    } else {
                        level.addFreshEntity(ItemEntity(level, pos.x, pos.y, pos.z, stoneStack))
                    }
                    grantData.markGranted(playerUuid, speciesKey)
                }
            }
        }

        if (recipient != null) {
            val leaderboard = BossKillLeaderboardData.get(server.overworld())
            WildBossCriteria.BOSS_DEFEATED.trigger(
                recipient,
                BossDefeatedContext(
                    tier = bossInstance.tier,
                    wasShiny = wasShiny,
                    megaEvolved = megaEvolved,
                    enraged = enraged,
                    totalKills = leaderboard.totalKillsFor(playerUuid),
                    tiersDefeated = leaderboard.tiersDefeatedBy(playerUuid)
                )
            )
        }

        server.scoreboard.removePlayerFromTeam(bossInstance.entityUuid.toString())
        BossRegistry.unregister(bossInstance.entityUuid)
        BossPersistence.requestLevelCheckpoint(level)
    }
}
