package com.kaizzinho.wildbosses.config

import com.kaizzinho.wildbosses.boss.BossTier
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.util.Locale

class WildBossesConfigScreen private constructor(
    private val parent: Screen?,
    private val page: Page,
    private val working: WildBossesConfigData,
    private val selectedTierIndex: Int
) : Screen(Component.translatable("wildbosses.config.title")) {

    constructor(parent: Screen?) : this(
        parent,
        Page.GENERAL,
        WildBossesConfig.snapshot(),
        0
    )

    private enum class Page(val translationKey: String) {
        GENERAL("wildbosses.config.tab.general"),
        BATTLE("wildbosses.config.tab.battle"),
        SPECIES("wildbosses.config.tab.species"),
        TIERS("wildbosses.config.tab.tiers")
    }

    private var panelLeft = 0
    private var panelTop = 0
    private var panelWidth = 0
    private var panelHeight = 0
    private var contentTop = 0
    private var saveError = false

    override fun init() {
        panelWidth = (width - 24).coerceAtMost(430).coerceAtLeast(300)
        panelHeight = (height - 16).coerceAtMost(248).coerceAtLeast(218)
        panelLeft = (width - panelWidth) / 2
        panelTop = (height - panelHeight) / 2
        contentTop = panelTop + 62

        addTabs()

        when (page) {
            Page.GENERAL -> addGeneralPage()
            Page.BATTLE -> addBattlePage()
            Page.SPECIES -> addSpeciesPage()
            Page.TIERS -> addTiersPage()
        }

        addFooterButtons()
    }

    private fun addTabs() {
        val margin = 10
        val gap = 2
        val available = panelWidth - margin * 2
        val tabWidth = (available - gap * (Page.entries.size - 1)) / Page.entries.size
        val tabY = panelTop + 34

        Page.entries.forEachIndexed { index, candidate ->
            val x = panelLeft + margin + index * (tabWidth + gap)
            val button = Button.builder(Component.translatable(candidate.translationKey)) {
                if (page != candidate) {
                    minecraft?.setScreen(
                        WildBossesConfigScreen(
                            parent,
                            candidate,
                            working,
                            selectedTierIndex
                        )
                    )
                }
            }
                .bounds(x, tabY, tabWidth, 20)
                .build()

            button.active = page != candidate
            addRenderableWidget(button)
        }
    }

    private fun addGeneralPage() {
        addDoubleField(
            0,
            "wildbosses.config.option.boss_spawn_chance",
            working.bossSpawnChance * 100.0,
            0.0,
            100.0
        ) { working.bossSpawnChance = it / 100.0 }

        addLongField(
            1,
            "wildbosses.config.option.spawn_cooldown",
            ticksToSeconds(working.spawnCooldownTicks).toLong(),
            0L,
            86_400L
        ) { working.spawnCooldownTicks = secondsToTicks(it) }

        addLongField(
            2,
            "wildbosses.config.option.boss_lifetime",
            ticksToSeconds(working.bossLifetimeTicks).toLong(),
            1L,
            86_400L
        ) { working.bossLifetimeTicks = secondsToTicks(it) }

        addIntField(
            3,
            "wildbosses.config.option.max_scaled_level",
            working.maxScaledLevel,
            1,
            1000
        ) { working.maxScaledLevel = it }
    }

    private fun addBattlePage() {
        addIntField(
            0,
            "wildbosses.config.option.enrage_interval",
            working.enrageIntervalTurns,
            1,
            100
        ) { working.enrageIntervalTurns = it }

        addIntField(
            1,
            "wildbosses.config.option.enrage_stages",
            working.enrageStatStages,
            0,
            6
        ) { working.enrageStatStages = it }

        addBooleanOption(
            2,
            "wildbosses.config.option.boss_ai_debug",
            working.bossAiDebugLogging
        ) { working.bossAiDebugLogging = it }
    }

    private fun addSpeciesPage() {
        addStringField(
            0,
            "wildbosses.config.option.excluded_labels",
            working.excludedSpeciesLabels.joinToString(", ")
        ) { value ->
            val parsed = value.split(',')
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .distinct()

            if (parsed.isNotEmpty()) {
                working.excludedSpeciesLabels = parsed.toMutableList()
            }
        }
    }

    private fun addTiersPage() {
        val tier = selectedTier()
        val tierConfig = working.tiers.getValue(tier.name)

        addIntField(
            0,
            "wildbosses.config.option.tier_weight",
            tierConfig.weight,
            1,
            1000
        ) { tierConfig.weight = it }

        addIntField(
            1,
            "wildbosses.config.option.tier_level_bonus",
            tierConfig.levelBonus,
            0,
            1000
        ) { tierConfig.levelBonus = it }

        addIntField(
            2,
            "wildbosses.config.option.tier_ai_skill",
            tierConfig.aiSkill,
            0,
            5
        ) { tierConfig.aiSkill = it }

        addDoubleField(
            3,
            "wildbosses.config.option.tier_shiny_chance",
            tierConfig.shinyChance * 100.0,
            0.0,
            100.0
        ) { tierConfig.shinyChance = it / 100.0 }

        addBooleanOption(
            4,
            "wildbosses.config.option.tier_perfect_ivs",
            tierConfig.guaranteedPerfectIVs
        ) { tierConfig.guaranteedPerfectIVs = it }

        addDoubleField(
            5,
            "wildbosses.config.option.tier_mega_chance",
            tierConfig.megaEvolutionChance * 100.0,
            0.0,
            100.0
        ) { tierConfig.megaEvolutionChance = it / 100.0 }

        addTierNavigation()
    }

    private fun addTierNavigation() {
        val y = panelTop + 18
        val center = width / 2
        val arrowWidth = 20
        val gap = 72

        addRenderableWidget(
            Button.builder(Component.literal("<")) {
                openTier(selectedTierIndex - 1)
            }
                .bounds(center - gap - arrowWidth, y, arrowWidth, 14)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.literal(">")) {
                openTier(selectedTierIndex + 1)
            }
                .bounds(center + gap, y, arrowWidth, 14)
                .build()
        )
    }

    private fun openTier(index: Int) {
        val count = BossTier.entries.size
        val normalized = ((index % count) + count) % count
        minecraft?.setScreen(
            WildBossesConfigScreen(
                parent,
                Page.TIERS,
                working,
                normalized
            )
        )
    }

    private fun addFooterButtons() {
        val footerY = panelTop + panelHeight - 27
        val gap = 6
        val buttonWidth = 100
        val total = buttonWidth * 2 + gap
        val startX = width / 2 - total / 2

        addRenderableWidget(
            Button.builder(Component.translatable("wildbosses.config.save")) {
                saveError = !runCatching {
                    WildBossesConfig.save(working)
                }.isSuccess

                if (!saveError) {
                    minecraft?.setScreen(parent)
                }
            }
                .bounds(startX, footerY, buttonWidth, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel")) {
                minecraft?.setScreen(parent)
            }
                .bounds(startX + buttonWidth + gap, footerY, buttonWidth, 20)
                .build()
        )
    }

    private fun addBooleanOption(
        row: Int,
        translationKey: String,
        initial: Boolean,
        setter: (Boolean) -> Unit
    ) {
        var value = initial
        lateinit var button: Button

        button = Button.builder(booleanText(translationKey, value)) {
            value = !value
            setter(value)
            button.setMessage(booleanText(translationKey, value))
        }
            .bounds(optionX(), rowY(row), optionWidth(), 20)
            .build()

        addRenderableWidget(button)
    }

    private fun addIntField(
        row: Int,
        translationKey: String,
        initial: Int,
        min: Int,
        max: Int,
        setter: (Int) -> Unit
    ) {
        addEditField(row, translationKey, initial.toString(), 32) { text ->
            text.toIntOrNull()?.takeIf { it in min..max }?.let(setter)
        }
    }

    private fun addLongField(
        row: Int,
        translationKey: String,
        initial: Long,
        min: Long,
        max: Long,
        setter: (Long) -> Unit
    ) {
        addEditField(row, translationKey, initial.toString(), 32) { text ->
            text.toLongOrNull()?.takeIf { it in min..max }?.let(setter)
        }
    }

    private fun addDoubleField(
        row: Int,
        translationKey: String,
        initial: Double,
        min: Double,
        max: Double,
        setter: (Double) -> Unit
    ) {
        addEditField(row, translationKey, formatDecimal(initial), 32) { text ->
            text.replace(',', '.').toDoubleOrNull()
                ?.takeIf { it in min..max }
                ?.let(setter)
        }
    }

    private fun addStringField(
        row: Int,
        translationKey: String,
        initial: String,
        setter: (String) -> Unit
    ) {
        addEditField(row, translationKey, initial, 512, setter)
    }

    private fun addEditField(
        row: Int,
        translationKey: String,
        initial: String,
        maxLength: Int,
        responder: (String) -> Unit
    ) {
        val field = EditBox(
            font,
            fieldX(),
            rowY(row),
            fieldWidth(),
            20,
            Component.translatable(translationKey)
        )
        field.setValue(initial)
        field.setMaxLength(maxLength)
        field.setResponder(responder)
        addRenderableWidget(field)
    }

    override fun render(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        graphics.fill(0, 0, width, height, 0x66090A0E)
        graphics.fill(
            panelLeft,
            panelTop,
            panelLeft + panelWidth,
            panelTop + panelHeight,
            0xE8141418.toInt()
        )
        graphics.fill(
            panelLeft,
            panelTop,
            panelLeft + panelWidth,
            panelTop + 1,
            0xFFB8B8C8.toInt()
        )
        graphics.fill(
            panelLeft,
            panelTop + panelHeight - 1,
            panelLeft + panelWidth,
            panelTop + panelHeight,
            0xFF50505C.toInt()
        )

        drawCentered(graphics, title, panelTop + 10, 0xFFFFFFFF.toInt(), true)
        drawCentered(graphics, pageSubtitle(), panelTop + 23, 0xFFB9B9C4.toInt(), false)

        renderFieldLabels(graphics)
        super.render(graphics, mouseX, mouseY, partialTick)

        if (saveError) {
            drawCentered(
                graphics,
                Component.translatable("wildbosses.config.save_failed"),
                panelTop + panelHeight - 39,
                0xFFFF5555.toInt(),
                true
            )
        }
    }

    override fun renderBackground(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        // no blur so old screens never bleed through
    }

    override fun renderBlurredBackground(partialTick: Float) {
        // keep blur off here too
    }

    private fun renderFieldLabels(graphics: GuiGraphics) {
        val keys = when (page) {
            Page.GENERAL -> listOf(
                "wildbosses.config.option.boss_spawn_chance",
                "wildbosses.config.option.spawn_cooldown",
                "wildbosses.config.option.boss_lifetime",
                "wildbosses.config.option.max_scaled_level"
            )
            Page.BATTLE -> listOf(
                "wildbosses.config.option.enrage_interval",
                "wildbosses.config.option.enrage_stages"
            )
            Page.SPECIES -> listOf(
                "wildbosses.config.option.excluded_labels"
            )
            Page.TIERS -> listOf(
                "wildbosses.config.option.tier_weight",
                "wildbosses.config.option.tier_level_bonus",
                "wildbosses.config.option.tier_ai_skill",
                "wildbosses.config.option.tier_shiny_chance",
                null,
                "wildbosses.config.option.tier_mega_chance"
            )
        }

        keys.forEachIndexed { row, key ->
            if (key != null) {
                graphics.drawString(
                    font,
                    Component.translatable(key),
                    optionX(),
                    rowY(row) + 6,
                    0xFFE0E0E0.toInt(),
                    false
                )
            }
        }
    }

    private fun pageSubtitle(): Component = when (page) {
        Page.GENERAL -> Component.translatable("wildbosses.config.page.general")
        Page.BATTLE -> Component.translatable("wildbosses.config.page.battle")
        Page.SPECIES -> Component.translatable("wildbosses.config.page.species")
        Page.TIERS -> Component.translatable(
            "wildbosses.config.page.tiers",
            Component.translatable("wildbosses.config.tier.${selectedTier().name.lowercase()}")
        )
    }

    private fun selectedTier(): BossTier = BossTier.entries[
        selectedTierIndex.coerceIn(0, BossTier.entries.lastIndex)
    ]

    private fun booleanText(key: String, value: Boolean): Component =
        Component.translatable(key)
            .append(": ")
            .append(Component.translatable(if (value) "options.on" else "options.off"))

    private fun optionX(): Int = panelLeft + 24

    private fun optionWidth(): Int = panelWidth - 48

    private fun fieldWidth(): Int = (optionWidth() * 0.38).toInt().coerceAtLeast(112)

    private fun fieldX(): Int = optionX() + optionWidth() - fieldWidth()

    private fun rowY(row: Int): Int = contentTop + row * 23

    private fun drawCentered(
        graphics: GuiGraphics,
        text: Component,
        y: Int,
        color: Int,
        shadow: Boolean
    ) {
        val x = width / 2 - font.width(text) / 2
        graphics.drawString(font, text, x, y, color, shadow)
    }

    private fun formatDecimal(value: Double): String =
        String.format(Locale.ROOT, "%.8f", value)
            .trimEnd('0')
            .trimEnd('.')

    private fun ticksToSeconds(ticks: Long): Int =
        (ticks / 20L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    private fun secondsToTicks(seconds: Long): Long = seconds * 20L

    override fun onClose() {
        minecraft?.setScreen(parent)
    }
}
