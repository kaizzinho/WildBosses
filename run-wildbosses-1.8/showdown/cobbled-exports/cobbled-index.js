"use strict";
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __export = (target, all) => {
  for (var name in all)
    __defProp(target, name, { get: all[name], enumerable: true });
};
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);
var cobbled_index_exports = {};
__export(cobbled_index_exports, {
  afterCobbledSpeciesInit: () => afterCobbledSpeciesInit,
  getCobbledAbilityIds: () => getCobbledAbilityIds,
  getCobbledItemIds: () => getCobbledItemIds,
  getCobbledMoves: () => getCobbledMoves,
  getTypeChart: () => getTypeChart,
  receiveAbilityData: () => receiveAbilityData,
  receiveBagItemData: () => receiveBagItemData,
  receiveHeldItemData: () => receiveHeldItemData,
  receiveMoveData: () => receiveMoveData,
  receiveSpeciesData: () => receiveSpeciesData,
  sendBattleMessage: () => sendBattleMessage,
  startBattle: () => startBattle
});
module.exports = __toCommonJS(cobbled_index_exports);
var import_battle_stream = require("../sim/battle-stream");
var import_dex = require("../sim/dex");
var import_cobblemon = require("../sim/cobblemon/cobblemon");
const battleMap = /* @__PURE__ */ new Map();
const toID = import_dex.Dex.toID;
function startBattle(graalShowdown, battleId, requestMessages) {
  const battleStream = new import_battle_stream.BattleStream();
  battleMap.set(battleId, battleStream);
  try {
    for (const element of requestMessages) {
      battleStream.write(element);
    }
  } catch (err) {
    graalShowdown.log(err.stack);
  }
  (async () => {
    for await (const output of battleStream) {
      graalShowdown.sendFromShowdown(battleId, output);
    }
  })();
}
function sendBattleMessage(battleId, messages) {
  const battleStream = battleMap.get(battleId);
  messages.forEach((msg) => {
    battleStream.write(msg);
  });
}
function getCobbledAbilityIds() {
  const base = import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).abilities.all();
  const addon = import_cobblemon.Cobblemon.abilityRegistry.all();
  return JSON.stringify(base.concat(addon).map((ability) => ability.id));
}
function getCobbledItemIds() {
  return JSON.stringify(import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).items.all().map((item) => item.id));
}
function getCobbledMoves() {
  const base = import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).moves.all();
  const addon = import_cobblemon.Cobblemon.moveRegistry.all();
  return JSON.stringify(base.concat(addon));
}
function getTypeChart() {
  return JSON.stringify(import_dex.Dex.data.TypeChart);
}
function receiveAbilityData(abilityId, ability) {
  import_cobblemon.Cobblemon.abilityRegistry.register(ability, toID(abilityId));
}
function receiveBagItemData(itemId, bagItem) {
  import_cobblemon.Cobblemon.bagItemRegistry.register(bagItem, toID(itemId));
}
function receiveHeldItemData(itemId, heldItem) {
  import_cobblemon.Cobblemon.heldItemRegistry.register(heldItem, toID(itemId));
}
function receiveMoveData(moveId, move) {
  import_cobblemon.Cobblemon.moveRegistry.register(move, toID(moveId));
}
function receiveSpeciesData(speciesArray) {
  import_cobblemon.Cobblemon.speciesRegistry.reset();
  speciesArray.forEach((speciesJson) => {
    const speciesData = JSON.parse(speciesJson);
    import_cobblemon.Cobblemon.speciesRegistry.register(speciesData);
  });
}
function afterCobbledSpeciesInit() {
  import_dex.Dex.modsLoaded = false;
  import_dex.Dex.includeMods();
}
//# sourceMappingURL=cobbled-index.js.map
