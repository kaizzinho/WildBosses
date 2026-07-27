package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.ai.StrongBattleAI
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossInstance
import com.kaizzinho.wildbosses.boss.BossMessageFormat
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
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

    private val battleAIField = AIBattleActor::class.java.getDeclaredField("battleAI").apply { isAccessible = true }

    private val SELF_DESTRUCT_MOVES = setOf(
        "explosion", "selfdestruct", "mindblown", "mistyexplosion", "chloroblast", "steelbeam"
    )

    private val RECHARGE_MOVES = setOf(
        "hyperbeam", "gigaimpact", "blastburn", "hydrocannon", "frenzyplant",
        "rockwrecker", "roaroftime", "eternabeam"
    )

    private val CHARGE_TURN_MOVES = setOf(
        "solarbeam", "solarblade", "skyattack", "skullbash", "razorwind",
        "dig", "fly", "bounce", "dive", "shadowforce", "phantomforce",
        "freezeshock", "iceburn", "meteorbeam", "electroshot", "skydrop"
    )

    private val LOCKED_IN_MOVES = setOf(
        "uproar", "thrash", "petaldance", "outrage", "rollout", "iceball"
    )

    private val CONDITIONAL_FAIL_MOVES = setOf(
        "dreameater", "lastresort", "synchronoise", "focuspunch",
        "counter", "mirrorcoat", "metalburst", "bide",
        "hiddenpower"
    )

    private val SELF_NERFING_MOVES = setOf(
        "dracometeor", "leafstorm", "overheat", "psychoboost", "fleurcannon",
        "superpower", "closecombat", "vcreate"
    )

    private val BLACKLISTED_MOVESET_SPECIES = setOf(
        "ditto",
        "smeargle"
    )

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
                val realAI = StrongBattleAI(bossInstance.tier.aiSkill)
                battleAIField.set(wildActor, MegaTriggeringAI(realAI, bossEntity.uuid))
            } catch (e: Exception) {
                WildBosses.logger.error("[WildBosses] Failed to apply StrongBattleAI to boss ${bossEntity.pokemon.species.name} - falling back to default AI", e)
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

            StrayLootWatcher.startWatching(bossEntity.uuid, bossInstance.lastKnownLevel!!)
            applyDamagingMoveset(bossPokemon)
            applyMegaEvolutionIfEligible(bossEntity, bossInstance)

            BossHealthBarManager.start(bossEntity, player, tier, scaledLevel)
            BossEnrageManager.startTracking(bossEntity.uuid, battle, wildActor, tier)
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

    private fun applyDamagingMoveset(bossPokemon: Pokemon) {
        if (bossPokemon.species.showdownId() in BLACKLISTED_MOVESET_SPECIES) {
            return
        }
        val favorsPhysical = Cobblemon.statProvider.getStatForPokemon(bossPokemon, Stats.ATTACK) >=
                Cobblemon.statProvider.getStatForPokemon(bossPokemon, Stats.SPECIAL_ATTACK)
        val favoredCategory = if (favorsPhysical) DamageCategories.PHYSICAL else DamageCategories.SPECIAL

        val bossTypes = bossPokemon.species.types.toSet()

        val allCandidates = bossPokemon.species.moves.getAllLegalMoves()
            .asSequence()
            .filter { it.damageCategory != DamageCategories.STATUS }
            .filter { it.accuracy <= 0 || it.accuracy >= 90 }
            .filter { it.name !in SELF_DESTRUCT_MOVES }
            .filter { it.name !in RECHARGE_MOVES }
            .filter { it.name !in CHARGE_TURN_MOVES }
            .filter { it.name !in LOCKED_IN_MOVES }
            .filter { it.name !in CONDITIONAL_FAIL_MOVES }
            .filter { it.name !in SELF_NERFING_MOVES }
            .toList()

        if (allCandidates.isEmpty()) {
            WildBosses.logger.warn("[WildBosses] No damaging moves passed filters for ${bossPokemon.species.name} - leaving natural moveset unchanged")
            return
        }

        val favoredCandidates = allCandidates.filter { it.damageCategory == favoredCategory }

        fun bestPerType(pool: List<com.cobblemon.mod.common.api.moves.MoveTemplate>) =
            pool.groupBy { it.elementalType }.mapValues { (_, moves) -> moves.maxByOrNull { it.power }!! }.values

        val selected = bestPerType(favoredCandidates).sortedByDescending { it.power }.take(4).toMutableList()

        val hasStab = selected.any { it.elementalType in bossTypes }
        if (!hasStab) {
            val bestStab = bestPerType(favoredCandidates).filter { it.elementalType in bossTypes }.maxByOrNull { it.power }
            if (bestStab != null) {
                selected.minByOrNull { it.power }?.let { weakest ->
                    selected.remove(weakest)
                    selected.add(bestStab)
                }
            }
        }

        if (selected.size < 4) {
            val usedTypes = selected.map { it.elementalType }.toSet()
            val backfill = bestPerType(allCandidates.filter { it.damageCategory != favoredCategory })
                .filter { it.elementalType !in usedTypes }
                .sortedByDescending { it.power }
                .take(4 - selected.size)
            selected.addAll(backfill)
        }

        val finalMoves = selected.sortedByDescending { it.power }.map { it.name }

        WildBosses.logger.info(
            "[WildBosses] ${bossPokemon.species.name} moveset (favored: ${if (favorsPhysical) "PHYSICAL" else "SPECIAL"}): " +
                    allCandidates.filter { it.name in finalMoves }
                        .joinToString { "${it.name}(type=${it.elementalType.showdownId}, cat=${it.damageCategory.name}, pwr=${it.power}, acc=${it.accuracy})" }
        )

        val properties = PokemonProperties()
        properties.moves = finalMoves
        properties.apply(bossPokemon)
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
        val title = Component.translatable(
            "wildbosses.title.encounter",
            BossMessageFormat.tierName(tier)
        ).withStyle(tier.color, ChatFormatting.BOLD)
        val subtitle = Component.literal(entity.pokemon.species.name).withStyle(ChatFormatting.ITALIC)

        player.connection.send(ClientboundSetTitleTextPacket(title))
        player.connection.send(ClientboundSetSubtitleTextPacket(subtitle))

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