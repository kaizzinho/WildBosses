package com.kaizzinho.wildbosses

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import com.kaizzinho.wildbosses.event.ModEvents

object WildBosses : ModInitializer {
	const val MOD_ID = "wildbosses"
	val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		logger.info("[WildBosses] Initializing Boss System...")
		ModEvents.register()
	}
}