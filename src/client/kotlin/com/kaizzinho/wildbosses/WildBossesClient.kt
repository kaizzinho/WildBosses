package com.kaizzinho.wildbosses

import net.fabricmc.api.ClientModInitializer

class WildBossesClient : ClientModInitializer {
	override fun onInitializeClient() {
		WildBosses.logger.info("[WildBosses] Client entrypoint initialized.")
	}
}