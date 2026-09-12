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
var registries_exports = {};
__export(registries_exports, {
  AbilityRegistry: () => AbilityRegistry,
  BagItemRegistry: () => BagItemRegistry,
  CobbleRegistry: () => CobbleRegistry,
  HeldItemRegistry: () => HeldItemRegistry,
  MoveRegistry: () => MoveRegistry,
  SpeciesRegistry: () => SpeciesRegistry
});
module.exports = __toCommonJS(registries_exports);
var import_dex_abilities = require("../dex-abilities");
var import_dex_moves = require("../dex-moves");
var import_dex_items = require("../dex-items");
var import_dex_species = require("../dex-species");
var import_dex = require("../dex");
var import_cobblemon = require("./cobblemon");
class CobbleRegistry {
  constructor() {
    this.contents = /* @__PURE__ */ new Map();
  }
  all() {
    return Array.from(this.contents.values());
  }
  get(id) {
    return this.contents.get(id);
  }
  has(id) {
    return this.contents.has(id);
  }
  reset() {
    this.contents.clear();
    this.invalidate();
  }
}
class AbilityRegistry extends CobbleRegistry {
  all() {
    return import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).abilities.all();
  }
  invalidate() {
    import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).abilities.allCache = null;
  }
  register(data, _id) {
    if (!data.num || data.num === 0)
      data.num = 10001 + this.contents.size;
    if (!data.name)
      data.name = _id;
    const ability = new import_dex_abilities.Ability(data);
    this.contents.set(ability.id, ability);
    return ability;
  }
}
class BagItemRegistry extends CobbleRegistry {
  invalidate() {
    return;
  }
  register(data, _id) {
    const script = data;
    this.contents.set(_id, script);
    return script;
  }
}
class HeldItemRegistry extends CobbleRegistry {
  all() {
    return import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).items.all();
  }
  invalidate() {
    import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).items.allCache = null;
  }
  register(data, _id) {
    if (!data.num || data.num === 0)
      data.num = 10001 + this.contents.size;
    if (!data.name)
      data.name = _id;
    const item = new import_dex_items.Item(data);
    this.contents.set(item.id, item);
    return item;
  }
}
class MoveRegistry extends CobbleRegistry {
  all() {
    return import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).moves.all();
  }
  invalidate() {
    import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).moves.allCache = null;
  }
  register(data, _id) {
    if (!data.num || data.num === 0)
      data.num = 10001 + this.contents.size;
    if (!data.name)
      data.name = _id;
    const move = new import_dex_moves.DataMove(data);
    this.contents.set(move.id, move);
    return move;
  }
}
class SpeciesRegistry extends CobbleRegistry {
  all() {
    return import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).species.all();
  }
  invalidate() {
    import_dex.Dex.mod(import_cobblemon.Cobblemon.modId).species.allCache = null;
  }
  register(data) {
    if (!data.num || data.num === 0)
      data.num = 10001 + this.contents.size;
    const species = new import_dex_species.Species(data);
    this.contents.set(species.id, species);
    return species;
  }
}
//# sourceMappingURL=registries.js.map
