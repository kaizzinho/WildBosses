package com.kaizzinho.wildbosses.config

import com.google.gson.GsonBuilder
import com.kaizzinho.wildbosses.WildBosses
import net.fabricmc.loader.api.FabricLoader
import java.util.Locale

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
    var maxScaledLevel: Int = 200,
    var enrageIntervalTurns: Int = 8,
    var enrageStatStages: Int = 1,
    var bossAiDebugLogging: Boolean = false,
    var bossLifetimeTicks: Long = 12000L,
    var tiers: MutableMap<String, TierConfig> = defaultTiers()
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
    private val CONFIG_FILE = FabricLoader.getInstance().configDir
        .resolve("wildbosses")
        .resolve("wildbosses.json")
        .toFile()

    private val TEMPLATE_EN = """
        {
          // Config version. Leave it.
          "configVersion": 2,

          // Boss roll. Default: 1/512.
          "bossSpawnChance": 0.001953125,

          // Cooldown in ticks. 20 ticks = 1s.
          "spawnCooldownTicks": 18000,

          // Cobblemon labels blocked from Boss rolls.
          "excludedSpeciesLabels": ["legendary", "mythical", "ultra_beast"],

          // Max scaled Boss level.
          "maxScaledLevel": 200,

          // Rare+ Enrage interval. Warns 1 turn early.
          "enrageIntervalTurns": 8,

          // Stages per Enrage. Battle cap is +6.
          "enrageStatStages": 1,

          // AI scores/choices in console. Pretty noisy.
          "bossAiDebugLogging": false,

          // Untouched Boss lifetime. 12000 ticks = 10 min.
          "bossLifetimeTicks": 12000,

          // Tier odds after a spawn becomes a Boss.
          "tiers": {
            "UNCOMMON": {
              "weight": 54,
              "levelBonus": 5,
              "aiSkill": 2,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": false,
              "megaEvolutionChance": 0.0
            },
            "RARE": {
              "weight": 30,
              "levelBonus": 10,
              "aiSkill": 3,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": false,
              "megaEvolutionChance": 0.0
            },
            "EPIC": {
              "weight": 10,
              "levelBonus": 15,
              "aiSkill": 4,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 0.0
            },
            "LEGENDARY": {
              "weight": 5,
              "levelBonus": 20,
              "aiSkill": 5,
              "shinyChance": 0.25,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 0.5
            },
            "MYTHIC": {
              "weight": 1,
              "levelBonus": 25,
              "aiSkill": 5,
              "shinyChance": 0.45,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 1.0
            }
          }
        }
    """.trimIndent()

    private val TEMPLATE_PT_BR = """
        {
          // Versão da config. Deixa quieta.
          "configVersion": 2,

          // Roll de Boss. Padrão: 1/512.
          "bossSpawnChance": 0.001953125,

          // Cooldown em ticks. 20 ticks = 1s.
          "spawnCooldownTicks": 18000,

          // Labels do Cobblemon bloqueadas no roll.
          "excludedSpeciesLabels": ["legendary", "mythical", "ultra_beast"],

          // Nível máximo do Boss escalado.
          "maxScaledLevel": 200,

          // Intervalo do Enrage em Rare+. Avisa 1 turno antes.
          "enrageIntervalTurns": 8,

          // Estágios por Enrage. O cap da batalha é +6.
          "enrageStatStages": 1,

          // Scores/escolhas da IA no console. Bem barulhento.
          "bossAiDebugLogging": false,

          // Vida de Boss intocado. 12000 ticks = 10 min.
          "bossLifetimeTicks": 12000,

          // Odds do tier depois que o spawn vira Boss.
          "tiers": {
            "UNCOMMON": {
              "weight": 54,
              "levelBonus": 5,
              "aiSkill": 2,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": false,
              "megaEvolutionChance": 0.0
            },
            "RARE": {
              "weight": 30,
              "levelBonus": 10,
              "aiSkill": 3,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": false,
              "megaEvolutionChance": 0.0
            },
            "EPIC": {
              "weight": 10,
              "levelBonus": 15,
              "aiSkill": 4,
              "shinyChance": 0.0,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 0.0
            },
            "LEGENDARY": {
              "weight": 5,
              "levelBonus": 20,
              "aiSkill": 5,
              "shinyChance": 0.25,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 0.5
            },
            "MYTHIC": {
              "weight": 1,
              "levelBonus": 25,
              "aiSkill": 5,
              "shinyChance": 0.45,
              "guaranteedPerfectIVs": true,
              "megaEvolutionChance": 1.0
            }
          }
        }
    """.trimIndent()

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
        if (!CONFIG_FILE.exists()) {
            CONFIG_FILE.parentFile?.mkdirs()
            val isPortuguese = Locale.getDefault().language.equals("pt", ignoreCase = true)
            CONFIG_FILE.writeText(if (isPortuguese) TEMPLATE_PT_BR else TEMPLATE_EN)
            return WildBossesConfigData()
        }

        return try {
            val sourceText = CONFIG_FILE.readText()
            val stripped = stripLineComments(sourceText)
            val raw = GSON.fromJson(stripped, WildBossesConfigData::class.java) ?: WildBossesConfigData()
            sanitize(migrate(raw, sourceText))
        } catch (_: Exception) {
            WildBossesConfigData()
        }
    }

    private fun stripLineComments(json: String): String =
        json.lineSequence()
            .map { line -> if (line.trimStart().startsWith("//")) "" else line }
            .joinToString("\n")

    private fun migrate(raw: WildBossesConfigData, sourceText: String): WildBossesConfigData {
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
        }

        return raw
    }

    // Old configs can skip Kotlin defaults. Fix them once here.
    private fun sanitize(raw: WildBossesConfigData): WildBossesConfigData {
        val defaults = WildBossesConfigData()

        raw.configVersion = raw.configVersion.coerceAtLeast(2)
        raw.bossSpawnChance = raw.bossSpawnChance.coerceIn(0.0, 1.0)
        raw.spawnCooldownTicks = raw.spawnCooldownTicks.coerceAtLeast(0L)
        raw.maxScaledLevel = raw.maxScaledLevel.coerceIn(1, 1000)
        raw.enrageIntervalTurns = raw.enrageIntervalTurns.coerceAtLeast(1)
        raw.enrageStatStages = raw.enrageStatStages.coerceIn(0, 6)
        raw.bossLifetimeTicks = raw.bossLifetimeTicks.coerceAtLeast(20L)

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
            tiers = source.tiers.mapValues { (_, tier) -> tier.copy() }.toMutableMap()
        )

    fun tier(name: String): TierConfig =
        data.tiers[name] ?: error("Missing tier config for $name after sanitize()")
}
