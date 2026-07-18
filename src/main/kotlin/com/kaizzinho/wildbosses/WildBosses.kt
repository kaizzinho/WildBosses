package com.kaizzinho.wildbosses

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object WildBosses : ModInitializer {
	const val MOD_ID = "wildbosses"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		LOGGER.info("[WildBosses] Initializing on top of Cobblemon...")
		// fazer ainda: register spawn hook (1/128 boss roll), BattleStartedEvent listener,
		// loot table logic, and NBT mega-stone memory check here.
	}
}
