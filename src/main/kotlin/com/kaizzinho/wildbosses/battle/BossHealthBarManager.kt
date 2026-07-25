package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.boss.BossTier
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import java.util.UUID

/**
 * Manages a ServerBossEvent (the Ender Dragon/Wither-style bar) per active boss fight.
 * Scoped strictly to the single player actually engaged in that fight - never shown to
 * anyone else, and removed the instant that specific fight ends (win, loss, or flee).
 */
object BossHealthBarManager {

    // Disabled - Cobblemon's own battle GUI already shows HP as a percentage bar,
    // making this redundant. Flip back to true to re-enable.
    private const val ENABLED = false

    private const val UPDATE_INTERVAL_TICKS = 2L // once per second is plenty for an HP bar

    private data class ActiveBar(val bar: ServerBossEvent, val bossEntityUuid: UUID)

    // keyed by boss entity UUID - the one identifier that survives across BATTLE_STARTED_PRE,
    // BATTLE_FLED, and both branches of BATTLE_VICTORY without needing a live entity reference
    // to remove an entry (the boss entity may already be gone by the time we need to clean up,
    // same issue we solved for loot awarding).
    private val activeBars = mutableMapOf<UUID, ActiveBar>()
    private var tickCounter = 0L

    private fun barColorFor(tier: BossTier): BossEvent.BossBarColor = when (tier) {
        BossTier.UNCOMMON -> BossEvent.BossBarColor.GREEN
        BossTier.RARE -> BossEvent.BossBarColor.BLUE
        BossTier.EPIC -> BossEvent.BossBarColor.PURPLE
        BossTier.LEGENDARY -> BossEvent.BossBarColor.YELLOW
        BossTier.MYTHIC -> BossEvent.BossBarColor.PINK
    }

    private fun barName(tier: BossTier, speciesName: String, level: Int): Component {
        val tierLabel = tier.name.lowercase().replaceFirstChar { it.uppercase() }
        return Component.literal("$tierLabel Boss $speciesName Lv $level")
            .withStyle(tier.color, ChatFormatting.ITALIC, ChatFormatting.BOLD)
    }

    fun start(bossEntity: PokemonEntity, player: ServerPlayer, tier: BossTier, level: Int) {
        if (!ENABLED) return

        // Guard against double-starting if this somehow fires twice for the same fight.
        activeBars[bossEntity.uuid]?.let { end(bossEntity.uuid) }

        val bar = ServerBossEvent(
            barName(tier, bossEntity.pokemon.species.name, level),
            barColorFor(tier),
            BossEvent.BossBarOverlay.PROGRESS
        )
        bar.addPlayer(player)

        val hpRatio = (bossEntity.pokemon.currentHealth / bossEntity.pokemon.maxHealth.toFloat()).coerceIn(0f, 1f)
        bar.setProgress(hpRatio)

        activeBars[bossEntity.uuid] = ActiveBar(bar, bossEntity.uuid)
    }

    fun end(bossEntityUuid: UUID) {
        if (!ENABLED) return
        activeBars.remove(bossEntityUuid)?.bar?.removeAllPlayers()
    }

    fun register() {
        if (!ENABLED) return

        ServerTickEvents.END_SERVER_TICK.register { server ->
            tickCounter++
            if (tickCounter % UPDATE_INTERVAL_TICKS != 0L) return@register
            if (activeBars.isEmpty()) return@register

            for ((bossUuid, active) in activeBars) {
                val entity = findEntity(server, bossUuid) ?: continue
                val hpRatio = (entity.pokemon.currentHealth / entity.pokemon.maxHealth.toFloat()).coerceIn(0f, 1f)
                active.bar.setProgress(hpRatio)
            }
        }
    }

    private fun findEntity(server: MinecraftServer, uuid: UUID): PokemonEntity? {
        for (level in server.allLevels) {
            val entity = level.getEntity(uuid)
            if (entity is PokemonEntity) return entity
        }
        return null
    }
}