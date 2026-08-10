# WildBosses 🐾⚔️

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-0.17.2%2B-DBB69B?logo=fabric&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)
![Cobblemon](https://img.shields.io/badge/Cobblemon-1.7.3-3E8E41)
![License](https://img.shields.io/badge/license-MIT-blue)

**[Modrinth](https://modrinth.com/mod/wildbosses) · [Source](https://github.com/kaizzinho/WildBosses) · [Issues](https://github.com/kaizzinho/WildBosses/issues)**

*Read this in* *[English](#english)* *| Leia em* *[Português](#português)*

---

## English

### What is it?

**WildBosses** adds rare, roaming Boss Pokémon to [Cobblemon](https://cobblemon.com/).

Instead of creating separate scripted encounters, WildBosses occasionally promotes an ordinary wild Pokémon into a **Boss** with its own tier, visual identity, difficulty, battle scaling, and reward pool.

Bosses are designed as an endgame PvE activity. Their real level stays hidden until battle, they scale to the player who challenges them, and the strongest tiers can reach well beyond level 100.

WildBosses works on top of Cobblemon rather than replacing or forking its core systems.

### Screenshots

The screenshots below are arranged in the same order a player would normally experience a Boss encounter.

#### A Boss Has Appeared

When a Boss spawns nearby, the player receives a chat message. It tells you that something rare has appeared — but you still have to find it.

<!--
SCREENSHOT 01 — Spawn alert
Recommended shot: capture the WildBosses spawn message in chat before the Boss is visible.
Keep the normal game HUD and surrounding environment visible so it feels like a natural encounter.

File: docs/images/01-boss-spawn.png
Uncomment after adding the image:
![A Wild Boss has appeared nearby](docs/images/01-boss-spawn.png)
-->

#### Something Is Nearby

Every Boss glows in the color of its tier. The outline can be seen through walls and underwater, giving the player a clue when the Boss is close but not yet in sight.

<!--
SCREENSHOT 02 — Boss behind a wall
Recommended shot: stand on the opposite side of terrain, a cave wall, building, or other obstruction while the Boss glow is clearly visible through it.
A Legendary or Mythic glow will make this shot especially striking.

File: docs/images/02-boss-through-wall.png
Uncomment after adding the image:
![A Wild Boss glowing through a wall](docs/images/02-boss-through-wall.png)
-->

#### Finding the Boss

Once you reach it, the Boss can be seen normally in the overworld — but its real level is still hidden as `Lv. ??` until someone accepts the fight.

<!--
SCREENSHOT 03 — First sight
Recommended shot: the first clear view after following the glow.
Keep some terrain in the frame so this feels like discovery rather than a staged showcase.
Make sure the Boss nameplate and Lv. ?? are readable.

File: docs/images/03-boss-found.png
Uncomment after adding the image:
![Finding a Wild Boss](docs/images/03-boss-found.png)
-->

#### Legendary & Mythic Bosses

The highest tiers are deliberately rare. Their stronger glow, improved stats, increased shiny odds, and access to the most dangerous mechanics make finding one feel very different from an ordinary wild encounter.

<!--
SCREENSHOT 04 — Main showcase
Recommended shot: a clean Legendary or Mythic Boss in a good-looking overworld location.
Show the full Pokémon model, tier-colored glow, nameplate, and Lv. ??.
Avoid admin/debug messages in this one — this should be the polished hero screenshot for the mod.

File: docs/images/04-mythic-overworld.png
Uncomment after adding the image:
![A Mythic Wild Boss in the overworld](docs/images/04-mythic-overworld.png)
-->

#### Dynamic Level Scaling

The real Boss level is calculated when battle begins. High-level players can therefore face encounters far beyond Cobblemon's normal level-100 progression.

<!--
SCREENSHOT 05 — Level 100+ battle
Recommended shot: battle against an Epic, Legendary, or Mythic Boss at Lv. 101+.
Try to show the Boss health bar, revealed level, your Pokémon, and the normal battle interface at the same time.

File: docs/images/05-scaled-battle.png
Uncomment after adding the image:
![A dynamically scaled level 100+ Boss battle](docs/images/05-scaled-battle.png)
-->

#### Enrage & Mega Evolution

Boss fights become more dangerous when they drag on. Rare+ Bosses periodically gather power and **Enrage**, while eligible high-tier encounters can also **Mega Evolve** when compatible Mega Evolution data is available.

<!--
SCREENSHOT 06 — Enrage + Mega Evolution
Recommended shot: a Mega-Evolved Legendary/Mythic Boss during an Enrage cycle.
Best possible shot: Lv. 100+ Mega Boss + Boss health bar + the Enrage warning/trigger message visible together.
If that becomes too visually crowded, split this into two screenshots.

File: docs/images/06-mega-enrage.png
Uncomment after adding the image:
![A Mega-Evolved Boss during the Enrage mechanic](docs/images/06-mega-enrage.png)
-->

#### Boss Rewards

Each tier has its own reward table. With **Cobblemon Loot Menu** installed, regular WildBosses tier rewards can be combined with the Pokémon's normal species loot and resolved through a single server-authoritative selection screen.

**Mega Stones are the exception.** When a player earns one from a Boss that actually Mega Evolved, the stone is granted **directly to that player** and is never sent through the Loot Menu. Mega Stones are rare, one-time progression rewards rather than normal selectable loot.

<!--
SCREENSHOT 07 — Loot Menu integration
Recommended shot: the selection menu after defeating a Legendary or Mythic Boss.
Use a reward roll with several visually distinct items so the difference between a normal wild drop and a Boss reward is obvious.

File: docs/images/07-loot-menu.png
Uncomment after adding the image:
![WildBosses rewards inside Cobblemon Loot Menu](docs/images/07-loot-menu.png)
-->

#### Progression

WildBosses also includes its own advancement tree for defeating tiers, surviving special encounters, hunting multiple Bosses, finding shiny Bosses, and interacting with the Mega Evolution system.

<!--
SCREENSHOT 08 — Advancements
Recommended shot: open the WildBosses advancement tab after unlocking several connected advancements.
A partially completed tree usually looks better than either a completely empty or completely finished one.

File: docs/images/08-advancements.png
Uncomment after adding the image:
![WildBosses advancement progression](docs/images/08-advancements.png)
-->

### How does an encounter work?

```text
a normal wild Pokémon spawns
    -> WildBosses rolls the Boss chance
    -> a Boss tier is selected
    -> the Pokémon becomes a roaming Boss
    -> its real level stays hidden as Lv. ??
    -> a player finds and challenges it
    -> its level is scaled for that player
    -> the encounter is forced into 1v1
    -> Rare+ Bosses can trigger Enrage in longer fights
    -> eligible high-tier Bosses may Mega Evolve
    -> victory rolls the tier reward table
```

The Boss is not permanently assigned to the player who caused it to appear. Other players may find and challenge an existing Boss too.

### Boss tiers

There are five tiers:

| Tier | Default weight | Level bonus | AI skill | Shiny chance | Perfect IVs | Mega chance* |
|---|---:|---:|---:|---:|---|---:|
| **Uncommon** | 54 | +5 | 2 | 0% | No | 0% |
| **Rare** | 30 | +10 | 3 | 0% | No | 0% |
| **Epic** | 10 | +15 | 4 | 0% | Yes | 0% |
| **Legendary** | 5 | +20 | 5 | 25% | Yes | 50% |
| **Mythic** | 1 | +25 | 5 | 45% | Yes | 100% |

The default weights add up to 100, so they can be read directly as the tier distribution **after a wild spawn has already been selected to become a Boss**. They are not the chance for an arbitrary Pokémon to become a Boss.

\* Mega Evolution only applies when the Boss is eligible and compatible Mega Evolution data is available.

Every value in the table is configurable.

### Features

- five Boss tiers from **Uncommon** to **Mythic**;
- configurable chance for ordinary wild spawns to become Bosses;
- chat notification when a Boss appears nearby;
- hidden overworld level displayed as `Lv. ??`;
- dynamic battle scaling based on the challenger's strongest party Pokémon;
- configurable maximum Boss level, defaulting to **200**;
- forced **1v1** encounters;
- aggression-focused Boss AI that prioritizes KOs, STAB, type effectiveness, coverage, accuracy, and priority moves;
- delayed-move awareness for attacks such as **Future Sight** and **Doom Desire**, so the AI does not waste back-to-back turns on pending damage;
- Epic+ Bosses are forced into their final evolutionary form;
- Epic+ Bosses receive perfect IVs by default;
- Legendary and Mythic Bosses receive increased shiny odds by default;
- tier-colored glow visible through walls and underwater;
- configurable **Enrage** mechanic for Rare+ Bosses in long battles;
- optional Mega Evolution behavior for eligible high-tier Bosses;
- five vanilla Minecraft JSON loot tables, one for each tier;
- persistent per-player spawn cooldowns;
- Boss-state recovery after server and world reloads;
- persistent Boss kill leaderboard;
- WildBosses advancement tree;
- admin and testing command suite under `/wildbosses` and `/wb`;
- public Boss metadata API for optional client/server integrations;
- soft integration with **Cobblemon Battle Slider** and **CobbleTunes** when those mods are installed;
- public loot-award event for integrations with other server-side mods.

### Dynamic level scaling

A Boss does not have one permanent battle level.

When a player starts the encounter, WildBosses checks the strongest Pokémon in that player's party and adds the bonus configured for the Boss tier.

For example, with the default configuration:

```text
strongest party Pokémon: Lv. 72
Epic tier bonus:         +15
--------------------------------
Boss battle level:       Lv. 87
```

Before battle, the overworld label still shows `Lv. ??`.

Levels above 100 use Cobblemon's normal stat formulas for the full stat line — **HP, Attack, Defense, Sp. Atk, Sp. Def, and Speed** all continue scaling from the Boss's effective level. WildBosses does not apply a second hidden stat multiplier on top.

If another player later challenges the same Boss, its battle level is calculated again from that player's own party. This lets the encounter stay relevant across very different stages of server progression.

### Battle rules

Boss encounters are forced into **1v1**.

If the player wins, the Boss is defeated and its tier reward table is rolled.

If the player loses or flees, the Boss is restored to full HP and remains available for another attempt. A later challenger receives a fresh level calculation based on their own party.

Bosses cannot be captured.

### Aggressive Boss AI

WildBosses keeps Bosses intentionally different from competitive trainers. They do not play like long-term tacticians: they behave like dangerous wild creatures trying to end the fight.

The Boss AI scores usable attacks from the current matchup and heavily favors immediate KOs. It also considers STAB, type effectiveness, physical/special stats, accuracy, priority, multi-hit damage, and common type-immunity abilities. Risky self-nerfing attacks such as Close Combat or Draco Meteor are allowed when the payoff is worth it instead of being permanently blacklisted.

Higher `aiSkill` values make the Boss more consistent:

- lower tiers can occasionally pick a merely good attack instead of the optimal one;
- Epic+ Bosses can receive one aggressive setup move when their species has a suitable option;
- Legendary/Mythic Bosses may use a single recovery option when badly hurt and no immediate KO is available;
- setup is limited and recovery is deliberately desperate, so Bosses stay aggressive instead of turning into stall trainers.

The moveset is rebuilt when the battle starts. Higher-skill Bosses accept slightly riskier accuracy in exchange for stronger attacks and can keep a useful priority move for finishing weakened targets.

All Boss tiers also have **danger coverage**. If a species has a 4× defensive weakness, WildBosses looks through its legal attacks for a good move that can punish Pokémon of that threatening type. This is not a species hardcode: Omastar can prefer Ice coverage into Grass, Swampert can do the same, Gyarados can value Ground coverage into Electric, and the same logic works for other typings. Weak or badly matched coverage is still rejected instead of forcing junk moves into the set.

The caution level scales by tier:

| Tier | 4× weakness coverage | Telegraph reaction |
|---|---|---|
| **Uncommon** | Moderate preference | Usually reacts |
| **Rare** | Strong preference | Reacts most of the time |
| **Epic** | Near-guaranteed when a good move exists | Almost always reacts |
| **Legendary** | Guaranteed when a good move exists | Perfect awareness |
| **Mythic** | Guaranteed when a good move exists | Perfect awareness |

The battle AI can also read a damaging two-turn move that was visibly prepared on the previous turn. A 2× incoming hit makes an aware Boss avoid greedy setup and lean harder toward its best attack. A 4× hit is treated as a real danger state: setup/recovery are skipped and the Boss uses its best immediate non-delayed attack. Legendary and Mythic Bosses always respect telegraphed threats; lower tiers can still make the occasional bad call.

Delayed attacks are handled as delayed damage instead of fake instant nukes. **Future Sight** and **Doom Desire** get a lower moveset/scoring priority, cannot steal the normal immediate-KO bonus, and are blocked while a previous delayed hit is still pending. The Boss can still use them when they make sense — it just will not spam them on back-to-back turns.

For testing, enable `bossAiDebugLogging` in the config or **Boss AI Debug Logging** in Mod Menu. Starting a Boss battle is enough to see the AI activation and generated moveset in the console; with debug logging enabled, every turn also prints candidate scores, estimated damage, type multiplier, KO-on-hit checks, setup/recovery choices, and the selected move.

### Enrage

Boss battles are not meant to be stalled forever.

By default, **Rare, Epic, Legendary, and Mythic** Bosses Enrage every **8 turns**. Uncommon Bosses do not use Enrage. One turn before the next Enrage, the player receives a warning that the Boss is gathering power.

When the Enrage triggers, the Boss gains the configured number of stages in Attack, Defense, Sp. Atk, Sp. Def, and Speed. The default is **+1 stage** per Enrage, with the normal battle-stage ceiling still applying.

The interval and strength are configurable.

### Mega Evolution

The current build contains optional support for Mega-Evolved Boss encounters through compatible Mega Evolution data such as **MegaShowdown**.

By default:

- Uncommon, Rare, and Epic Bosses do not attempt to Mega Evolve;
- Legendary Bosses have a `50%` configured Mega Evolution chance;
- Mythic Bosses have a `100%` configured Mega Evolution chance.

The Boss must still be eligible for a compatible Mega Evolution. The chance alone does not give a species a Mega form that does not exist.

#### Mega Stone rewards

Mega Stones are tied to Bosses that **actually Mega Evolve during the battle**. Defeating an eligible Boss that never Mega Evolves does not grant its Mega Stone.

When earned, the Mega Stone is granted **directly to the player**. It does not enter the normal Boss loot pool and is not passed to **Cobblemon Loot Menu**.

Each Mega Stone is also a **one-time reward per species, per player**. Once a player has received the Mega Stone associated with that species, defeating more Mega-Evolved Bosses of the same species will not award another copy.

For example:

```text
1st Mega Pidgeot defeated -> Pidgeotite awarded
2nd Mega Pidgeot defeated -> no Pidgeotite
3rd Mega Pidgeot defeated -> no Pidgeotite
...
Mega Charizard defeated   -> its Mega Stone can still be awarded
```

This makes Mega Stones progression rewards for discovering and defeating different Mega-capable Boss species, rather than a repeatable farming source.

WildBosses also contains progression related to defeating Mega-Evolved Bosses and receiving Mega Stones.

### Spawn and lifecycle

The default Boss promotion chance is:

```text
1 / 512
```

After a player causes a Boss to spawn, that player enters a persistent spawn cooldown. The default cooldown is:

```text
15 minutes
```

This cooldown only prevents that player from causing another Boss spawn. It does not stop them from fighting a Boss that already exists nearby or one that appeared for somebody else.

An unchallenged Boss normally roams for approximately:

```text
10 minutes
```

before automatically despawning.

Active Boss state is recovered after world reloads, including its tracked tier and glow state. Player spawn cooldowns also survive server restarts.

### Species restrictions

Legendary, Mythical, and Ultra Beast species are excluded from becoming WildBosses by default.

WildBosses reads Cobblemon species labels instead of maintaining a separate hardcoded Pokémon list. The excluded labels can be changed in the configuration.

### Loot

Each tier uses a standard Minecraft loot table:

```text
data/wildbosses/loot_table/boss/uncommon.json
data/wildbosses/loot_table/boss/rare.json
data/wildbosses/loot_table/boss/epic.json
data/wildbosses/loot_table/boss/legendary.json
data/wildbosses/loot_table/boss/mythic.json
```

The reward pools scale in quantity and quality and can include items such as:

- type gems;
- evolution items;
- held items;
- healing items;
- relic coins;
- rare high-tier rewards.

Because the rewards use normal Minecraft loot tables, server and modpack authors can replace or modify them through datapacks without changing WildBosses code.

#### Customizing Boss loot with a datapack

You do not need to modify the WildBosses JAR to rebalance Boss rewards. A datapack can override any of the five built-in loot tables.

For Minecraft `1.21.1`, create a datapack inside your world's `datapacks` folder:

```text
<world>/
└─ datapacks/
   └─ my_wildbosses_loot/
      ├─ pack.mcmeta
      └─ data/
         └─ wildbosses/
            └─ loot_table/
               └─ boss/
                  ├─ uncommon.json
                  ├─ rare.json
                  ├─ epic.json
                  ├─ legendary.json
                  └─ mythic.json
```

The namespace and paths matter. To replace a WildBosses table, keep the namespace as `wildbosses` and use the same path as the table bundled with the mod.

You only need to include the tiers you actually want to change. For example, a datapack that only overrides Mythic rewards can contain just:

```text
data/wildbosses/loot_table/boss/mythic.json
```

A minimal `pack.mcmeta` for Minecraft `1.21.1` uses data pack format `48`:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "Custom WildBosses loot"
  }
}
```

The easiest workflow is:

1. open the WildBosses JAR with an archive tool such as 7-Zip;
2. copy the tier file you want from `data/wildbosses/loot_table/boss/`;
3. place it at the matching path inside your datapack;
4. edit its pools, entries, weights, rolls, counts, conditions, or item IDs;
5. run `/reload` after adding or changing the datapack;
6. use `/wb loot <tier>` to quickly test the resulting reward pool.

Because the datapack overrides the same resource location, you can rebalance one tier without touching the others or rebuilding the mod.

For the full datapack and loot-table formats, see the [Minecraft Wiki guide to creating a data pack](https://minecraft.wiki/w/Tutorials/Creating_a_data_pack) and the [Minecraft Wiki loot table reference](https://minecraft.wiki/w/Loot_table).

> **Note:** Mega Stones are not part of these tier loot tables. Their reward is handled separately by WildBosses when an eligible Boss actually Mega Evolves, and each species can award its stone only once per player.

### Advancements

WildBosses ships with its own advancement tree.

Current progression includes achievements for:

- defeating your first Boss;
- defeating an Uncommon, Rare, Epic, Legendary, and Mythic Boss;
- defeating 5 Bosses;
- defeating 25 Bosses;
- defeating at least one Boss of every tier;
- witnessing and overcoming Enrage;
- defeating a shiny Boss;
- successfully fleeing from a Boss encounter;
- defeating a Boss that Mega Evolved;
- receiving your first Mega Stone from a Boss.

### Leaderboard

Boss victories are tracked persistently.

The leaderboard can show the top Boss hunters overall or filter the ranking by tier:

```text
/wb leaderboard
/wb leaderboard mythic
```

### Cobblemon Loot Menu integration

WildBosses exposes a public reward event that allows another server-side mod to take responsibility for a generated Boss reward before WildBosses awards it normally.

**Cobblemon Loot Menu** uses this integration to combine the regular Boss reward with the Pokémon's normal species loot:

```text
WildBosses tier rewards
        +
the Pokémon's normal Cobblemon species loot
        ↓
one server-authoritative loot selection
```

**Mega Stones intentionally bypass this integration.** If the defeated Boss actually Mega Evolved and the player is eligible for that species' one-time Mega Stone reward, the stone is granted **directly to the player**. It is never added to the Loot Menu session.

```text
normal Boss loot  -> Cobblemon Loot Menu
Mega Stone        -> player directly
```

This keeps Mega Stones separate from ordinary selectable loot because they are rare, unique progression rewards that can only be earned once per species, per player.

When the integration successfully takes responsibility for the regular reward, it calls `claim()` on the event and WildBosses does not award the same stacks a second time.

The current event also exposes the Boss entity UUID so integrations can reliably associate rewards with the Pokémon that produced them.

### Optional cross-mod integrations

WildBosses can expose Boss battle metadata to other mods without making them hard dependencies. The current integration API can report whether a Pokémon is a Boss, its tier, and its current scaled battle level.

#### Cobblemon Battle Slider

With **Cobblemon Battle Slider** installed, WildBoss encounters get a dedicated VS intro instead of looking like a normal wild fight.

- the Boss side uses the tier color;
- the label reads like `Mythic Boss Garchomp`;
- the scaled level is shown as `Scaled Level = Lv.125`;
- the real Pokémon model is rendered in 3D and auto-fitted to the portrait slot;
- tiny and huge species share the same slot without a per-species scale table;
- the normal overworld Boss nameplate is hidden while the VS intro is on screen;
- the player/trainer side can use controlled 3D upper-body portraits with a 2D skin fallback;
- WildBosses skips its own encounter title/subtitle while Battle Slider is loaded, preventing the old `Boss Encounter!` screen from flashing before the VS intro. The Boss cry and normal battle setup are kept.

The integration is soft. WildBosses still runs normally if Battle Slider is missing, including its original encounter title/subtitle.

#### CobbleTunes

With **CobbleTunes** installed, Boss battles can receive a region-aware theme based on the Boss species.

CobbleTunes builds a safe regional pool instead of blindly using every legendary song:

1. regional rival/PvP theme — highest priority;
2. generic regional legendary theme — medium priority when one exists;
3. Black/White World Tournament regional remix — lower priority;
4. species-specific legendary encounter themes — excluded.

The exact mix changes by Boss tier, and CobbleTunes rerolls once when it would immediately repeat the same Boss track for that region. The music integration is also soft; no extra soundtrack files are bundled by WildBosses.

### Requirements

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.17.2+`
- Fabric Language Kotlin `1.13.6+kotlin.2.2.20+`
- Cobblemon `1.7.3+`

Optional integrations:

- Mod Menu `11.0.3+` + YetAnotherConfigLib (YACL) `3.8.1+` for the config screen;
- Cobblemon Battle Slider for the Boss VS presentation;
- CobbleTunes for regional Boss music.

WildBosses is built for Fabric. Cobblemon's own required dependencies still apply. Mod Menu and YACL are only needed for the in-game configuration screen.

### Installation

1. Install Fabric Loader, Fabric Language Kotlin, Cobblemon, and Cobblemon's required dependencies.
2. Put the WildBosses JAR in the `mods` folder.
3. Start the game or server once to generate the configuration.
4. Adjust the generated settings if desired.

### Configuration

The first launch creates:

```text
config/wildbosses/wildbosses.json
```

With **Mod Menu + YACL** installed on the client, the same settings can also be edited from:

```text
Mods -> WildBosses -> Configure
```

The screen edits the same `wildbosses.json` model; it does not create a second config. In singleplayer the integrated server uses those values directly. On dedicated servers, gameplay remains controlled by the server's own config, so changing a local client's copy does not override server rules.

The generated file contains comments explaining the available values and controls:

- global Boss spawn chance;
- player spawn cooldown;
- excluded Cobblemon species labels;
- maximum scaled Boss level;
- Enrage interval and strength;
- Boss lifetime;
- tier weights;
- tier level bonuses;
- battle AI skill;
- Boss AI debug logging;
- shiny chance;
- guaranteed perfect IVs;
- Mega Evolution chance.

Default global values:

```text
Boss spawn chance:       1/512
Spawn cooldown:          15 minutes
Boss lifetime:           10 minutes
Maximum scaled level:    200
Enrage interval:         8 turns
Enrage strength:         +1 stage
Boss AI debug logging:   off
```

The configuration can be reloaded without restarting the server:

```text
/wb reloadconfig
```

Loot tables can be reloaded separately:

```text
/wb reload
```

### Admin commands

`/wildbosses` and `/wb` point to the same command tree.

```text
/wb help

/wb spawn <tier> [species] [respectCooldown]
/wb list
/wb info <target>
/wb teleport <target>
/wb despawn <target>
/wb forcebattle <target>

/wb loot <tier>
/wb setlevel <target> <level>
/wb glow <target>
/wb tier <target> <tier>

/wb cooldown
/wb leaderboard [tier]

/wb reload
/wb reloadconfig
/wb killall
/wb version
```

The suite is intended for administration, debugging, balance testing, and verifying Boss behavior without waiting for a natural spawn.

For `/wb spawn`, `respectCooldown` defaults to `false` so administrators can bypass the normal player cooldown while testing. Set it to `true` when testing the real player-facing spawn gate.

### Public API

`WildBossIntegrationApi` exposes lightweight Boss metadata for optional integrations:

```text
isBoss(PokemonEntity)
getTierName(PokemonEntity / UUID)
getTierNameByPokemonUuid(UUID)
getScaledLevel(PokemonEntity / UUID)
```

This is the bridge currently used by Battle Slider and CobbleTunes. Integrations should still treat WildBosses as optional and only call the API when the mod is present.

Other server-side mods can intercept generated Boss rewards through `WildBossLootAwardEvent`.

The event exposes:

```text
entityUuid
player
level
position
speciesName
tierName
stacks
claimed
```

A Kotlin integration can subscribe to the event and claim responsibility for the reward:

```kotlin
WildBossLootEvents.subscribe { event ->
    val bossUuid = event.entityUuid
    val player = event.player
    val rewards = event.stacks

    // Queue, transform, or otherwise resolve the reward here.

    event.claim()
}
```

`claim()` should only be called after the integration has actually taken responsibility for resolving the reward.

### Building

Java 21 is required. From the project root:

```powershell
./gradlew clean build
```

The JAR will be generated in:

```text
build/libs/
```

### Roadmap

WildBosses is still in development. Current areas planned for continued work include:

- further MegaShowdown compatibility and testing;
- possible **Myths and Legends** integration for legendary/mythical rewards;
- additional balancing and compatibility testing;
- a Cobblemon `1.8` port after the `1.7.3` release is stabilized.

### License

Available under the MIT license.

---

## Português

### O que é?

**WildBosses** adiciona Pokémon Boss raros que vagam naturalmente pelo mundo do [Cobblemon](https://cobblemon.com/).

Em vez de criar encontros separados ou roteirizados, o WildBosses ocasionalmente promove um Pokémon selvagem comum a **Boss**, dando a ele seu próprio tier, identidade visual, dificuldade, escalonamento de batalha e conjunto de recompensas.

Os Bosses foram pensados como uma atividade PvE de endgame. O nível real fica escondido até a batalha, a dificuldade escala de acordo com o jogador que os desafia e os tiers mais fortes podem ultrapassar com folga o nível 100.

O WildBosses funciona por cima do Cobblemon, sem substituir ou criar um fork de seus sistemas principais.

### Capturas de tela

As capturas abaixo seguem a mesma ordem em que um jogador normalmente descobriria e enfrentaria um Boss.

#### Um Boss apareceu

Quando um Boss spawna por perto, o jogador recebe uma mensagem no chat. Você sabe que algo raro apareceu — mas ainda precisa encontrá-lo.

<!--
SCREENSHOT 01 — Aviso de spawn
Imagem recomendada: capture a mensagem do WildBosses no chat antes do Boss ficar visível.
Mantenha HUD e ambiente normal do jogo para o encontro parecer natural.

Arquivo: docs/images/01-boss-spawn.png
Descomente após adicionar a imagem:
![Um Wild Boss apareceu por perto](docs/images/01-boss-spawn.png)
-->

#### Tem algo por perto

Todo Boss brilha na cor de seu tier. O contorno pode ser visto através de paredes e debaixo d'água, servindo como pista quando o Boss está próximo, mas ainda fora de vista.

<!--
SCREENSHOT 02 — Boss atrás da parede
Imagem recomendada: fique do outro lado de uma parede, relevo, caverna ou construção enquanto o brilho do Boss aparece claramente através dela.
Um Legendary ou Mythic deixará essa imagem especialmente marcante.

Arquivo: docs/images/02-boss-through-wall.png
Descomente após adicionar a imagem:
![Wild Boss brilhando através de uma parede](docs/images/02-boss-through-wall.png)
-->

#### Encontrando o Boss

Ao chegar até ele, o Boss pode ser visto normalmente no overworld — mas seu nível real continua escondido como `Lv. ??` até alguém aceitar a luta.

<!--
SCREENSHOT 03 — Primeiro encontro
Imagem recomendada: a primeira visão clara depois de seguir o brilho.
Mantenha parte do terreno no enquadramento para parecer uma descoberta, não uma imagem montada.
Garanta que o nome do Boss e Lv. ?? estejam legíveis.

Arquivo: docs/images/03-boss-found.png
Descomente após adicionar a imagem:
![Encontrando um Wild Boss](docs/images/03-boss-found.png)
-->

#### Bosses Legendary & Mythic

Os tiers mais altos são propositalmente raros. O brilho mais marcante, os atributos melhores, a chance maior de shiny e o acesso às mecânicas mais perigosas fazem esses encontros se destacarem de um spawn selvagem normal.

<!--
SCREENSHOT 04 — Showcase principal
Imagem recomendada: um Boss Legendary ou Mythic em um local bonito do overworld.
Mostre o modelo completo, brilho do tier, nome e Lv. ??.
Evite mensagens de admin/debug nessa imagem — ela deve ser o screenshot principal e mais polido do mod.

Arquivo: docs/images/04-mythic-overworld.png
Descomente após adicionar a imagem:
![Um Wild Boss Mythic no overworld](docs/images/04-mythic-overworld.png)
-->

#### Escalonamento dinâmico

O nível real do Boss é calculado quando a batalha começa. Jogadores avançados podem enfrentar Bosses muito acima da progressão normal de nível 100 do Cobblemon.

<!--
SCREENSHOT 05 — Batalha nível 100+
Imagem recomendada: batalha contra um Boss Epic, Legendary ou Mythic no Lv. 101+.
Tente mostrar ao mesmo tempo a barra de vida do Boss, nível revelado, seu Pokémon e a interface normal da batalha.

Arquivo: docs/images/05-scaled-battle.png
Descomente após adicionar a imagem:
![Batalha dinamicamente escalonada acima do nível 100](docs/images/05-scaled-battle.png)
-->

#### Enrage & Mega Evolução

As batalhas ficam mais perigosas quando se prolongam. Bosses Rare+ acumulam poder e entram em **Enrage** periodicamente, enquanto encontros elegíveis de tiers altos também podem **Mega Evoluir** quando há dados compatíveis de Mega Evolução disponíveis.

<!--
SCREENSHOT 06 — Enrage + Mega Evolução
Imagem recomendada: Boss Legendary/Mythic Mega Evoluído durante um ciclo de Enrage.
Melhor cenário: Boss Mega Lv. 100+ + barra de vida + mensagem de Enrage visível na mesma imagem.
Se ficar visualmente carregado demais, divida em duas screenshots.

Arquivo: docs/images/06-mega-enrage.png
Descomente após adicionar a imagem:
![Boss Mega Evoluído durante a mecânica de Enrage](docs/images/06-mega-enrage.png)
-->

#### Recompensas do Boss

Cada tier possui sua própria tabela de recompensas. Com o **Cobblemon Loot Menu** instalado, o loot normal dos tiers do WildBosses pode ser combinado ao loot normal da espécie e resolvido em uma única seleção controlada pelo servidor.

**Mega Stones são a exceção.** Quando o jogador recebe uma por derrotar um Boss que realmente Mega Evoluiu, a pedra é entregue **diretamente ao jogador** e nunca passa pelo Loot Menu. Mega Stones são recompensas raras e únicas de progressão, não loot comum para seleção.

<!--
SCREENSHOT 07 — Integração com Loot Menu
Imagem recomendada: tela de seleção depois de derrotar um Boss Legendary ou Mythic.
Use um roll com vários itens visualmente diferentes para deixar clara a diferença entre loot selvagem normal e recompensa de Boss.

Arquivo: docs/images/07-loot-menu.png
Descomente após adicionar a imagem:
![Recompensas do WildBosses no Cobblemon Loot Menu](docs/images/07-loot-menu.png)
-->

#### Progressão

O WildBosses também possui sua própria árvore de advancements por derrotar tiers, sobreviver a encontros especiais, caçar vários Bosses, encontrar Bosses shiny e interagir com o sistema de Mega Evolução.

<!--
SCREENSHOT 08 — Advancements
Imagem recomendada: abra a aba do WildBosses depois de desbloquear vários advancements conectados.
Uma árvore parcialmente concluída costuma ficar mais interessante do que uma completamente vazia ou 100% concluída.

Arquivo: docs/images/08-advancements.png
Descomente após adicionar a imagem:
![Progressão de advancements do WildBosses](docs/images/08-advancements.png)
-->

### Como funciona um encontro?

```text
um Pokémon selvagem comum spawna
    -> WildBosses rola a chance de Boss
    -> um tier é selecionado
    -> o Pokémon se torna um Boss
    -> seu nível real fica escondido como Lv. ??
    -> um jogador encontra e desafia o Boss
    -> o nível é escalonado para aquele jogador
    -> o encontro é forçado para 1v1
    -> Bosses Rare+ podem ativar Enrage em batalhas longas
    -> Bosses elegíveis de tier alto podem Mega Evoluir
    -> a vitória rola a recompensa do tier
```

O Boss não fica permanentemente vinculado ao jogador que causou seu spawn. Outros jogadores também podem encontrá-lo e enfrentar o mesmo Boss.

### Tiers de Boss

Existem cinco tiers:

| Tier | Peso padrão | Bônus de nível | IA | Chance shiny | IVs perfeitos | Chance Mega* |
|---|---:|---:|---:|---:|---|---:|
| **Uncommon** | 54 | +5 | 2 | 0% | Não | 0% |
| **Rare** | 30 | +10 | 3 | 0% | Não | 0% |
| **Epic** | 10 | +15 | 4 | 0% | Sim | 0% |
| **Legendary** | 5 | +20 | 5 | 25% | Sim | 50% |
| **Mythic** | 1 | +25 | 5 | 45% | Sim | 100% |

Os pesos padrão somam 100 e, por isso, podem ser lidos diretamente como a distribuição dos tiers **depois que um spawn selvagem já foi escolhido para virar Boss**. Eles não representam a chance de qualquer Pokémon do mundo virar um Boss.

\* A Mega Evolução só se aplica quando o Boss é elegível e existem dados compatíveis de Mega Evolução disponíveis.

Todos os valores da tabela podem ser configurados.

### Recursos

- cinco tiers de **Uncommon** até **Mythic**;
- chance configurável de spawns selvagens comuns virarem Bosses;
- aviso no chat quando um Boss aparece por perto;
- nível oculto no overworld como `Lv. ??`;
- escalonamento dinâmico baseado no Pokémon mais forte da party do desafiante;
- nível máximo configurável, com padrão de **200**;
- encontros **1v1** forçados;
- IA de Boss focada em agressividade, priorizando KOs, STAB, efetividade de tipo, cobertura, precisão e golpes de prioridade;
- IA ciente de golpes atrasados como **Future Sight** e **Doom Desire**, evitando gastar turnos seguidos em dano que ainda está pendente;
- Bosses Epic+ são forçados para sua forma evolutiva final;
- Bosses Epic+ recebem IVs perfeitos por padrão;
- Legendary e Mythic recebem chance aumentada de shiny por padrão;
- brilho colorido por tier, visível através de paredes e debaixo d'água;
- mecânica configurável de **Enrage** para Bosses Rare+ em batalhas longas;
- comportamento opcional de Mega Evolução para Bosses elegíveis de tier alto;
- cinco loot tables JSON padrão do Minecraft, uma para cada tier;
- cooldown de spawn persistente por jogador;
- recuperação do estado dos Bosses após reinicializações de servidor ou mundo;
- leaderboard persistente de Bosses derrotados;
- árvore própria de advancements;
- comandos administrativos e de teste em `/wildbosses` e `/wb`;
- API pública de metadados do Boss para integrações opcionais;
- integração leve com **Cobblemon Battle Slider** e **CobbleTunes** quando instalados;
- evento público de recompensa para integração com outros mods server-side.

### Escalonamento dinâmico de nível

Um Boss não possui um único nível permanente de batalha.

Quando o jogador inicia o encontro, o WildBosses verifica o Pokémon mais forte da party e adiciona o bônus configurado para o tier do Boss.

Por exemplo, usando a configuração padrão:

```text
Pokémon mais forte:      Lv. 72
Bônus do tier Epic:      +15
--------------------------------
Nível do Boss:           Lv. 87
```

Antes da batalha, o nome no overworld continua mostrando `Lv. ??`.

Acima do nível 100, o Cobblemon continua usando as fórmulas normais para a linha completa de stats — **HP, Attack, Defense, Sp. Atk, Sp. Def e Speed** escalam com o nível efetivo do Boss. O WildBosses não aplica um segundo multiplicador escondido por cima disso.

Se outro jogador desafiar o mesmo Boss mais tarde, o nível é calculado novamente a partir da party desse novo desafiante. Assim, o mesmo encontro continua relevante para jogadores em pontos diferentes da progressão do servidor.

### Regras de batalha

Encontros contra Boss são forçados para **1v1**.

Se o jogador vencer, o Boss é derrotado e a recompensa do tier é rolada.

Se o jogador perder ou fugir, o Boss volta ao HP máximo e permanece disponível para outra tentativa. Um desafiante posterior recebe um novo cálculo de nível baseado na própria party.

Bosses não podem ser capturados.

### IA agressiva dos Bosses

O WildBosses mantém os Bosses propositalmente diferentes de treinadores competitivos. Eles não jogam pensando em estratégia de longo prazo: se comportam como criaturas selvagens perigosas tentando encerrar a luta.

A IA pontua os ataques disponíveis de acordo com o confronto atual e dá um peso enorme para KOs imediatos. Ela também considera STAB, efetividade de tipo, stats físicos/especiais, precisão, prioridade, golpes multi-hit e imunidades de tipo comuns por habilidade. Golpes arriscados que reduzem os próprios stats, como Close Combat ou Draco Meteor, podem ser usados quando o ganho compensa em vez de ficarem permanentemente bloqueados.

Valores maiores de `aiSkill` deixam o Boss mais consistente:

- tiers menores ainda podem escolher um golpe bom em vez do golpe perfeito;
- Bosses Epic+ podem receber um único golpe de setup agressivo quando a espécie possui uma opção adequada;
- Legendary/Mythic podem usar uma única recuperação quando estiverem muito feridos e não houver KO imediato;
- setup é limitado e recuperação é propositalmente desesperada, então o Boss continua agressivo em vez de virar um treinador de stall.

O moveset é reconstruído no início da batalha. Bosses com skill maior aceitam um pouco mais de risco de precisão em troca de golpes fortes e podem manter um golpe de prioridade útil para finalizar alvos enfraquecidos.

Todos os tiers também têm **cobertura contra perigo**. Se a espécie possui uma fraqueza defensiva de 4×, o WildBosses procura entre seus golpes legais uma opção boa para punir Pokémon daquele tipo ameaçador. Não existe hardcode por espécie: Omastar pode priorizar cobertura Ice contra Grass, Swampert pode fazer o mesmo, Gyarados pode valorizar Ground contra Electric e a mesma lógica vale para outras tipagens. Cobertura fraca ou incompatível com os stats ofensivos ainda é descartada em vez de enfiar golpe ruim no set.

O nível de cautela escala por tier:

| Tier | Cobertura contra fraqueza 4× | Reação a golpe anunciado |
|---|---|---|
| **Uncommon** | Preferência moderada | Normalmente reage |
| **Rare** | Preferência forte | Reage na maior parte das vezes |
| **Epic** | Quase garantida quando existe um golpe bom | Quase sempre reage |
| **Legendary** | Garantida quando existe um golpe bom | Consciência perfeita |
| **Mythic** | Garantida quando existe um golpe bom | Consciência perfeita |

A IA também consegue ler um golpe ofensivo de dois turnos que foi claramente preparado no turno anterior. Um golpe recebido em 2× faz um Boss atento evitar setup ganancioso e puxar mais para seu melhor ataque. Um golpe em 4× vira perigo real: setup/recuperação são ignorados e o Boss usa seu melhor ataque imediato que não seja atrasado. Legendary e Mythic sempre respeitam ameaças anunciadas; tiers menores ainda podem dar uma vacilada de vez em quando.

Golpes atrasados são tratados como dano atrasado de verdade. **Future Sight** e **Doom Desire** recebem menos prioridade no moveset e no score, não ganham o bônus normal de KO imediato e ficam bloqueados enquanto o golpe anterior ainda está pendente. O Boss ainda pode usá-los quando fizer sentido — só não vai torrar dois turnos seguidos à toa.

Para testar, ative `bossAiDebugLogging` no config ou **Logs de Debug da IA dos Bosses** no Mod Menu. Só iniciar uma batalha já mostra no console que a IA foi aplicada e qual moveset foi gerado; com o debug ligado, cada turno também mostra os scores, dano estimado, multiplicador de tipo, checagem de KO ao acertar, decisões de setup/recuperação e o golpe escolhido.

### Enrage

Batalhas contra Boss não foram feitas para serem prolongadas indefinidamente.

Por padrão, Bosses **Rare, Epic, Legendary e Mythic** entram em Enrage a cada **8 turnos**. Bosses Uncommon não usam Enrage. Um turno antes do próximo Enrage, o jogador recebe um aviso de que o Boss está acumulando poder.

Quando o Enrage ativa, o Boss recebe a quantidade configurada de estágios em Attack, Defense, Sp. Atk, Sp. Def e Speed. O padrão é **+1 estágio** por Enrage, mantendo o limite normal de estágios da batalha.

Tanto o intervalo quanto a força do Enrage podem ser configurados.

### Mega Evolução

A build atual contém suporte opcional para encontros com Bosses Mega Evoluídos através de dados compatíveis de Mega Evolução, como os fornecidos pelo **MegaShowdown**.

Por padrão:

- Uncommon, Rare e Epic não tentam Mega Evoluir;
- Legendary possui `50%` de chance configurada de Mega Evolução;
- Mythic possui `100%` de chance configurada de Mega Evolução.

O Boss ainda precisa ser elegível para uma Mega Evolução compatível. A porcentagem por si só não cria uma Mega forma para uma espécie que não possui uma.

#### Recompensas de Mega Stone

As Mega Stones estão vinculadas a Bosses que **realmente Mega Evoluem durante a batalha**. Derrotar um Boss elegível que não chegou a Mega Evoluir não concede sua Mega Stone.

Quando recebida, a Mega Stone é entregue **diretamente ao jogador**. Ela não entra no pool normal de loot do Boss e não é enviada para o **Cobblemon Loot Menu**.

Cada Mega Stone também é uma **recompensa única por espécie, por jogador**. Depois que um jogador recebe a Mega Stone associada àquela espécie, derrotar outros Bosses Mega Evoluídos da mesma espécie não entrega outra cópia.

Por exemplo:

```text
1º Mega Pidgeot derrotado -> Pidgeotite recebida
2º Mega Pidgeot derrotado -> sem Pidgeotite
3º Mega Pidgeot derrotado -> sem Pidgeotite
...
Mega Charizard derrotado  -> sua Mega Stone ainda pode ser recebida
```

Isso transforma as Mega Stones em recompensas de progressão por encontrar e derrotar diferentes espécies de Bosses capazes de Mega Evoluir, em vez de uma fonte de farm repetível.

O WildBosses também possui progressão relacionada a derrotar Bosses Mega Evoluídos e receber Mega Stones.

### Spawn e ciclo de vida

A chance padrão de promoção para Boss é:

```text
1 / 512
```

Depois que um jogador causa o spawn de um Boss, ele entra em um cooldown persistente. O padrão é:

```text
15 minutos
```

Esse cooldown apenas impede aquele jogador de causar outro spawn. Ele ainda pode enfrentar normalmente um Boss que já existe por perto ou que apareceu para outro jogador.

Um Boss que nunca foi enfrentado normalmente permanece no mundo por aproximadamente:

```text
10 minutos
```

antes de desaparecer automaticamente.

O estado dos Bosses ativos é recuperado após reloads do mundo, incluindo o tier rastreado e o brilho. Os cooldowns de spawn também sobrevivem a reinicializações do servidor.

### Restrições de espécies

Lendários, Míticos e Ultra Beasts são excluídos de virar WildBosses por padrão.

O WildBosses lê as labels das espécies fornecidas pelo próprio Cobblemon em vez de manter uma lista separada e hardcoded de Pokémon. As labels excluídas podem ser alteradas na configuração.

### Loot

Cada tier utiliza uma loot table padrão do Minecraft:

```text
data/wildbosses/loot_table/boss/uncommon.json
data/wildbosses/loot_table/boss/rare.json
data/wildbosses/loot_table/boss/epic.json
data/wildbosses/loot_table/boss/legendary.json
data/wildbosses/loot_table/boss/mythic.json
```

Os pools de recompensa escalam em quantidade e qualidade e podem incluir itens como:

- gemas de tipo;
- itens de evolução;
- itens equipáveis;
- itens de cura;
- relic coins;
- recompensas raras de tier alto.

Como as recompensas usam loot tables normais do Minecraft, administradores de servidor e autores de modpacks podem substituí-las ou modificá-las através de datapacks sem alterar o código do WildBosses.

#### Personalizando o loot dos Bosses com datapack

Não é necessário modificar o JAR do WildBosses para rebalancear as recompensas. Um datapack pode sobrescrever qualquer uma das cinco loot tables incluídas no mod.

No Minecraft `1.21.1`, crie um datapack dentro da pasta `datapacks` do mundo:

```text
<mundo>/
└─ datapacks/
   └─ meu_loot_wildbosses/
      ├─ pack.mcmeta
      └─ data/
         └─ wildbosses/
            └─ loot_table/
               └─ boss/
                  ├─ uncommon.json
                  ├─ rare.json
                  ├─ epic.json
                  ├─ legendary.json
                  └─ mythic.json
```

O namespace e os caminhos precisam ser mantidos. Para substituir uma tabela do WildBosses, use o namespace `wildbosses` e o mesmo caminho da tabela original incluída no mod.

Você só precisa incluir os tiers que deseja alterar. Por exemplo, um datapack que muda apenas o loot Mythic pode conter somente:

```text
data/wildbosses/loot_table/boss/mythic.json
```

Um `pack.mcmeta` mínimo para Minecraft `1.21.1` usa o formato de datapack `48`:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "Loot customizado do WildBosses"
  }
}
```

O fluxo mais simples é:

1. abra o JAR do WildBosses com um programa como 7-Zip;
2. copie o arquivo do tier desejado em `data/wildbosses/loot_table/boss/`;
3. coloque-o no mesmo caminho dentro do datapack;
4. altere pools, entries, weights, rolls, quantidades, conditions ou IDs dos itens;
5. execute `/reload` depois de adicionar ou alterar o datapack;
6. use `/wb loot <tier>` para testar rapidamente o pool resultante.

Como o datapack sobrescreve a mesma resource location, é possível rebalancear apenas um tier sem mexer nos outros ou recompilar o mod.

Para consultar o formato completo, veja o [guia de criação de datapacks da Minecraft Wiki](https://minecraft.wiki/w/Tutorials/Creating_a_data_pack) e a [referência de loot tables da Minecraft Wiki](https://minecraft.wiki/w/Loot_table).

> **Nota:** Mega Stones não fazem parte dessas loot tables de tier. Essa recompensa é tratada separadamente pelo WildBosses quando um Boss elegível realmente Mega Evolui, e cada espécie pode conceder sua pedra apenas uma vez por jogador.

### Advancements

O WildBosses inclui sua própria árvore de advancements.

A progressão atual inclui objetivos por:

- derrotar seu primeiro Boss;
- derrotar um Boss Uncommon, Rare, Epic, Legendary e Mythic;
- derrotar 5 Bosses;
- derrotar 25 Bosses;
- derrotar pelo menos um Boss de cada tier;
- presenciar e superar um Enrage;
- derrotar um Boss shiny;
- fugir com sucesso de um encontro contra Boss;
- derrotar um Boss que Mega Evoluiu;
- receber sua primeira Mega Stone de um Boss.

### Leaderboard

As vitórias contra Bosses são registradas persistentemente.

O leaderboard pode mostrar os maiores caçadores de Bosses no total ou filtrar o ranking por tier:

```text
/wb leaderboard
/wb leaderboard mythic
```

### Integração com Cobblemon Loot Menu

O WildBosses expõe um evento público que permite que outro mod server-side assuma a responsabilidade por uma recompensa de Boss gerada antes que o próprio WildBosses a entregue normalmente.

O **Cobblemon Loot Menu** utiliza essa integração para combinar a recompensa normal do Boss com o loot normal da espécie:

```text
recompensas do tier WildBosses
        +
loot normal da espécie do Cobblemon
        ↓
uma única seleção de loot controlada pelo servidor
```

**Mega Stones ignoram essa integração de propósito.** Se o Boss derrotado realmente Mega Evoluiu e o jogador ainda pode receber a recompensa única daquela espécie, a pedra é entregue **diretamente ao jogador**. Ela nunca é adicionada à sessão do Loot Menu.

```text
loot normal do Boss -> Cobblemon Loot Menu
Mega Stone          -> jogador diretamente
```

Isso mantém as Mega Stones separadas do loot comum selecionável, já que são recompensas raras e únicas de progressão, recebidas apenas uma vez por espécie, por jogador.

Quando a integração assume a responsabilidade pelo loot normal, ela chama `claim()` no evento e o WildBosses não entrega os mesmos stacks uma segunda vez.

O evento atual também fornece o UUID da entidade Boss, permitindo que integrações associem com segurança a recompensa ao Pokémon que a gerou.

### Integrações opcionais entre mods

O WildBosses pode expor dados da batalha para outros mods sem transformá-los em dependências obrigatórias. A API atual informa se o Pokémon é um Boss, seu tier e o nível escalado atual da batalha.

#### Cobblemon Battle Slider

Com o **Cobblemon Battle Slider** instalado, encontros contra Boss recebem uma intro VS própria em vez de parecerem uma batalha selvagem comum.

- o lado do Boss usa a cor do tier;
- o nome aparece como `Mythic Boss Garchomp`;
- o nível aparece como `Scaled Level = Lv.125`;
- o modelo real do Pokémon é renderizado em 3D e ajustado automaticamente ao espaço;
- espécies minúsculas e gigantes usam o mesmo slot sem tabela manual por espécie;
- a name tag normal do Boss no mundo fica escondida durante a intro;
- jogador e treinadores podem usar bustos 3D controlados, com a skin 2D antiga como fallback;
- o WildBosses pula seu próprio título/subtítulo de encontro enquanto o Battle Slider estiver carregado, evitando que a tela antiga de `Boss Encounter!` apareça por alguns ticks antes da intro VS. O cry do Boss e a inicialização normal da batalha continuam intactos.

A integração é leve. O WildBosses continua funcionando normalmente sem o Battle Slider, incluindo seu título/subtítulo de encontro original.

#### CobbleTunes

Com o **CobbleTunes** instalado, a batalha do Boss pode receber um tema baseado na região de origem da espécie.

O CobbleTunes monta um pool regional mais seguro em vez de jogar qualquer tema lendário na roleta:

1. tema de rival/PvP da região — maior prioridade;
2. tema lendário genérico da região — prioridade média quando existir;
3. remix regional do World Tournament de Black/White — prioridade menor;
4. temas de encontros lendários específicos — excluídos.

O peso muda conforme o tier, e o CobbleTunes faz um reroll quando cair exatamente na mesma faixa usada no último Boss daquela região e houver outra opção. A integração também é opcional; o WildBosses não inclui áudio extra.

### Requisitos

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.17.2+`
- Fabric Language Kotlin `1.13.6+kotlin.2.2.20+`
- Cobblemon `1.7.3+`

Integrações opcionais:

- Mod Menu `11.0.3+` + YetAnotherConfigLib (YACL) `3.8.1+` para a tela de config;
- Cobblemon Battle Slider para a intro VS dos Bosses;
- CobbleTunes para música regional de Boss.

O WildBosses é desenvolvido para Fabric. As dependências exigidas pelo próprio Cobblemon continuam sendo necessárias. Mod Menu e YACL só são necessários para a tela de configuração dentro do jogo.

### Instalação

1. Instale Fabric Loader, Fabric Language Kotlin, Cobblemon e as dependências exigidas pelo Cobblemon.
2. Coloque o JAR do WildBosses na pasta `mods`.
3. Inicie o jogo ou servidor uma vez para gerar a configuração.
4. Ajuste as opções geradas, caso deseje.

### Configuração

Na primeira inicialização, o mod cria:

```text
config/wildbosses/wildbosses.json
```

Com **Mod Menu + YACL** instalados no cliente, as mesmas opções também podem ser alteradas em:

```text
Mods -> WildBosses -> Configure
```

A tela edita o mesmo modelo de `wildbosses.json`; não existe uma segunda configuração separada. No singleplayer, o servidor integrado usa esses valores diretamente. Em servidores dedicados, as regras continuam sendo controladas pelo arquivo do servidor, então alterar o JSON local do cliente não sobrescreve a configuração do servidor.

O arquivo gerado contém comentários explicando as opções disponíveis e controla:

- chance global de Boss;
- cooldown de spawn por jogador;
- labels de espécies excluídas;
- nível máximo escalonado;
- intervalo e força do Enrage;
- tempo de vida do Boss;
- peso dos tiers;
- bônus de nível;
- habilidade da IA de batalha;
- logs de debug da IA dos Bosses;
- chance de shiny;
- IVs perfeitos garantidos;
- chance de Mega Evolução.

Valores globais padrão:

```text
Chance de Boss:          1/512
Cooldown de spawn:       15 minutos
Tempo de vida:           10 minutos
Nível máximo:            200
Intervalo de Enrage:     8 turnos
Força do Enrage:         +1 estágio
Debug da IA dos Bosses:  desligado
```

A configuração pode ser recarregada sem reiniciar o servidor:

```text
/wb reloadconfig
```

As loot tables podem ser recarregadas separadamente:

```text
/wb reload
```

### Comandos administrativos

`/wildbosses` e `/wb` apontam para a mesma árvore de comandos.

```text
/wb help

/wb spawn <tier> [species] [respectCooldown]
/wb list
/wb info <target>
/wb teleport <target>
/wb despawn <target>
/wb forcebattle <target>

/wb loot <tier>
/wb setlevel <target> <level>
/wb glow <target>
/wb tier <target> <tier>

/wb cooldown
/wb leaderboard [tier]

/wb reload
/wb reloadconfig
/wb killall
/wb version
```

Os comandos são voltados para administração, debugging, testes de balanceamento e verificação do comportamento dos Bosses sem precisar esperar por um spawn natural.

No `/wb spawn`, `respectCooldown` é `false` por padrão para que administradores possam ignorar o cooldown normal durante testes. Use `true` para testar o mesmo bloqueio de spawn visto por jogadores comuns.

### API pública

`WildBossIntegrationApi` expõe metadados leves do Boss para integrações opcionais:

```text
isBoss(PokemonEntity)
getTierName(PokemonEntity / UUID)
getTierNameByPokemonUuid(UUID)
getScaledLevel(PokemonEntity / UUID)
```

Essa é a ponte usada atualmente pelo Battle Slider e pelo CobbleTunes. A integração deve continuar tratando o WildBosses como opcional e só chamar a API quando o mod estiver presente.

Outros mods server-side podem interceptar recompensas geradas por Boss através do `WildBossLootAwardEvent`.

O evento disponibiliza:

```text
entityUuid
player
level
position
speciesName
tierName
stacks
claimed
```

Uma integração Kotlin pode assinar o evento e assumir a responsabilidade pela recompensa:

```kotlin
WildBossLootEvents.subscribe { event ->
    val bossUuid = event.entityUuid
    val player = event.player
    val rewards = event.stacks

    // Enfileire, transforme ou resolva a recompensa aqui.

    event.claim()
}
```

`claim()` só deve ser chamado depois que a integração realmente assumir a responsabilidade por resolver aquela recompensa.

### Compilação

É necessário utilizar Java 21. Na raiz do projeto:

```powershell
./gradlew clean build
```

O JAR será gerado em:

```text
build/libs/
```

### Roadmap

O WildBosses ainda está em desenvolvimento. As áreas planejadas atualmente incluem:

- continuação dos testes e da compatibilidade com MegaShowdown;
- possível integração com **Myths and Legends** para recompensas lendárias/míticas;
- mais testes de balanceamento e compatibilidade;
- port para Cobblemon `1.8` depois que a versão para `1.7.3` estiver estabilizada.

### Licença

Disponível sob a licença MIT.
