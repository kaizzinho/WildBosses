package com.kaizzinho.wildbosses.battle

import com.google.gson.JsonParser
//import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.io.InputStreamReader
import java.util.UUID

data class MegaStoneEntry(
    val showdownId: String,
    val featureValue: String, // "mega", "mega_x", "mega_y", etc.
    val itemId: ResourceLocation
)

object MegaEvolutionManager {

    private val megaStonesBySpecies: MutableMap<String, MutableList<MegaStoneEntry>> = mutableMapOf()

    // Bosses queued to Mega Evolve on their first turn - set by BattleTierScalingListener
    // right after giving the boss its held stone, consumed once by MegaTriggeringAI when it
    // stamps the "mega" gimmick flag onto the AI's first chosen move.
    private val pendingMegaTrigger = mutableSetOf<UUID>()

    fun register() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            loadFromResources(server.resourceManager)
        }
    }

    private fun loadFromResources(resourceManager: ResourceManager) {
        megaStonesBySpecies.clear()
        val resources = resourceManager.listResources("mega_showdown/mega") { it.path.endsWith(".json") }

        for ((location, resource) in resources) {
            try {
                InputStreamReader(resource.open()).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    val speciesList = json.getAsJsonArray("pokemons").map { it.asString.lowercase() }
                    val aspectString = json.getAsJsonObject("aspect_conditions")
                        .getAsJsonObject("apply")
                        .getAsJsonArray("aspects")[0].asString // e.g. "mega_evolution=mega_x"
                    val featureValue = aspectString.substringAfter("=")

                    val fileName = location.path.substringAfterLast("/").removeSuffix(".json")
                    val itemId = ResourceLocation.fromNamespaceAndPath("mega_showdown", fileName)

                    val entry = MegaStoneEntry(
                        showdownId = json.get("showdown_id").asString,
                        featureValue = featureValue,
                        itemId = itemId
                    )

                    speciesList.forEach { species ->
                        megaStonesBySpecies.getOrPut(species) { mutableListOf() }.add(entry)
                    }
                }
            } catch (_: Exception) {
               // WildBosses.logger.warn("[WildBosses] Failed to parse mega stone data at $location", e)
            }
        }

        //WildBosses.logger.info("[WildBosses] Loaded mega evolution data for ${megaStonesBySpecies.size} species")
    }

    fun getEligibleEntries(speciesName: String): List<MegaStoneEntry> =
        megaStonesBySpecies[speciesName.lowercase()] ?: emptyList()

    fun markPendingMegaTrigger(bossEntityUuid: UUID) {
        pendingMegaTrigger.add(bossEntityUuid)
    }

    fun consumePendingMegaTrigger(bossEntityUuid: UUID): Boolean {
        return pendingMegaTrigger.remove(bossEntityUuid)
    }
}