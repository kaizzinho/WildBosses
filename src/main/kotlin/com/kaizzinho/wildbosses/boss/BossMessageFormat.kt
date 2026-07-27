package com.kaizzinho.wildbosses.boss

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack

object BossMessageFormat {

    // Neutral color for loot quantities - deliberately none of the 5 tier colors
    // (GREEN/AQUA/LIGHT_PURPLE/GOLD/YELLOW), so it never gets confused for a tier indicator.
    private val QUANTITY_COLOR = ChatFormatting.BLUE

    /** The tier's display name, translated (Uncommon -> Incomum, etc). */
    fun tierName(tier: BossTier): Component =
        Component.translatable("wildbosses.tier.${tier.name.lowercase()}")

    /**
     * "[Tier] Boss [Species]" as one styled unit, colored in the tier's color.
     * The whole phrase is a single translation key so other languages can reorder it freely -
     * PT-BR puts the noun first ("Chefe Epico Pidgeot"), which string concatenation couldn't handle.
     */
    fun bossName(tier: BossTier, speciesName: String): MutableComponent =
        Component.translatable("wildbosses.message.boss_name", tierName(tier), speciesName)
            .withStyle(tier.color)

    /** Plain white text, explicitly reset so it never inherits a preceding component's color. */
    fun plain(text: String): Component = Component.literal(text).withStyle(ChatFormatting.WHITE)

    /** A translated snippet in plain white, for message bodies that follow a colored boss name. */
    fun plainKey(key: String, vararg args: Any): Component =
        Component.translatable(key, *args).withStyle(ChatFormatting.WHITE)

    /** A level value, bolded and tier-colored to stand out from surrounding plain text. */
    fun levelValue(tier: BossTier, level: Int): Component =
        Component.literal("$level").withStyle(tier.color, ChatFormatting.BOLD)

    /** One loot item, its quantity highlighted in a neutral color, name left plain. */
    fun lootItem(stack: ItemStack): Component =
        Component.translatable("wildbosses.message.loot_item", stack.count)
            .withStyle(QUANTITY_COLOR, ChatFormatting.BOLD)
            .append(Component.literal(" ").withStyle(ChatFormatting.WHITE).append(stack.hoverName))

    /** Prepends the standard "[WildBosses] " prefix to a message body. */
    fun build(content: Component): Component =
        Component.translatable("wildbosses.prefix").withStyle(ChatFormatting.GRAY).append(content)
}
