package com.kaizzinho.wildbosses.command

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.spawn.BossSpawnListener
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

object WildBossCommands {

    private val NOT_A_BOSS = SimpleCommandExceptionType(Component.literal("That entity is not a WildBosses boss."))
    private val INVALID_TIER = SimpleCommandExceptionType(Component.literal("Invalid tier. Valid tiers: uncommon, rare, epic, legendary, mythic."))
    private data class HelpEntry(val usage: String, val description: String)

    private val HELP_ENTRIES = listOf(
        HelpEntry("/wb spawn <tier> [species] [respectCooldown]", "Force-spawns a boss. respectCooldown defaults to false (bypasses your cooldown for testing); set to true to test the real player-facing gate."),
        HelpEntry("/wb list", "Lists every currently active boss with species, tier, position, and UUID."),
        HelpEntry("/wb info <target>", "Shows a boss's tier, level, shiny status, and position."),
        HelpEntry("/wb teleport <target>", "Teleports you to the given boss."),
        HelpEntry("/wb despawn <target>", "Force-despawns a specific boss and cleans up its registry/glow."),
        HelpEntry("/wb forcebattle <target>", "Immediately starts a battle with the given boss."),
        HelpEntry("/wb loot <tier>", "Gives you the items from a tier's loot table directly, bypassing combat."),
        HelpEntry("/wb setlevel <target> <level>", "Manually overrides a boss's level (1-200)."),
        HelpEntry("/wb reload", "Reloads datapacks (e.g. after editing a loot table JSON)."),
        HelpEntry("/wb glow <target>", "Re-applies tier glow/team color to a boss."),
        HelpEntry("/wb tier <target> <tier>", "Changes an already-spawned boss's tier (updates tags, glow, and registry)."),
        HelpEntry("/wb killall", "Despawns every active boss and clears the registry."),
        HelpEntry("/wb version", "Shows the mod version."),
        HelpEntry("/wb help", "Shows this list."),
        HelpEntry("/wb cooldown", "Checks whether you're currently on boss-spawn cooldown."),
        HelpEntry("/wb leaderboard [tier]", "Shows the top 10 boss hunters overall, or for a specific tier.")
    )


    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("wildbosses")
                    .then(Commands.literal("spawn")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("tier", StringArgumentType.word())
                            .executes { ctx -> spawnBoss(ctx, null, false) }
                            .then(Commands.argument("species", StringArgumentType.word())
                                .executes { ctx -> spawnBoss(ctx, StringArgumentType.getString(ctx, "species"), false) }
                                .then(Commands.argument("respectCooldown", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                                    .executes { ctx ->
                                        spawnBoss(
                                            ctx,
                                            StringArgumentType.getString(ctx, "species"),
                                            com.mojang.brigadier.arguments.BoolArgumentType.getBool(ctx, "respectCooldown")
                                        )
                                    }
                                )
                            )
                        )
                    )
                    .then(Commands.literal("list")
                        .requires { it.hasPermission(2) }
                        .executes { ctx -> listBosses(ctx) }
                    )
                    .then(Commands.literal("info")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> bossInfo(ctx) }
                        )
                    )
                    .then(Commands.literal("teleport")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> teleportToBoss(ctx) }
                        )
                    )
                    .then(Commands.literal("help")
                        .executes { ctx -> showHelp(ctx) }
                    )
                    .then(Commands.literal("despawn")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> despawnBoss(ctx) }
                        )
                    )
                    .then(Commands.literal("forcebattle")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> forceBattle(ctx) }
                        )
                    )
                    .then(Commands.literal("loot")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("tier", StringArgumentType.word())
                            .executes { ctx -> giveLoot(ctx) }
                        )
                    )
                    .then(Commands.literal("leaderboard")
                        .executes { ctx -> showLeaderboard(ctx, null) }
                        .then(Commands.argument("tier", StringArgumentType.word())
                            .executes { ctx -> showLeaderboard(ctx, StringArgumentType.getString(ctx, "tier")) }
                        )
                    )
                    .then(Commands.literal("cooldown")
                        .requires { it.hasPermission(2) }
                        .executes { ctx -> checkCooldown(ctx) }
                    )
                    .then(Commands.literal("setlevel")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 200))
                                .executes { ctx -> setBossLevel(ctx) }
                            )
                        )
                    )
                    .then(Commands.literal("reload")
                        .requires { it.hasPermission(2) }
                        .executes { ctx -> reloadDatapacks(ctx) }
                    )
                    .then(Commands.literal("glow")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> reapplyGlow(ctx) }
                        )
                    )
                    .then(Commands.literal("tier")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .then(Commands.argument("tier", StringArgumentType.word())
                                .executes { ctx -> setBossTier(ctx) }
                            )
                        )
                    )
                    .then(Commands.literal("killall")
                        .requires { it.hasPermission(2) }
                        .executes { ctx -> killAllBosses(ctx) }
                    )
                    .then(Commands.literal("version")
                        .executes { ctx -> showVersion(ctx) }
                    )
            )
            dispatcher.register(
                Commands.literal("wb").redirect(dispatcher.root.getChild("wildbosses"))
            )
        }
    }

    private fun parseTier(name: String): BossTier =
        BossTier.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: throw INVALID_TIER.create()

    private fun getBossEntity(ctx: CommandContext<CommandSourceStack>): PokemonEntity {
        val entity = EntityArgument.getEntity(ctx, "target")
        if (entity !is PokemonEntity || !BossRegistry.isBoss(entity.uuid)) throw NOT_A_BOSS.create()
        return entity
    }

    // --- /wildbosses spawn <tier> [species] ---
    private fun spawnBoss(ctx: CommandContext<CommandSourceStack>, speciesOverride: String?, respectCooldown: Boolean): Int {
        val source = ctx.source
        val player = source.playerOrException
        val tier = parseTier(StringArgumentType.getString(ctx, "tier"))

        val speciesString = speciesOverride ?: "pikachu"
        val properties = PokemonProperties.parse(speciesString)
        val entity = properties.createEntity(source.level)
        entity.moveTo(player.x, player.y, player.z, player.yRot, player.xRot)
        source.level.addFreshEntity(entity)

        BossSpawnListener.forcePromote(entity, tier, player, respectCooldown)

        source.sendSuccess({ Component.literal("Spawned a ${tier.name} boss (${entity.pokemon.species.name}) at your location.") }, false)
        return 1
    }

    // --- /wildbosses list ---
    private fun listBosses(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val bosses = BossRegistry.allBosses()
        if (bosses.isEmpty()) {
            source.sendSuccess({ Component.literal("No active bosses.") }, false)
            return 0
        }
        source.sendSuccess({ Component.literal("Active bosses (${bosses.size}):") }, false)
        for (instance in bosses) {
            val pos = instance.lastKnownPos
            val posText = if (pos != null) "(${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()})" else "(unknown pos)"
            source.sendSuccess({
                Component.literal("- ${instance.speciesName ?: "?"} [${instance.tier.name}] $posText uuid=${instance.entityUuid}")
            }, false)
        }
        return bosses.size
    }

    // --- /wildbosses info <target> ---
    private fun bossInfo(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val instance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()
        ctx.source.sendSuccess({
            Component.literal(
                "${instance.speciesName ?: entity.pokemon.species.name} [${instance.tier.name}] " +
                        "level=${entity.pokemon.level} (override=${instance.currentLevelOverride}) " +
                        "shiny=${entity.pokemon.shiny} pos=${entity.blockPosition()} uuid=${entity.uuid}"
            )
        }, false)
        return 1
    }
    // --- /wildbosses help ---
    private fun showHelp(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        source.sendSuccess({ Component.literal("=== WildBosses Commands ===") }, false)
        for (entry in HELP_ENTRIES) {
            source.sendSuccess({
                Component.literal(entry.usage).withStyle(net.minecraft.ChatFormatting.AQUA)
                    .append(Component.literal(" - ${entry.description}"))
            }, false)
        }
        return HELP_ENTRIES.size
    }

    // --- /wildbosses teleport <target> ---
    private fun teleportToBoss(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val player = ctx.source.playerOrException
        player.teleportTo(entity.x, entity.y, entity.z)
        ctx.source.sendSuccess({ Component.literal("Teleported to ${entity.pokemon.species.name}.") }, false)
        return 1
    }

    // --- /wildbosses leaderboard [tier] ---
    private fun showLeaderboard(ctx: CommandContext<CommandSourceStack>, tierName: String?): Int {
        val source = ctx.source
        val overworld = source.server.overworld()
        val data = com.kaizzinho.wildbosses.boss.BossKillLeaderboardData.get(overworld)

        if (tierName == null) {
            val top = data.topByTotal(10)
            source.sendSuccess({ Component.literal("=== Top Boss Hunters (Total) ===") }, false)
            top.forEachIndexed { i, (record, count) ->
                source.sendSuccess({ Component.literal("${i + 1}. ${record.playerName} - $count kill(s)") }, false)
            }
            return top.size
        }

        val tier = parseTier(tierName)
        val top = data.topByTier(tier, 10)
        source.sendSuccess({ Component.literal("=== Top Boss Hunters (${tier.name}) ===") }, false)
        top.forEachIndexed { i, (record, count) ->
            source.sendSuccess({ Component.literal("${i + 1}. ${record.playerName} - $count kill(s)") }, false)
        }
        return top.size
    }

    // --- /wildbosses despawn <target> ---
    private fun despawnBoss(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val server = ctx.source.server
        server.scoreboard.removePlayerFromTeam(entity.uuid.toString())
        BossRegistry.unregister(entity.uuid)
        entity.discard()
        ctx.source.sendSuccess({ Component.literal("Despawned boss.") }, false)
        return 1
    }

    // --- /wildbosses forcebattle <target> ---
    private fun forceBattle(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val player = ctx.source.playerOrException
        val started = entity.forceBattle(player)
        ctx.source.sendSuccess({ Component.literal(if (started) "Battle started." else "Failed to start battle.") }, false)
        return if (started) 1 else 0
    }

    // --- /wildbosses loot <tier> --- (gives directly to inventory, bypasses combat entirely)
    private fun giveLoot(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val player = source.playerOrException
        val tier = parseTier(StringArgumentType.getString(ctx, "tier"))
        val server = source.server

        val lootTableKey = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("wildbosses", "boss/${tier.name.lowercase()}")
        )
        val lootTable = server.reloadableRegistries().getLootTable(lootTableKey)
        val lootParams = LootParams.Builder(source.level).create(LootContextParamSets.EMPTY)

        val items = lootTable.getRandomItems(lootParams)
        items.forEach { stack -> player.inventory.add(stack) }

        source.sendSuccess({ Component.literal("Gave ${items.size} item(s) from the ${tier.name} loot table.") }, false)
        return items.size
    }

    // --- /wildbosses setlevel <target> <level> ---
    private fun setBossLevel(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val level = IntegerArgumentType.getInteger(ctx, "level")
        val instance = BossRegistry.get(entity.uuid)

        entity.pokemon.level = level
        instance?.currentLevelOverride = level

        ctx.source.sendSuccess({ Component.literal("Set ${entity.pokemon.species.name}'s level to $level.") }, false)
        return 1
    }

    // --- /wildbosses reload --- (mirrors vanilla's own /reload command)
    private fun reloadDatapacks(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        source.server.reloadResources(source.server.worldData.dataConfiguration.dataPacks().enabled)
        source.sendSuccess({ Component.literal("Reloading datapacks...") }, false)
        return 1
    }

    // --- /wildbosses glow <target> ---
    private fun reapplyGlow(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val instance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()
        BossSpawnListener.applyTierGlow(entity, instance.tier)
        ctx.source.sendSuccess({ Component.literal("Re-applied ${instance.tier.name} glow to ${entity.pokemon.species.name}.") }, false)
        return 1
    }

    // --- /wildbosses tier <target> <tier> ---
    private fun setBossTier(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val newTier = parseTier(StringArgumentType.getString(ctx, "tier"))
        val oldInstance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()

        entity.tags.remove("wildbosses:tier_${oldInstance.tier.name}")
        entity.tags.add("wildbosses:tier_${newTier.name}")

        ctx.source.server.scoreboard.removePlayerFromTeam(entity.uuid.toString())
        BossSpawnListener.applyTierGlow(entity, newTier)

        BossRegistry.unregister(entity.uuid)
        BossRegistry.register(entity, newTier, oldInstance.spawnedAtTick)

        ctx.source.sendSuccess({ Component.literal("Changed ${entity.pokemon.species.name}'s tier to ${newTier.name}.") }, false)
        return 1
    }

    // --- /wildbosses killall ---
    private fun killAllBosses(ctx: CommandContext<CommandSourceStack>): Int {
        val server = ctx.source.server
        val bosses = BossRegistry.allBosses().toList()
        var count = 0

        for (instance in bosses) {
            for (level in server.allLevels) {
                val entity = level.getEntity(instance.entityUuid)
                if (entity is PokemonEntity) {
                    entity.discard()
                    count++
                    break
                }
            }
            server.scoreboard.removePlayerFromTeam(instance.entityUuid.toString())
            BossRegistry.unregister(instance.entityUuid)
        }

        ctx.source.sendSuccess({ Component.literal("Despawned $count boss(es) and cleared the registry.") }, false)
        return count
    }

    // --- /wildbosses version ---
    private fun showVersion(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.source.sendSuccess({ Component.literal("WildBosses - see fabric.mod.json for version.") }, false)
        return 1
    }

    // --- /wildbosses cooldown ---
    private fun checkCooldown(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val player = source.playerOrException
        val overworld = source.server.overworld()
        val currentTick = overworld.gameTime

        val cooldownData = com.kaizzinho.wildbosses.boss.BossSpawnCooldownData.get(overworld)
        val onCooldown = cooldownData.isOnCooldown(player.uuid, currentTick)

        source.sendSuccess({
            Component.literal(if (onCooldown) "You ARE on spawn cooldown." else "You are NOT on spawn cooldown.")
        }, false)
        return 1
    }
}