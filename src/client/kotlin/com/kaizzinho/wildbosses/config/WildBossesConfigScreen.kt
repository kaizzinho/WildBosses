package com.kaizzinho.wildbosses.config

import com.kaizzinho.wildbosses.boss.BossTier
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object WildBossesConfigScreen {
    @JvmStatic
    fun create(parent: Screen): Screen {
        val draft = WildBossesConfig.snapshot()
        val defaults = WildBossesConfig.defaults()

        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("wildbosses.config.title"))
            .category(generalCategory(draft, defaults))
            .category(battleCategory(draft, defaults))
            .category(speciesCategory(draft, defaults))
            .category(tiersCategory(draft, defaults))
            .save { WildBossesConfig.save(draft) }
            .build()
            .generateScreen(parent)
    }

    private fun generalCategory(draft: WildBossesConfigData, defaults: WildBossesConfigData): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("wildbosses.config.category.general"))
            .tooltip(Component.translatable("wildbosses.config.category.general.desc"))
            .option(doubleField(
                "boss_spawn_chance",
                defaults.bossSpawnChance * 100.0,
                { draft.bossSpawnChance * 100.0 },
                { draft.bossSpawnChance = it / 100.0 },
                0.0,
                100.0
            ))
            .option(integerField(
                "spawn_cooldown",
                ticksToSeconds(defaults.spawnCooldownTicks),
                { ticksToSeconds(draft.spawnCooldownTicks) },
                { draft.spawnCooldownTicks = secondsToTicks(it) },
                0,
                86_400
            ))
            .option(integerField(
                "boss_lifetime",
                ticksToSeconds(defaults.bossLifetimeTicks),
                { ticksToSeconds(draft.bossLifetimeTicks) },
                { draft.bossLifetimeTicks = secondsToTicks(it.coerceAtLeast(1)) },
                1,
                86_400
            ))
            .option(integerField(
                "max_scaled_level",
                defaults.maxScaledLevel,
                { draft.maxScaledLevel },
                { draft.maxScaledLevel = it },
                1,
                1000
            ))
            .build()

    private fun battleCategory(draft: WildBossesConfigData, defaults: WildBossesConfigData): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("wildbosses.config.category.battle"))
            .tooltip(Component.translatable("wildbosses.config.category.battle.desc"))
            .option(integerField(
                "enrage_interval",
                defaults.enrageIntervalTurns,
                { draft.enrageIntervalTurns },
                { draft.enrageIntervalTurns = it },
                1,
                100
            ))
            .option(integerSlider(
                "enrage_stages",
                defaults.enrageStatStages,
                { draft.enrageStatStages },
                { draft.enrageStatStages = it },
                0,
                6
            ))
            .option(booleanOption(
                "boss_ai_debug",
                defaults.bossAiDebugLogging,
                { draft.bossAiDebugLogging },
                { draft.bossAiDebugLogging = it }
            ))
            .build()

    private fun speciesCategory(draft: WildBossesConfigData, defaults: WildBossesConfigData): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("wildbosses.config.category.species"))
            .tooltip(Component.translatable("wildbosses.config.category.species.desc"))
            .option(Option.createBuilder<String>()
                .name(Component.translatable("wildbosses.config.option.excluded_labels"))
                .description(description("excluded_labels"))
                .binding(
                    defaults.excludedSpeciesLabels.joinToString(", "),
                    { draft.excludedSpeciesLabels.joinToString(", ") },
                    { value ->
                        draft.excludedSpeciesLabels = value.split(',')
                            .map { it.trim().lowercase() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .toMutableList()
                    }
                )
                .controller(StringControllerBuilder::create)
                .build())
            .build()

    private fun tiersCategory(draft: WildBossesConfigData, defaults: WildBossesConfigData): ConfigCategory {
        val builder = ConfigCategory.createBuilder()
            .name(Component.translatable("wildbosses.config.category.tiers"))
            .tooltip(Component.translatable("wildbosses.config.category.tiers.desc"))

        BossTier.entries.forEach { tier ->
            val key = tier.name
            val current = draft.tiers.getValue(key)
            val default = defaults.tiers.getValue(key)
            builder.group(tierGroup(tier, current, default))
        }

        return builder.build()
    }

    private fun tierGroup(tier: BossTier, current: TierConfig, default: TierConfig): OptionGroup =
        OptionGroup.createBuilder()
            .name(Component.translatable("wildbosses.config.tier.${tier.name.lowercase()}"))
            .description(OptionDescription.of(Component.translatable("wildbosses.config.tier.desc")))
            .collapsed(true)
            .option(integerField(
                "tier_weight",
                default.weight,
                { current.weight },
                { current.weight = it },
                1,
                1000
            ))
            .option(integerField(
                "tier_level_bonus",
                default.levelBonus,
                { current.levelBonus },
                { current.levelBonus = it },
                0,
                1000
            ))
            .option(integerSlider(
                "tier_ai_skill",
                default.aiSkill,
                { current.aiSkill },
                { current.aiSkill = it },
                0,
                5
            ))
            .option(doubleField(
                "tier_shiny_chance",
                default.shinyChance * 100.0,
                { current.shinyChance * 100.0 },
                { current.shinyChance = it / 100.0 },
                0.0,
                100.0
            ))
            .option(booleanOption(
                "tier_perfect_ivs",
                default.guaranteedPerfectIVs,
                { current.guaranteedPerfectIVs },
                { current.guaranteedPerfectIVs = it }
            ))
            .option(doubleField(
                "tier_mega_chance",
                default.megaEvolutionChance * 100.0,
                { current.megaEvolutionChance * 100.0 },
                { current.megaEvolutionChance = it / 100.0 },
                0.0,
                100.0
            ))
            .build()

    private fun integerField(
        key: String,
        default: Int,
        getter: () -> Int,
        setter: (Int) -> Unit,
        min: Int,
        max: Int
    ): Option<Int> = Option.createBuilder<Int>()
        .name(Component.translatable("wildbosses.config.option.$key"))
        .description(description(key))
        .binding(default, getter, setter)
        .controller { option -> IntegerFieldControllerBuilder.create(option).range(min, max) }
        .build()

    private fun integerSlider(
        key: String,
        default: Int,
        getter: () -> Int,
        setter: (Int) -> Unit,
        min: Int,
        max: Int
    ): Option<Int> = Option.createBuilder<Int>()
        .name(Component.translatable("wildbosses.config.option.$key"))
        .description(description(key))
        .binding(default, getter, setter)
        .controller { option -> IntegerSliderControllerBuilder.create(option).range(min, max).step(1) }
        .build()

    private fun doubleField(
        key: String,
        default: Double,
        getter: () -> Double,
        setter: (Double) -> Unit,
        min: Double,
        max: Double
    ): Option<Double> = Option.createBuilder<Double>()
        .name(Component.translatable("wildbosses.config.option.$key"))
        .description(description(key))
        .binding(default, getter, setter)
        .controller { option -> DoubleFieldControllerBuilder.create(option).range(min, max) }
        .build()

    private fun booleanOption(
        key: String,
        default: Boolean,
        getter: () -> Boolean,
        setter: (Boolean) -> Unit
    ): Option<Boolean> = Option.createBuilder<Boolean>()
        .name(Component.translatable("wildbosses.config.option.$key"))
        .description(description(key))
        .binding(default, getter, setter)
        .controller(TickBoxControllerBuilder::create)
        .build()

    private fun description(key: String): OptionDescription =
        OptionDescription.of(Component.translatable("wildbosses.config.option.$key.desc"))

    private fun ticksToSeconds(ticks: Long): Int = (ticks / 20L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    private fun secondsToTicks(seconds: Int): Long = seconds.toLong() * 20L
}
