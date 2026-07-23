# WildBosses 🐾⚔️

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-Loom-DBB69B?logo=fabric&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)
![Cobblemon](https://img.shields.io/badge/Cobblemon-1.7.3-3E8E41)
![License](https://img.shields.io/badge/license-MIT-blue)

*Read this in [English](#english) | Leia em [Português](#português)*

---

## English

### Overview
**WildBosses** is an ARPG-inspired endgame addon for [Cobblemon](https://cobblemon.com/) (Fabric/Kotlin). It turns rare wild Pokémon spawns into dynamically scaled Boss encounters — hidden level, tier-based loot, smarter battle AI, and a persistent spawn cooldown system — without touching Cobblemon's core mechanics.

### The problem
Vanilla Cobblemon's wild encounters stay flat throughout a server's lifetime: a level-cap party has nothing left to chase once the main story/PvP loop is done, and there's no rare, high-stakes PvE goal built into the mod itself.

### The solution
A lightweight addon that occasionally promotes an ordinary wild spawn into a **Boss** — scaled live to the challenging player's own strongest Pokémon, dropping tier-appropriate rewards, and gated by a per-player cooldown so encounters stay meaningful instead of farmable.

### Architecture
Single-module Fabric mod (Architectury Loom, official Mojang mappings), built as a soft-dependent addon on top of Cobblemon rather than a fork:

- **Spawn layer** — hooks Cobblemon's own spawn event, rolls for boss promotion, applies the species blacklist.
- **Battle layer** — hooks battle start/end events to apply dynamic level scaling, tiered AI, and post-battle loot/HP logic.
- **Boss registry** — in-memory tracking of active bosses, with a `SavedData`-backed system for cross-restart spawn cooldowns and a recovery listener that restores boss state on world reload.
- **Data-driven loot** — standard Minecraft loot table JSON per tier, no custom parsing.

### Key Features

- [x] **Dynamic spawn & lifecycle** — wild Pokémon have a small chance to spawn as a Boss, roam peacefully for 10 minutes before despawning, and cannot be captured.
- [x] **Species blacklist** — Legendaries, Mythicals, and Ultra Beasts are automatically excluded from ever becoming a Boss, read directly from Cobblemon's own species labels.
- [x] **Hidden level & live scaling** — Boss level shows as `Lv. ??` in the overworld. On engaging a battle, the level is calculated from the challenging player's strongest party Pokémon plus a tier bonus (+10 to +50), can exceed the vanilla level-100 cap, and is revealed in chat the moment the fight starts.
- [x] **Forced 1v1** — if the player loses or flees, the Boss resets to full HP and stays available for the next challenger, rescaled fresh each time.
- [x] **Tier-scaled battle AI** — Bosses use a smarter, damage-calculating battle AI instead of default random move selection, with difficulty scaling by tier.
- [x] **Force-evolution** — Epic-tier Bosses and above always spawn in their final evolutionary form.
- [x] **Perfect IVs & shiny odds** — Epic+ Bosses guarantee maxed IVs; Legendary and Mythic Bosses carry a boosted shiny chance.
- [x] **Tier-colored glow** — every Boss glows in its tier's color, visible through walls and underwater.
- [x] **Tiered loot tables** — five fully custom loot tables (Uncommon → Mythic), scaling in item count, quality, and rarity: type gems, evolution items, held items, healing progression, relic coins, and a low-odds top-tier reward pool.
- [x] **Per-player spawn cooldown** — persists across server restarts; blocks further Bosses from spawning *for that player specifically*, without preventing others from engaging one that already spawned nearby.
- [x] **Boss-state recovery** — Bosses survive server restarts and world reloads without losing their tier, glow, or tracked state.
- [x] **Debug/admin command suite** — 15 commands under `/wildbosses` (alias `/wb`) to spawn, inspect, teleport to, force-battle, or clean up Bosses, and to test loot/cooldowns directly.

### Roadmap
- [ ] Mega Evolution integration (soft-dependency on MegaShowdown)
- [ ] "Myths and Legends" soft-dependency — legendary/mythical Boss loot that summons the matching species

### Setup
For setup instructions, see the [Fabric Documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) page for the IDE you're using. Requires Cobblemon 1.7.3+ and Fabric Language Kotlin.

### License
This project is available under the MIT license.

---

## Português

### Visão geral
**WildBosses** é um addon de endgame inspirado em ARPGs para o [Cobblemon](https://cobblemon.com/) (Fabric/Kotlin). Ele transforma spawns raros de Pokémon selvagens em encontros de **Boss** dinamicamente escalonados — nível oculto, loot por tier, IA de batalha mais inteligente e um sistema de cooldown de spawn persistente — sem alterar as mecânicas centrais do Cobblemon.

### O problema
Os encontros selvagens padrão do Cobblemon permanecem estáticos durante toda a vida do servidor: uma party no nível máximo não tem mais nada a perseguir depois que o loop principal de história/PvP acaba, e o próprio mod não oferece um objetivo PvE raro e de alto risco.

### A solução
Um addon leve que ocasionalmente promove um spawn selvagem comum a **Boss** — escalonado em tempo real de acordo com o Pokémon mais forte do jogador desafiante, com recompensas adequadas ao tier, e limitado por um cooldown por jogador para que os encontros continuem significativos em vez de farmáveis.

### Arquitetura
Mod Fabric de módulo único (Architectury Loom, mappings oficiais da Mojang), construído como um addon com dependência opcional do Cobblemon, e não como um fork:

- **Camada de spawn** — utiliza o próprio evento de spawn do Cobblemon, sorteia a promoção a Boss e aplica a blacklist de espécies.
- **Camada de batalha** — utiliza os eventos de início/fim de batalha para aplicar o escalonamento dinâmico de nível, a IA por tier e a lógica de loot/HP pós-batalha.
- **Registro de bosses** — rastreamento em memória dos bosses ativos, com um sistema baseado em `SavedData` para cooldowns de spawn que sobrevivem a reinicializações, e um listener de recuperação que restaura o estado do boss ao recarregar o mundo.
- **Loot orientado a dados** — loot tables padrão do Minecraft em JSON por tier, sem parsing customizado.

### Funcionalidades principais

- [x] **Spawn dinâmico e ciclo de vida** — Pokémon selvagens têm uma pequena chance de spawnar como Boss, andam pacificamente por 10 minutos antes de sofrerem despawn, e não podem ser capturados.
- [x] **Blacklist de espécies** — Lendários, Míticos e Ultra Beasts são automaticamente excluídos de virarem Boss, lidos diretamente das labels de espécie do próprio Cobblemon.
- [x] **Nível oculto e escalonamento ao vivo** — o nível do Boss aparece como `Lv. ??` no overworld. Ao iniciar uma batalha, o nível é calculado a partir do Pokémon mais forte da party do jogador desafiante mais um bônus de tier (+10 a +50), podendo ultrapassar o limite vanilla de nível 100, e é revelado no chat assim que a luta começa.
- [x] **1v1 forçado** — se o jogador perder ou fugir, o Boss volta ao HP máximo e continua disponível para o próximo desafiante, recalculado a cada tentativa.
- [x] **IA de batalha escalonada por tier** — os Bosses usam uma IA de batalha mais inteligente, com cálculo de dano real, em vez da seleção aleatória de golpes padrão, com dificuldade escalando por tier.
- [x] **Evolução forçada** — Bosses de tier Epic ou superior sempre spawnam em sua forma evolutiva final.
- [x] **IVs perfeitos e chance de shiny** — Bosses Epic+ garantem IVs máximos; Bosses Legendary e Mythic carregam uma chance de shiny aumentada.
- [x] **Brilho colorido por tier** — todo Boss brilha na cor do seu tier, visível através de paredes e debaixo d'água.
- [x] **Loot tables por tier** — cinco loot tables totalmente customizadas (Uncommon → Mythic), escalando em quantidade de itens, qualidade e raridade: gemas de tipo, itens de evolução, itens equipáveis, progressão de cura, relic coins e um pool de recompensa de topo com baixa chance.
- [x] **Cooldown de spawn por jogador** — persiste entre reinicializações do servidor; impede que novos Bosses spawnem *especificamente para aquele jogador*, sem impedir que outros enfrentem um Boss que já tenha spawnado por perto.
- [x] **Recuperação de estado dos bosses** — os Bosses sobrevivem a reinicializações do servidor e recarregamentos de mundo sem perder seu tier, brilho ou estado rastreado.
- [x] **Suite de comandos de debug/admin** — 15 comandos sob `/wildbosses` (atalho `/wb`) para spawnar, inspecionar, teleportar até, forçar batalha ou limpar Bosses, além de testar loot/cooldowns diretamente.

### Roadmap
- [ ] Integração com Mega Evolução (dependência opcional do MegaShowdown)
- [ ] Dependência opcional "Myths and Legends" — loot de Boss lendário/mítico que invoca a espécie correspondente

### Configuração
Para instruções de configuração, veja a página de [Documentação do Fabric](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) referente à IDE que você está usando. Requer Cobblemon 1.7.3+ e Fabric Language Kotlin.

### Licença
Este projeto está disponível sob a licença MIT.
