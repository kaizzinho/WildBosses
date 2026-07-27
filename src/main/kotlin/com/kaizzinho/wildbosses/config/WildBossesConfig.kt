package com.kaizzinho.wildbosses.config

import com.google.gson.GsonBuilder
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
    var bossSpawnChance: Double = 1.0 / 512,
    var spawnCooldownTicks: Long = 18000L,
    var excludedSpeciesLabels: MutableList<String> = mutableListOf("legendary", "mythical", "ultra_beast"),
    var maxScaledLevel: Int = 200,
    var enrageIntervalTurns: Int = 8,
    var enrageStatStages: Int = 1,
    var bossLifetimeTicks: Long = 12000L,
    var tiers: MutableMap<String, TierConfig> = defaultTiers()
)

private fun defaultTiers(): MutableMap<String, TierConfig> = mutableMapOf(
    "UNCOMMON" to TierConfig(weight = 54, levelBonus = 15, aiSkill = 2, shinyChance = 0.0, guaranteedPerfectIVs = false, megaEvolutionChance = 0.0),
    "RARE" to TierConfig(weight = 30, levelBonus = 25, aiSkill = 3, shinyChance = 0.0, guaranteedPerfectIVs = false, megaEvolutionChance = 0.0),
    "EPIC" to TierConfig(weight = 10, levelBonus = 35, aiSkill = 4, shinyChance = 0.0, guaranteedPerfectIVs = true, megaEvolutionChance = 0.0),
    "LEGENDARY" to TierConfig(weight = 5, levelBonus = 45, aiSkill = 5, shinyChance = 0.25, guaranteedPerfectIVs = true, megaEvolutionChance = 0.50),
    "MYTHIC" to TierConfig(weight = 1, levelBonus = 55, aiSkill = 5, shinyChance = 0.45, guaranteedPerfectIVs = true, megaEvolutionChance = 1.0)
)

    object WildBossesConfig {
        private val GSON = GsonBuilder().setPrettyPrinting().create()

        // Config now lives in its own subfolder: config/wildbosses/wildbosses.json
        private val CONFIG_FILE = FabricLoader.getInstance().configDir
            .resolve("wildbosses")
            .resolve("wildbosses.json")
            .toFile()

        // IMPORTANT: declared BEFORE `data` below - Kotlin object properties initialize
        // top-to-bottom, and `data`'s initializer calls load(), which reads these templates.

        private val TEMPLATE_EN = """
    {
      // Chance (0.0 to 1.0) that ANY wild Pokémon spawn attempt becomes a boss instead.
      // Default 1/512 ≈ 0.00195.
      "bossSpawnChance": 0.001953125,

      // Ticks (20 ticks = 1 second) a player must wait after a boss spawns targeting them
      // before another boss can spawn targeting them again. Default 18000 = 15 minutes.
      // Does NOT stop them from engaging a boss that spawned for someone else nearby.
      "spawnCooldownTicks": 18000,

      // Species carrying any of these Cobblemon labels can NEVER become a boss.
      "excludedSpeciesLabels": ["legendary", "mythical", "ultra_beast"],

      // Hard ceiling on a boss's scaled battle level. Raise with caution - very high values
      // are outside what Cobblemon's own battle engine has been tested against.
      "maxScaledLevel": 200,

      // A boss enrages (permanent stat boost) every N turns a fight lasts. Also controls
      // the "is gathering power..." warning shown one turn before each enrage.
      "enrageIntervalTurns": 8,

      // Stat stages (1 stage = +50%) added to ALL of a boss's stats per enrage. Real stat
      // stages cap at +6 total regardless of this setting.
      "enrageStatStages": 1,

      // Ticks (20 ticks = 1 second) a boss roams before auto-despawning if never engaged.
      // Default 12000 = 10 minutes.
      "bossLifetimeTicks": 12000,

      // Per-tier settings. Do not rename, remove, or add tier keys - exactly these 5 must exist.
      // "weight" values are relative odds - they don't need to sum to 100, but this project's
      // defaults do, making each weight directly readable as a percentage chance.
      "tiers": {
        "UNCOMMON": {
          "weight": 54,
          "levelBonus": 15,
          "aiSkill": 2,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": false,
          "megaEvolutionChance": 0.0
        },
        "RARE": {
          "weight": 30,
          "levelBonus": 25,
          "aiSkill": 3,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": false,
          "megaEvolutionChance": 0.0
        },
        "EPIC": {
          "weight": 10,
          "levelBonus": 35,
          "aiSkill": 4,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": true,
          "megaEvolutionChance": 0.0
        },
        "LEGENDARY": {
          "weight": 5,
          "levelBonus": 45,
          "aiSkill": 5,
          "shinyChance": 0.25,
          "guaranteedPerfectIVs": true,
          "megaEvolutionChance": 0.5
        },
        "MYTHIC": {
          "weight": 1,
          "levelBonus": 55,
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
      // Chance (0.0 a 1.0) de QUALQUER spawn de Pokémon selvagem virar um chefe.
      // Padrão 1/512 ≈ 0,00195.
      "bossSpawnChance": 0.001953125,

      // Ticks (20 ticks = 1 segundo) que um jogador deve esperar depois que um chefe
      // aparece por perto antes que outro chefe possa spawnar de novo.
      // Padrão 18000 = 15 minutos. NÃO impede o jogador de enfrentar um chefe que
      // spawnou para outra pessoa por perto.
      "spawnCooldownTicks": 18000,

      // Espécies com qualquer uma dessas categorias do Cobblemon NUNCA podem virar chefe.
      "excludedSpeciesLabels": ["legendary", "mythical", "ultra_beast"],

      // Teto máximo do nível escalado de um chefe em batalha. Aumente com cuidado -
      // valores muito altos não foram testados pelo próprio motor de batalha do Cobblemon.
      "maxScaledLevel": 200,

      // Um chefe enfurece (ganha bônus de status) a cada N turnos de luta.
      // Também controla o aviso "está acumulando poder...", mostrado um turno antes.
      "enrageIntervalTurns": 8,

      // Estágios de status (1 estágio = +50%) somados em TODOS os status do chefe a
      // cada enfurecimento. Estágios de status reais têm limite de +6 no total.
      "enrageStatStages": 1,

      // Ticks (20 ticks = 1 segundo) que um chefe fica no mundo antes de desaparecer
      // sozinho se nunca for enfrentado. Padrão 12000 = 10 minutos.
      "bossLifetimeTicks": 12000,

      // Configurações por tier. Não renomeie, remova ou adicione chaves de tier -
      // devem existir exatamente estas 5. Os valores de "weight" são chances relativas -
      // não precisam somar 100, mas os padrões deste projeto somam, então cada peso
      // já pode ser lido diretamente como uma porcentagem.
      "tiers": {
        "UNCOMMON": {
          "weight": 54,
          "levelBonus": 15,
          "aiSkill": 2,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": false,
          "megaEvolutionChance": 0.0
        },
        "RARE": {
          "weight": 30,
          "levelBonus": 25,
          "aiSkill": 3,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": false,
          "megaEvolutionChance": 0.0
        },
        "EPIC": {
          "weight": 10,
          "levelBonus": 35,
          "aiSkill": 4,
          "shinyChance": 0.0,
          "guaranteedPerfectIVs": true,
          "megaEvolutionChance": 0.0
        },
        "LEGENDARY": {
          "weight": 5,
          "levelBonus": 45,
          "aiSkill": 5,
          "shinyChance": 0.25,
          "guaranteedPerfectIVs": true,
          "megaEvolutionChance": 0.5
        },
        "MYTHIC": {
          "weight": 1,
          "levelBonus": 55,
          "aiSkill": 5,
          "shinyChance": 0.45,
          "guaranteedPerfectIVs": true,
          "megaEvolutionChance": 1.0
        }
      }
    }
    """.trimIndent()

        // `var` + private set, so reload() (Point 2) can swap in a freshly-read copy at runtime.
        var data: WildBossesConfigData = load()
            private set

        /** Re-reads and re-validates the config file from disk. Used by /wb reloadconfig. */
        fun reload() {
            data = load()
        }

        private fun load(): WildBossesConfigData {
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.parentFile?.mkdirs()
                val isPortuguese = Locale.getDefault().language.equals("pt", ignoreCase = true)
                CONFIG_FILE.writeText(if (isPortuguese) TEMPLATE_PT_BR else TEMPLATE_EN)
                return WildBossesConfigData()
            }
            val raw = try {
                val stripped = stripLineComments(CONFIG_FILE.readText())
                GSON.fromJson(stripped, WildBossesConfigData::class.java) ?: WildBossesConfigData()
            } catch (e: Exception) {
                WildBossesConfigData()
            }
            return sanitize(raw)
        }

        private fun stripLineComments(json: String): String =
            json.lineSequence()
                .map { line -> if (line.trimStart().startsWith("//")) "" else line }
                .joinToString("\n")

        /**
         * Clamps every value to a safe range and fills in anything missing. Necessary because
         * Gson constructs objects via reflection, bypassing Kotlin's default parameter values
         * entirely - a field genuinely absent from the JSON file comes back null/0, not the
         * declared default. Without this, a hand-edited or partially-outdated config file could
         * crash the server (e.g. a zero tier weight throwing inside Random.nextInt, or a missing
         * tier entry throwing in tier()).
         */
        private fun sanitize(raw: WildBossesConfigData): WildBossesConfigData {
            val defaults = WildBossesConfigData()

            raw.bossSpawnChance = raw.bossSpawnChance.coerceIn(0.0, 1.0)
            raw.spawnCooldownTicks = raw.spawnCooldownTicks.coerceAtLeast(0L)
            raw.maxScaledLevel = raw.maxScaledLevel.coerceIn(1, 1000)
            raw.enrageIntervalTurns = raw.enrageIntervalTurns.coerceAtLeast(1)
            raw.enrageStatStages = raw.enrageStatStages.coerceIn(0, 6)
            raw.bossLifetimeTicks = raw.bossLifetimeTicks.coerceAtLeast(20L)

            if (raw.excludedSpeciesLabels.isEmpty() && defaults.excludedSpeciesLabels.isNotEmpty()) {
                // An empty list here is ambiguous (deliberate choice vs. a field Gson never
                // populated) - only refill from defaults if it's genuinely empty, never overwrite
                // a real custom list the admin intentionally shortened.
                raw.excludedSpeciesLabels = defaults.excludedSpeciesLabels
            }

            val defaultTiersMap = defaultTiers()
            val sanitizedTiers = mutableMapOf<String, TierConfig>()
            for ((tierName, defaultTier) in defaultTiersMap) {
                val existing = raw.tiers[tierName]
                sanitizedTiers[tierName] = if (existing == null) {
                    defaultTier // whole tier entry missing entirely - use the built-in default
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

        fun tier(name: String): TierConfig =
            data.tiers[name]
                ?: error("Missing tier config entry for $name - this should be unreachable after sanitize()")
    }
