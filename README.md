# WildBosses 🐾⚔️

*Read this in [English](#english) | Leia em [Português](#português)*

---

## English

### Overview
**WildBosses** is an ARPG-inspired endgame addon for Cobblemon (Fabric/Kotlin). It introduces dynamic Pokémon Boss encounters that scale according to the player's level, focusing on valuable loot drops and a strictly controlled server economy, especially regarding Mega Evolutions.

### Key Features
*   **Dynamic Spawn & Lifecycle:** Wild Pokémon have a 1/128 chance to spawn as a Boss. Bosses roam peacefully for exactly 10 minutes (12,000 ticks) before despawning to prevent server lag. They cannot be captured (`isCapturable = false`).
*   **Hidden Level & Scaling:** Boss levels are hidden ("Level ??") in the overworld. Upon starting a battle, the Boss's level is calculated based on the player's strongest party Pokémon, adding +10 to +50 levels depending on the Boss tier (Uncommon to Mythic). Stats (HP/Atk) receive massive multipliers to compensate for the level 100 cap.
*   **Instanced 1v1 Battles:** Battles are strictly 1v1. If a player loses or runs, the Boss resets to 100% HP in the overworld, ready for the next challenger with recalculated scaling.
*   **Tiered Economy & Loot:** Custom JSON loot tables provide scaling rewards. Epic drops focus on breeding (Destiny Knot, Mints), Legendary on stat maximization (Bottle Caps), and Mythic on Mega Stones.
*   **Anti-Exploit Mega Stones (NBT Memory):** Defeating a Mythic Boss guarantees a Mega Stone drop for that species, but only once per player (tracked via NBT tags). Subsequent kills of the same Mythic species reward premium alternative items (e.g., Gold Bottle Caps) to prevent infinite farming and protect the server economy.

### Setup
For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

### License
This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

---

## Português

### Visão Geral
**WildBosses** é um addon de endgame inspirado em ARPGs para o Cobblemon (Fabric/Kotlin). Ele introduz encontros dinâmicos com Pokémon Bosses que escalam de acordo com o nível do jogador, focando em drops valiosos e controle rígido da economia do servidor, especialmente em relação às Mega Evoluções.

### Funcionalidades Principais
*   **Spawn Dinâmico e Ciclo de Vida:** Pokémon selvagens têm 1/128 de chance de spawnar como Boss. Eles andam pacificamente por exatos 10 minutos (12.000 ticks) antes de sofrerem despawn para evitar lag no servidor. Eles não podem ser capturados (`isCapturable = false`).
*   **Nível Oculto e Escalonamento:** O nível do Boss fica oculto ("Level ??") no overworld. Ao iniciar a batalha, o nível é calculado com base no Pokémon mais forte da party do jogador, adicionando de +10 a +50 níveis dependendo do Tier (Uncommon a Mythic). Os status (HP/Atk) recebem multiplicadores absurdos para compensar o limite do nível 100.
*   **Batalhas Instanciadas 1v1:** As lutas são estritamente 1v1. Se o jogador perder ou fugir, o Boss reseta para 100% de HP no overworld, pronto para um novo desafiante com escalonamento recalculado.
*   **Economia e Loot por Tier:** Loot tables em JSON customizadas fornecem recompensas escalonadas. Tier Epic foca em breeding (Destiny Knot, Mints), Legendary em maximização de status (Bottle Caps), e Mythic foca em Mega Stones.
*   **Mega Stones Anti-Exploit (Memória NBT):** Derrotar um Boss Mítico garante o drop de uma Mega Stone daquela espécie, mas apenas uma vez por jogador (rastreado via tags NBT). Derrotar o mesmo Mítico no futuro recompensa o jogador com itens premium alternativos (ex: Gold Bottle Caps) para evitar farm infinito e proteger a economia.

### Setup
For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

### Licença
This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.