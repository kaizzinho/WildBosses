package com.kaizzinho.wildbosses.command

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossRegistry
import com.kaizzinho.wildbosses.boss.BossPersistence
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import com.kaizzinho.wildbosses.spawn.BossSpawnListener
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

object WildBossCommands {

    private val NOT_A_BOSS = SimpleCommandExceptionType(Component.translatable("wildbosses.command.error.not_a_boss"))
    private val INVALID_TIER = SimpleCommandExceptionType(Component.translatable("wildbosses.command.error.invalid_tier"))

    private data class HelpEntry(val usage: String, val descriptionKey: String)

    private val HELP_ENTRIES = listOf(
        HelpEntry("/wb spawn <tier> [pokemon properties...] [respectCooldown=true]", "wildbosses.help.spawn"),
        HelpEntry("/wb list", "wildbosses.help.list"),
        HelpEntry("/wb info <target>", "wildbosses.help.info"),
        HelpEntry("/wb teleport <target>", "wildbosses.help.teleport"),
        HelpEntry("/wb despawn <target>", "wildbosses.help.despawn"),
        HelpEntry("/wb forcebattle <target>", "wildbosses.help.forcebattle"),
        HelpEntry("/wb loot <tier>", "wildbosses.help.loot"),
        HelpEntry("/wb setlevel <target> <level>", "wildbosses.help.setlevel"),
        HelpEntry("/wb reload", "wildbosses.help.reload"),
        HelpEntry("/wb reloadconfig", "wildbosses.help.reloadconfig"),
        HelpEntry("/wb glow <target>", "wildbosses.help.glow"),
        HelpEntry("/wb tier <target> <tier>", "wildbosses.help.tier"),
        HelpEntry("/wb killall", "wildbosses.help.killall"),
        HelpEntry("/wb version", "wildbosses.help.version"),
        HelpEntry("/wb help", "wildbosses.help.help"),
        HelpEntry("/wb cooldown", "wildbosses.help.cooldown"),
        HelpEntry("/wb leaderboard [tier]", "wildbosses.help.leaderboard")
    )


    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("wildbosses")
                    .then(Commands.literal("spawn")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("tier", StringArgumentType.word())
                            .executes { ctx -> spawnBoss(ctx, null) }
                            .then(Commands.argument("properties", StringArgumentType.greedyString())
                                .executes { ctx -> spawnBoss(ctx, StringArgumentType.getString(ctx, "properties")) }
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
                    .then(Commands.literal("debugentity")
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes { ctx -> debugEntity(ctx) }
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
                    .then(Commands.literal("reloadconfig")
                        .requires { it.hasPermission(2) }
                        .executes { ctx -> reloadConfig(ctx) }
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

    private data class SpawnInput(
        val properties: PokemonProperties,
        val respectCooldown: Boolean
    )

    private fun spawnBoss(ctx: CommandContext<CommandSourceStack>, rawProperties: String?): Int {
        val source = ctx.source
        val player = source.playerOrException
        val tier = parseTier(StringArgumentType.getString(ctx, "tier"))
        val input = parseSpawnInput(rawProperties)

        val entity = input.properties.createEntity(source.level)
        entity.moveTo(player.x, player.y, player.z, player.yRot, player.xRot)
        source.level.addFreshEntity(entity)

        BossSpawnListener.forcePromote(entity, tier, player, input.respectCooldown)

        source.sendSuccess({
            Component.translatable("wildbosses.command.spawn.success", tier.name, entity.pokemon.species.name)
        }, false)
        return 1
    }

    private fun parseSpawnInput(rawProperties: String?): SpawnInput {
        if (rawProperties.isNullOrBlank()) {
            return SpawnInput(PokemonProperties.parse("pikachu"), false)
        }

        val tokens = rawProperties.trim().split(Regex("\\s+")).toMutableList()
        var respectCooldown = false
        val propertiesTokens = mutableListOf<String>()

        for (token in tokens) {
            if (token.startsWith("respectCooldown=", ignoreCase = true)) {
                val value = token.substringAfter('=').lowercase().toBooleanStrictOrNull()
                    ?: throw SimpleCommandExceptionType(
                        Component.translatable("wildbosses.command.error.invalid_respect_cooldown")
                    ).create()
                respectCooldown = value
            } else {
                propertiesTokens += token
            }
        }

        if (propertiesTokens.size > 1) {
            val legacyValue = propertiesTokens.last().lowercase().toBooleanStrictOrNull()
            if (legacyValue != null) {
                respectCooldown = legacyValue
                propertiesTokens.removeLast()
            }
        }

        val propertiesText = propertiesTokens.joinToString(" ").ifBlank { "pikachu" }
        val properties = PokemonProperties.parse(propertiesText)
        if (properties.species == null) {
            throw SimpleCommandExceptionType(
                Component.translatable("wildbosses.command.error.invalid_properties", propertiesText)
            ).create()
        }

        return SpawnInput(properties, respectCooldown)
    }

    private fun listBosses(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val bosses = BossRegistry.allBosses()
        if (bosses.isEmpty()) {
            source.sendSuccess({ Component.translatable("wildbosses.command.list.empty") }, false)
            return 0
        }
        source.sendSuccess({ Component.translatable("wildbosses.command.list.header", bosses.size) }, false)
        for (instance in bosses) {
            val pos = instance.lastKnownPos
            val posText = if (pos != null) {
                "(${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()})"
            } else {
                Component.translatable("wildbosses.command.list.unknown_pos").string
            }
            source.sendSuccess({
                Component.translatable(
                    "wildbosses.command.list.entry",
                    instance.speciesName ?: "?",
                    instance.tier.name,
                    posText,
                    instance.entityUuid.toString()
                )
            }, false)
        }
        return bosses.size
    }

    private fun bossInfo(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val instance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()
        ctx.source.sendSuccess({
            Component.translatable(
                "wildbosses.command.info.line",
                instance.speciesName ?: entity.pokemon.species.name,
                instance.tier.name,
                entity.pokemon.level,
                instance.currentLevelOverride.toString(),
                entity.pokemon.shiny.toString(),
                entity.blockPosition().toString(),
                entity.uuid.toString()
            )
        }, false)
        return 1
    }

    private fun showHelp(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        source.sendSuccess({ Component.translatable("wildbosses.command.help.header") }, false)
        for (entry in HELP_ENTRIES) {
            source.sendSuccess({
                Component.literal(entry.usage).withStyle(net.minecraft.ChatFormatting.AQUA)
                    .append(
                        Component.translatable(
                            "wildbosses.command.help.entry",
                            Component.translatable(entry.descriptionKey)
                        )
                    )
            }, false)
        }
        return HELP_ENTRIES.size
    }

    private fun teleportToBoss(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val player = ctx.source.playerOrException
        player.teleportTo(entity.x, entity.y, entity.z)
        ctx.source.sendSuccess({
            Component.translatable("wildbosses.command.teleport.success", entity.pokemon.species.name)
        }, false)
        return 1
    }

    private fun showLeaderboard(ctx: CommandContext<CommandSourceStack>, tierName: String?): Int {
        val source = ctx.source
        val overworld = source.server.overworld()
        val data = com.kaizzinho.wildbosses.boss.BossKillLeaderboardData.get(overworld)

        if (tierName == null) {
            val top = data.topByTotal(10)
            source.sendSuccess({ Component.translatable("wildbosses.command.leaderboard.header_total") }, false)
            top.forEachIndexed { i, (record, count) ->
                source.sendSuccess({
                    Component.translatable("wildbosses.command.leaderboard.entry", i + 1, record.playerName, count)
                }, false)
            }
            return top.size
        }

        val tier = parseTier(tierName)
        val top = data.topByTier(tier, 10)
        source.sendSuccess({
            Component.translatable("wildbosses.command.leaderboard.header_tier", tier.name)
        }, false)
        top.forEachIndexed { i, (record, count) ->
            source.sendSuccess({
                Component.translatable("wildbosses.command.leaderboard.entry", i + 1, record.playerName, count)
            }, false)
        }
        return top.size
    }

    private fun despawnBoss(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val server = ctx.source.server
        server.scoreboard.removePlayerFromTeam(entity.uuid.toString())
        BossRegistry.unregister(entity.uuid)
        entity.discard()
        (entity.level() as? ServerLevel)?.let { BossPersistence.requestLevelCheckpoint(it) }
        ctx.source.sendSuccess({ Component.translatable("wildbosses.command.despawn.success") }, false)
        return 1
    }

    private fun forceBattle(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val player = ctx.source.playerOrException
        val started = entity.forceBattle(player)
        ctx.source.sendSuccess({
            Component.translatable(
                if (started) "wildbosses.command.forcebattle.started" else "wildbosses.command.forcebattle.failed"
            )
        }, false)
        return if (started) 1 else 0
    }

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

        source.sendSuccess({
            Component.translatable("wildbosses.command.loot.success", items.size, tier.name)
        }, false)
        return items.size
    }

    private fun setBossLevel(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val level = IntegerArgumentType.getInteger(ctx, "level")
        val instance = BossRegistry.get(entity.uuid)

        entity.pokemon.level = level
        instance?.currentLevelOverride = level

        ctx.source.sendSuccess({
            Component.translatable("wildbosses.command.setlevel.success", entity.pokemon.species.name, level)
        }, false)
        return 1
    }

    private fun reloadDatapacks(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        source.server.reloadResources(source.server.worldData.dataConfiguration.dataPacks().enabled)
        source.sendSuccess({ Component.translatable("wildbosses.command.reload.success") }, false)
        return 1
    }

    private fun reloadConfig(ctx: CommandContext<CommandSourceStack>): Int {
        WildBossesConfig.reload()
        WildBosses.ensureCobblemonLevelCap()
        ctx.source.sendSuccess({ Component.translatable("wildbosses.command.reloadconfig.success") }, false)
        return 1
    }

    private fun reapplyGlow(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val instance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()
        BossPersistence.restoreRuntimeState(entity, instance.tier, instance.spawnedAtTick)
        BossPersistence.requestCheckpoint(entity)
        ctx.source.sendSuccess({
            Component.translatable("wildbosses.command.glow.success", instance.tier.name, entity.pokemon.species.name)
        }, false)
        return 1
    }

    private fun setBossTier(ctx: CommandContext<CommandSourceStack>): Int {
        val entity = getBossEntity(ctx)
        val newTier = parseTier(StringArgumentType.getString(ctx, "tier"))
        val oldInstance = BossRegistry.get(entity.uuid) ?: throw NOT_A_BOSS.create()

        entity.tags.remove("wildbosses:tier_${oldInstance.tier.name}")
        entity.tags.add("wildbosses:tier_${newTier.name}")

        ctx.source.server.scoreboard.removePlayerFromTeam(entity.uuid.toString())

        BossRegistry.unregister(entity.uuid)
        BossRegistry.register(entity, newTier, oldInstance.spawnedAtTick)
        BossPersistence.restoreRuntimeState(entity, newTier, oldInstance.spawnedAtTick)
        BossPersistence.requestCheckpoint(entity)

        ctx.source.sendSuccess({
            Component.translatable("wildbosses.command.tier.success", entity.pokemon.species.name, newTier.name)
        }, false)
        return 1
    }

    private fun killAllBosses(ctx: CommandContext<CommandSourceStack>): Int {
        val server = ctx.source.server
        val bosses = BossRegistry.allBosses().toList()
        var count = 0

        for (instance in bosses) {
            for (level in server.allLevels) {
                val entity = level.getEntity(instance.entityUuid)
                if (entity is PokemonEntity) {
                    entity.discard()
                    BossPersistence.requestLevelCheckpoint(level)
                    count++
                    break
                }
            }
            server.scoreboard.removePlayerFromTeam(instance.entityUuid.toString())
            BossRegistry.unregister(instance.entityUuid)
        }

        ctx.source.sendSuccess({ Component.translatable("wildbosses.command.killall.success", count) }, false)
        return count
    }

    private fun showVersion(ctx: CommandContext<CommandSourceStack>): Int {
        ctx.source.sendSuccess({
            Component.literal("WildBosses | persistence build ${WildBosses.PERSISTENCE_PROBE_BUILD}")
        }, false)
        return 1
    }

    private fun debugEntity(ctx: CommandContext<CommandSourceStack>): Int {
        val target = EntityArgument.getEntity(ctx, "target")
        if (target !is PokemonEntity) {
            ctx.source.sendFailure(Component.literal("Target is not a PokemonEntity"))
            return 0
        }

        val registry = BossRegistry.get(target.uuid)
        val syncedBoss = runCatching { target.entityData.get(com.kaizzinho.wildbosses.boss.WildBossEntityData.IS_BOSS) }.getOrNull()
        val syncedTier = runCatching { target.entityData.get(com.kaizzinho.wildbosses.boss.WildBossEntityData.TIER) }.getOrNull()
        val labelLevel = runCatching { target.entityData.get(PokemonEntity.LABEL_LEVEL) }.getOrNull()
        val teamName = target.team?.name ?: "<none>"
        val bossTag = target.tags.contains("wildbosses:is_boss")
        val tierTags = target.tags.filter { it.startsWith("wildbosses:tier_") }.joinToString(",").ifBlank { "<none>" }
        val spawnTags = target.tags.filter { it.startsWith("wildbosses:spawned_at_") }.joinToString(",").ifBlank { "<none>" }

        val lines = listOf(
            "build=${WildBosses.PERSISTENCE_PROBE_BUILD}",
            "species=${target.pokemon.species.name} entityUuid=${target.uuid} pokemonUuid=${target.pokemon.uuid}",
            "registry=${registry != null} registryTier=${registry?.tier?.name ?: "<none>"}",
            "bossTag=$bossTag tierTags=$tierTags spawnTags=$spawnTags",
            "syncedBoss=$syncedBoss syncedTier=$syncedTier labelLevel=$labelLevel pokemonLevel=${target.pokemon.level}",
            "team=$teamName glowing=${target.isCurrentlyGlowing()} persistenceRequired=${target.isPersistenceRequired()} removed=${target.isRemoved}"
        )
        for (line in lines) {
            ctx.source.sendSuccess({ Component.literal("[WB-DEBUG] $line") }, false)
        }
        BossPersistence.probeState("COMMAND_DEBUG_ENTITY", target)
        return 1
    }

    private fun checkCooldown(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val player = source.playerOrException
        val overworld = source.server.overworld()
        val currentTick = overworld.gameTime

        val cooldownData = com.kaizzinho.wildbosses.boss.BossSpawnCooldownData.get(overworld)
        val onCooldown = cooldownData.isOnCooldown(player.uuid, currentTick)

        source.sendSuccess({
            Component.translatable(
                if (onCooldown) "wildbosses.command.cooldown.on" else "wildbosses.command.cooldown.off"
            )
        }, false)
        return 1
    }
}
