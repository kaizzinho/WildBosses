package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object BossDepartureMessages {

    fun announceNaturalDespawn(entity: PokemonEntity) {
        val instance = BossRegistry.get(entity.uuid) ?: return
        if (instance.defeated) return

        val server = entity.server ?: return
        broadcast(
            server,
            instance.tier,
            entity.pokemon.species.name,
            "wildbosses.message.wandered_away"
        )

        server.scoreboard.removePlayerFromTeam(instance.entityUuid.toString())
        BossRegistry.unregister(instance.entityUuid)
    }

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
