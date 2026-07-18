package com.kaizzinho.wildbosses

import net.fabricmc.api.ClientModInitializer

object WildBossesClient : ClientModInitializer {
	override fun onInitializeClient() {
		WildBosses.LOGGER.info("[WildBosses] Client init - nameplate override ('Level ??') hooks go here.")
	}
}
