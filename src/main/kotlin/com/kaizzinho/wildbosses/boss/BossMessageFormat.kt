package com.kaizzinho.wildbosses.boss

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack

object BossMessageFormat {

    private val QUANTITY_COLOR = ChatFormatting.BLUE

    fun tierName(tier: BossTier): Component =
        Component.translatable("wildbosses.tier.${tier.name.lowercase()}")

    fun bossName(tier: BossTier, speciesName: String): MutableComponent =
        Component.translatable("wildbosses.message.boss_name", tierName(tier), speciesName)
            .withStyle(tier.color)

    fun plain(text: String): Component = Component.literal(text).withStyle(ChatFormatting.WHITE)

    fun plainKey(key: String, vararg args: Any): Component =
        Component.translatable(key, *args).withStyle(ChatFormatting.WHITE)

    fun levelValue(tier: BossTier, level: Int): Component =
        Component.literal("$level").withStyle(tier.color, ChatFormatting.BOLD)

    fun lootItem(stack: ItemStack): Component =
        Component.translatable("wildbosses.message.loot_item", stack.count)
            .withStyle(QUANTITY_COLOR, ChatFormatting.BOLD)
            .append(Component.literal(" ").withStyle(ChatFormatting.WHITE).append(stack.hoverName))

    fun build(content: Component): Component =
        Component.translatable("wildbosses.prefix").withStyle(ChatFormatting.GRAY).append(content)
}
