package com.kaizzinho.wildbosses

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import com.kaizzinho.wildbosses.event.ModEvents
import com.cobblemon.mod.common.Cobblemon
import com.kaizzinho.wildbosses.advancement.WildBossCriteria
import com.kaizzinho.wildbosses.boss.BossTier
//import net.minecraft.core.registries.BuiltInRegistries
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
//import java.io.File
import com.kaizzinho.wildbosses.boss.WildBossEntityData
import com.kaizzinho.wildbosses.config.WildBossesConfig


object WildBosses : ModInitializer {
	const val MOD_ID = "wildbosses"
	val logger = LoggerFactory.getLogger(MOD_ID)!!

	override fun onInitialize() {
		logger.info("[WildBosses] Initializing Boss System...")

		WildBossEntityData.IS_BOSS
		WildBossesConfig.data // force early load, ensures config/wildbosses/wildbosses.json exists at boot
		WildBossCriteria.register()

		ModEvents.register()

		ServerLifecycleEvents.SERVER_STARTED.register {
			if (Cobblemon.config.maxPokemonLevel < 200) {
				Cobblemon.config.maxPokemonLevel = 200
				logger.info("[WildBosses] Raised Cobblemon's maxPokemonLevel to 200 for boss overflow scaling")
			}
		}
	}
}

//private fun dumpAllItems() {
//	val allIds = BuiltInRegistries.ITEM.keySet().map { it.toString() }.sorted()
//	File("wildbosses_all_items.txt").writeText(allIds.joinToString("\n"))
//
//	val cobblemonOnly = allIds.filter { it.startsWith("cobblemon:") }
//	File("wildbosses_cobblemon_items.txt").writeText(cobblemonOnly.joinToString("\n"))
//
//	WildBosses.logger.info("[WildBosses] Dumped ${allIds.size} total items (${cobblemonOnly.size} Cobblemon-specific) to project root")
//}