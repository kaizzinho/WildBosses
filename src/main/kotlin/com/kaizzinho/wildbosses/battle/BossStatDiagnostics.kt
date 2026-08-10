package com.kaizzinho.wildbosses.battle

import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import com.kaizzinho.wildbosses.WildBosses
import com.kaizzinho.wildbosses.boss.BossTier
import com.kaizzinho.wildbosses.config.WildBossesConfig
import java.util.Locale
import kotlin.math.truncate

object BossStatDiagnostics {
    private data class StatSnapshot(
        val hp: Int,
        val attack: Int,
        val defence: Int,
        val specialAttack: Int,
        val specialDefence: Int,
        val speed: Int
    )

    fun log(pokemon: Pokemon, tier: BossTier) {
        val current = snapshot(pokemon)
        WildBosses.logger.info(
            "[BossStats] ${tier.name} ${pokemon.species.name} Lv.${pokemon.level} " +
                "HP=${current.hp} ATK=${current.attack} DEF=${current.defence} " +
                "SPA=${current.specialAttack} SPD=${current.specialDefence} SPE=${current.speed}"
        )

        if (!WildBossesConfig.data.bossAiDebugLogging || pokemon.level <= 100) return

        val baseline = snapshotAtLevel(pokemon, 100)
        WildBosses.logger.info(
            "[BossStats][DEBUG] Lv.100 -> Lv.${pokemon.level} " +
                "HP ${delta(baseline.hp, current.hp)} | ATK ${delta(baseline.attack, current.attack)} | " +
                "DEF ${delta(baseline.defence, current.defence)} | SPA ${delta(baseline.specialAttack, current.specialAttack)} | " +
                "SPD ${delta(baseline.specialDefence, current.specialDefence)} | SPE ${delta(baseline.speed, current.speed)}"
        )
    }

    private fun snapshot(pokemon: Pokemon) = StatSnapshot(
        hp = pokemon.maxHealth,
        attack = pokemon.attack,
        defence = pokemon.defence,
        specialAttack = pokemon.specialAttack,
        specialDefence = pokemon.specialDefence,
        speed = pokemon.speed
    )

    private fun snapshotAtLevel(pokemon: Pokemon, level: Int) = StatSnapshot(
        hp = statAtLevel(pokemon, Stats.HP, level),
        attack = statAtLevel(pokemon, Stats.ATTACK, level),
        defence = statAtLevel(pokemon, Stats.DEFENCE, level),
        specialAttack = statAtLevel(pokemon, Stats.SPECIAL_ATTACK, level),
        specialDefence = statAtLevel(pokemon, Stats.SPECIAL_DEFENCE, level),
        speed = statAtLevel(pokemon, Stats.SPEED, level)
    )

    private fun statAtLevel(pokemon: Pokemon, stat: Stat, level: Int): Int {
        val iv = pokemon.ivs.getEffectiveBattleIV(stat)
        val base = pokemon.form.baseStats[stat] ?: 0
        val ev = pokemon.evs.getOrDefault(stat)

        if (stat == Stats.HP) {
            if (pokemon.species.resourceIdentifier.path == "shedinja") return 1
            return truncate(
                truncate(2.0 * base + iv + truncate(ev / 4.0) + 100) * level / 100.0 + 10
            ).toInt()
        }

        val raw = ((2 * base + iv + (ev / 4)) * level) / 100 + 5
        return pokemon.effectiveNature.modifyStat(stat, raw)
    }

    private fun delta(before: Int, after: Int): String {
        if (before <= 0) return "$before->$after"
        val change = ((after - before) * 100.0) / before
        return "$before->$after (${String.format(Locale.ROOT, "%+.1f%%", change)})"
    }
}
