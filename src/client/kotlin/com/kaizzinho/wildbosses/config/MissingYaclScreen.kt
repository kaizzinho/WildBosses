package com.kaizzinho.wildbosses.config

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class MissingYaclScreen(private val parent: Screen) : Screen(Component.translatable("wildbosses.config.missing_yacl.title")) {
    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("gui.back")) { minecraft?.setScreen(parent) }
                .bounds(width / 2 - 100, height / 2 + 34, 200, 20)
                .build()
        )
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 28, 0xFFFFFF)
        graphics.drawCenteredString(
            font,
            Component.translatable("wildbosses.config.missing_yacl.body"),
            width / 2,
            height / 2,
            0xA0A0A0
        )
    }
}
