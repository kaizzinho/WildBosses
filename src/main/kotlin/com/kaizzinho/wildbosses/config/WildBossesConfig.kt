package com.kaizzinho.wildbosses.config

import com.google.gson.GsonBuilder
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class TierConfig(
    var weight: Int,
    var levelBonus: Int,
    var aiSkill: Int,
    var shinyChance: Double,
    var guaranteedPerfectIVs: Boolean,
    var megaEvolutionChance: Double
)

data class WildBossesConfigData(
    var configVersion: Int = 2,
    var bossSpawnChance: Double = 1.0 / 512,
    var spawnCooldownTicks: Long = 18000L,
    var excludedSpeciesLabels: MutableList<String> = mutableListOf("legendary", "mythical", "ultra_beast"),
    var blockedBossMoves: MutableList<String> = defaultBlockedBossMoves(),
    var maxScaledLevel: Int = 200,
    var enrageIntervalTurns: Int = 8,
    var enrageStatStages: Int = 1,
    var bossAiDebugLogging: Boolean = false,
    var bossLifetimeTicks: Long = 12000L,
    var tiers: MutableMap<String, TierConfig> = defaultTiers()
)

private fun defaultBlockedBossMoves(): MutableList<String> = mutableListOf(
    "fissure",
    "guillotine",
    "horndrill",
    "sheercold",
    "perishsong",
    "destinybond",
    "endeavor",
    "painsplit",
    "superfang",
    "naturesmadness",
    "ruination",
    "toxic",
    "leechseed",
    "saltcure",
    "curse",
    "haze",
    "clearsmog",
    "topsyturvy",
    "trick",
    "switcheroo"
)

private fun defaultTiers(): MutableMap<String, TierConfig> = mutableMapOf(
    "UNCOMMON" to TierConfig(54, 5, 2, 0.0, false, 0.0),
    "RARE" to TierConfig(30, 10, 3, 0.0, false, 0.0),
    "EPIC" to TierConfig(10, 15, 4, 0.0, true, 0.0),
    "LEGENDARY" to TierConfig(5, 20, 5, 0.25, true, 0.50),
    "MYTHIC" to TierConfig(1, 25, 5, 0.45, true, 1.0)
)

object WildBossesConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_DIR = FabricLoader.getInstance().configDir.resolve("wildbosses")
    private val CONFIG_FILE = CONFIG_DIR.resolve("wildbosses.json").toFile()
    private val LEGACY_CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("wildbosses.json").toFile()

    var data: WildBossesConfigData = load()
        private set

    fun reload() {
        data = load()
    }

    fun snapshot(): WildBossesConfigData = copyOf(data)

    fun defaults(): WildBossesConfigData = WildBossesConfigData()

    fun save(newData: WildBossesConfigData) {
        data = sanitize(copyOf(newData))
        CONFIG_FILE.parentFile?.mkdirs()
        CONFIG_FILE.writeText(GSON.toJson(data))
    }

    private fun load(): WildBossesConfigData {
        migrateLegacyConfigLocation()

        if (!CONFIG_FILE.exists()) {
            CONFIG_FILE.parentFile?.mkdirs()
            val defaults = WildBossesConfigData()
            CONFIG_FILE.writeText(GSON.toJson(defaults))
            return defaults
        }

        return try {
            val sourceText = CONFIG_FILE.readText()
            val stripped = stripLineComments(sourceText)
            val raw = GSON.fromJson(stripped, WildBossesConfigData::class.java) ?: WildBossesConfigData()
            val hadComments = sourceText.lineSequence().any { it.trimStart().startsWith("//") }
            sanitize(migrate(raw, stripped, hadComments))
        } catch (_: Exception) {
            WildBossesConfigData()
        }
    }

    private fun migrateLegacyConfigLocation() {
        if (CONFIG_FILE.exists() || !LEGACY_CONFIG_FILE.exists()) return

        CONFIG_DIR.toFile().mkdirs()

        runCatching<Unit> {
            Files.move(
                LEGACY_CONFIG_FILE.toPath(),
                CONFIG_FILE.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }.recoverCatching {
            // keep old installs from losing their config
            LEGACY_CONFIG_FILE.copyTo(CONFIG_FILE, overwrite = true)
            LEGACY_CONFIG_FILE.delete()
            Unit
        }.onFailure {
            WildBosses.logger.warn(
                "[WildBosses] Could not migrate config to ${CONFIG_FILE.path}",
                it
            )
        }
    }

    private fun stripLineComments(json: String): String =
        json.lineSequence()
            .filterNot { it.trimStart().startsWith("//") }
            .joinToString("\n")

    private fun migrate(raw: WildBossesConfigData, sourceText: String, removedComments: Boolean): WildBossesConfigData {
        val legacy = mapOf("UNCOMMON" to 15, "RARE" to 25, "EPIC" to 35, "LEGENDARY" to 45, "MYTHIC" to 55)
        val updated = mapOf("UNCOMMON" to 5, "RARE" to 10, "EPIC" to 15, "LEGENDARY" to 20, "MYTHIC" to 25)
        val stillOnOldDefaults = legacy.all { (name, bonus) -> raw.tiers[name]?.levelBonus == bonus }

        if (stillOnOldDefaults) {
            updated.forEach { (name, bonus) -> raw.tiers[name]?.levelBonus = bonus }
            WildBosses.logger.info("[WildBosses] Migrated tier level bonuses to +5/+10/+15/+20/+25")
        }

        raw.configVersion = 2

        if (stillOnOldDefaults || !sourceText.contains("\"configVersion\"")) {
            CONFIG_FILE.writeText(GSON.toJson(raw))
        } else {
            val configText = addMissingMoveBlacklist(sourceText)
            if (removedComments || configText != sourceText) {
                CONFIG_FILE.writeText(configText)
            }
        }

        return raw
    }

    private fun addMissingMoveBlacklist(sourceText: String): String {
        if (Regex("\"blockedBossMoves\"\\s*:").containsMatchIn(sourceText)) return sourceText

        val tiers = Regex("(?m)^[ \t]*\"tiers\"\\s*:").find(sourceText) ?: return sourceText
        val tierLine = tiers.range.first
        val indent = sourceText.substring(tierLine, sourceText.indexOf('"', tierLine))
        val moves = defaultBlockedBossMoves().joinToString(",\n") { "${indent}  \"$it\"" }
        val addition = "${indent}\"blockedBossMoves\": [\n$moves\n${indent}],\n\n"

        return sourceText.substring(0, tierLine) + addition + sourceText.substring(tierLine)
    }

    private fun sanitize(raw: WildBossesConfigData): WildBossesConfigData {
        val defaults = WildBossesConfigData()

        raw.configVersion = raw.configVersion.coerceAtLeast(2)
        raw.bossSpawnChance = raw.bossSpawnChance.coerceIn(0.0, 1.0)
        raw.spawnCooldownTicks = raw.spawnCooldownTicks.coerceAtLeast(0L)
        raw.maxScaledLevel = raw.maxScaledLevel.coerceIn(1, 1000)
        raw.enrageIntervalTurns = raw.enrageIntervalTurns.coerceAtLeast(1)
        raw.enrageStatStages = raw.enrageStatStages.coerceIn(0, 6)
        raw.bossLifetimeTicks = raw.bossLifetimeTicks.coerceAtLeast(20L)
        raw.blockedBossMoves = (raw.blockedBossMoves as? List<*>)
            ?.mapNotNull { it as? String }
            ?.map { it.lowercase().filter(Char::isLetterOrDigit) }
            ?.filter { it.isNotEmpty() }
            ?.distinct()
            ?.toMutableList()
            ?: defaultBlockedBossMoves()

        if (raw.excludedSpeciesLabels.isEmpty() && defaults.excludedSpeciesLabels.isNotEmpty()) {
            raw.excludedSpeciesLabels = defaults.excludedSpeciesLabels.toMutableList()
        }

        val sanitizedTiers = mutableMapOf<String, TierConfig>()
        for ((tierName, defaultTier) in defaultTiers()) {
            val existing = raw.tiers[tierName]
            sanitizedTiers[tierName] = if (existing == null) {
                defaultTier
            } else {
                TierConfig(
                    weight = existing.weight.coerceAtLeast(1),
                    levelBonus = existing.levelBonus.coerceAtLeast(0),
                    aiSkill = existing.aiSkill.coerceIn(0, 5),
                    shinyChance = existing.shinyChance.coerceIn(0.0, 1.0),
                    guaranteedPerfectIVs = existing.guaranteedPerfectIVs,
                    megaEvolutionChance = existing.megaEvolutionChance.coerceIn(0.0, 1.0)
                )
            }
        }
        raw.tiers = sanitizedTiers

        return raw
    }

    private fun copyOf(source: WildBossesConfigData): WildBossesConfigData =
        source.copy(
            excludedSpeciesLabels = source.excludedSpeciesLabels.toMutableList(),
            blockedBossMoves = source.blockedBossMoves.toMutableList(),
            tiers = source.tiers.mapValues { (_, tier) -> tier.copy() }.toMutableMap()
        )

    fun tier(name: String): TierConfig =
        data.tiers[name] ?: error("Missing tier config for $name after sanitize()")
}
