package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.ai.StrongBattleAI
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossInstance
import com.kaizzinho.wildbosses.boss.BossMessageFormat
import com.kaizzinho.wildbosses.boss.BossPersistence
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack


object BattleTierScalingListener {

    private const val BATTLE_SLIDER_MOD_ID = "kaizzinhobattleslider"
    private val battleSliderLoaded by lazy {
        FabricLoader.getInstance().isModLoaded(BATTLE_SLIDER_MOD_ID)
    }

    private val battleAIField = AIBattleActor::class.java.getDeclaredField("battleAI").apply { isAccessible = true }

    fun register() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            val battle = event.battle

            val wildActor = battle.actors.firstOrNull { it.type == ActorType.WILD } as? PokemonBattleActor
                ?: return@subscribe
            val bossEntity = wildActor.entity ?: return@subscribe

            val bossInstance = BossRegistry.get(bossEntity.uuid) ?: return@subscribe

            bossInstance.lastKnownPos = bossEntity.position()
            bossInstance.lastKnownLevel = bossEntity.level() as? ServerLevel
            bossInstance.speciesName = bossEntity.pokemon.species.name

            try {
                val baseAI = StrongBattleAI(bossInstance.tier.aiSkill)
                val aggressiveAI = AggressiveBossAI(
                    delegate = baseAI,
                    tier = bossInstance.tier,
                    bossEntityUuid = bossEntity.uuid,
                    bossName = bossEntity.pokemon.species.name
                )
                battleAIField.set(wildActor, MegaTriggeringAI(aggressiveAI, bossEntity.uuid))
                WildBosses.logger.info(
                    "[BossAI] Active for ${bossEntity.pokemon.species.name} tier=${bossInstance.tier.name} skill=${bossInstance.tier.aiSkill} uuid=${bossEntity.uuid}"
                )
            } catch (e: Exception) {
                WildBosses.logger.error(
                    "[BossAI] Failed to apply aggressive AI to ${bossEntity.pokemon.species.name}; falling back to Cobblemon's default AI",
                    e
                )
            }

            val playerActor = battle.actors.firstOrNull { it.type == ActorType.PLAYER } as? PlayerBattleActor
                ?: return@subscribe
            val player = playerActor.entity ?: return@subscribe

            val party = Cobblemon.storage.getParty(player)
            val highestPlayerLevel = party.mapNotNull { it.level }.maxOrNull()

            if (highestPlayerLevel == null) {
                WildBosses.logger.warn("[WildBosses] Player ${player.name.string} has an empty party during boss battle - skipping scaling")
                return@subscribe
            }

            val tier = bossInstance.tier
            val scaledLevel = (highestPlayerLevel + tier.levelBonus).coerceAtMost(WildBossesConfig.data.maxScaledLevel)

            val bossPokemon = bossEntity.pokemon
            bossPokemon.level = scaledLevel
            bossInstance.currentLevelOverride = scaledLevel
            BossStatDiagnostics.log(bossPokemon, tier)

            StrayLootWatcher.startWatching(bossEntity.uuid, bossInstance.lastKnownLevel!!)
            BossMovesetBuilder.apply(bossPokemon, tier)
            applyMegaEvolutionIfEligible(bossEntity, bossInstance)

            BossHealthBarManager.start(bossEntity, player, tier, scaledLevel)
            BossEnrageManager.startTracking(bossEntity.uuid, battle, wildActor, tier)
            BossPersistence.requestCheckpoint(bossEntity)
            announceBattleEngage(bossEntity, player, tier)

            val chatMessage = BossMessageFormat.build(
                BossMessageFormat.bossName(tier, bossPokemon.species.name)
                    .append(BossMessageFormat.plainKey("wildbosses.message.scaled_to_level"))
                    .append(BossMessageFormat.levelValue(tier, scaledLevel))
                    .append(
                        Component.translatable(
                            "wildbosses.message.scaling_detail",
                            highestPlayerLevel,
                            tier.levelBonus
                        ).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                    )
            )
            player.sendSystemMessage(chatMessage)
        }
    }

    private fun applyMegaEvolutionIfEligible(bossEntity: PokemonEntity, bossInstance: BossInstance) {
        val chance = WildBossesConfig.tier(bossInstance.tier.name).megaEvolutionChance
        if (chance <= 0.0) return

        val bossPokemon = bossEntity.pokemon
        val eligibleEntries = MegaEvolutionManager.getEligibleEntries(bossPokemon.species.name)
        if (eligibleEntries.isEmpty()) return
        if (kotlin.random.Random.nextDouble() >= chance) return

        val chosen = eligibleEntries.random()
        val item = BuiltInRegistries.ITEM.get(chosen.itemId)
        if (item == Items.AIR) {
            return
        }

        val stoneStack = ItemStack(item)
        bossPokemon.swapHeldItem(stoneStack, decrement = false, aiCanDrop = false)
        bossPokemon.heldItemVisible = false
        bossInstance.megaStoneItemId = chosen.itemId
        MegaEvolutionManager.markPendingMegaTrigger(bossEntity.uuid)
    }

    private fun announceBattleEngage(entity: PokemonEntity, player: ServerPlayer, tier: BossTier) {
        if (!battleSliderLoaded) {
            val title = Component.translatable(
                "wildbosses.title.encounter",
                BossMessageFormat.tierName(tier)
            ).withStyle(tier.color, ChatFormatting.BOLD)
            val subtitle = Component.literal(entity.pokemon.species.name).withStyle(ChatFormatting.ITALIC)

            player.connection.send(ClientboundSetTitleTextPacket(title))
            player.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
        }

        val speciesKey = entity.pokemon.species.name
            .lowercase()
            .replace("♀", "f")
            .replace("♂", "m")
            .replace(Regex("[^a-z0-9-]"), "")
        val cryLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon.$speciesKey.cry")
        val cry = SoundEvent.createVariableRangeEvent(cryLocation)

        entity.level().playSound(null, entity.blockPosition(), cry, SoundSource.HOSTILE, 1.0f, 1.0f)
    }
}
