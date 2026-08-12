package com.kaizzinho.wildbosses.boss

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object BossDepartureMessages {

    fun announceLifetimeExpired(server: MinecraftServer, instance: BossInstance, speciesName: String) {
        if (instance.defeated) return
        broadcast(server, instance.tier, speciesName, "wildbosses.message.lifetime_expired")
    }

    fun announceDefeated(instance: BossInstance, player: ServerPlayer) {
        val server = player.serverLevel().server
        broadcast(
            server,
            instance.tier,
            instance.speciesName ?: "?",
            "wildbosses.message.defeated_by",
            player.name.string
        )
    }

    private fun broadcast(
        server: MinecraftServer,
        tier: BossTier,
        speciesName: String,
        key: String,
        vararg args: Any
    ) {
        val message = BossMessageFormat.build(
            BossMessageFormat.bossName(tier, speciesName)
                .append(BossMessageFormat.plainKey(key, *args))
        )
        server.playerList.broadcastSystemMessage(message, false)
    }
}
