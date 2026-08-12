package com.kaizzinho.wildbosses

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import com.kaizzinho.wildbosses.event.ModEvents
import com.cobblemon.mod.common.Cobblemon
import com.kaizzinho.wildbosses.advancement.WildBossCriteria
import com.kaizzinho.wildbosses.boss.BossTier
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import com.kaizzinho.wildbosses.boss.WildBossEntityData
import com.kaizzinho.wildbosses.config.WildBossesConfig


object WildBosses : ModInitializer {
	const val MOD_ID = "wildbosses"
	const val PERSISTENCE_PROBE_BUILD = "20260811-persist-probe-2"
	val logger = LoggerFactory.getLogger(MOD_ID)!!

	override fun onInitialize() {
		logger.info("[WildBosses] Initializing Boss System...")
		logger.warn("[WildBosses-PERSISTENCE-PROBE] build=$PERSISTENCE_PROBE_BUILD loaded")

		WildBossEntityData.IS_BOSS
		WildBossesConfig.data
		WildBossCriteria.register()

		ModEvents.register()

		ServerLifecycleEvents.SERVER_STARTED.register {
			ensureCobblemonLevelCap()
		}
	}

	fun ensureCobblemonLevelCap() {
		val requiredCap = WildBossesConfig.data.maxScaledLevel.coerceAtLeast(100)
		if (Cobblemon.config.maxPokemonLevel >= requiredCap) return

		Cobblemon.config.maxPokemonLevel = requiredCap
		logger.info("[WildBosses] Raised Cobblemon maxPokemonLevel to $requiredCap for Boss scaling")
	}
}
