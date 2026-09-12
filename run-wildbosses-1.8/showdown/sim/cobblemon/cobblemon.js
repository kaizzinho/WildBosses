"use strict";
var __create = Object.create;
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __getProtoOf = Object.getPrototypeOf;
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
var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
  // If the importer is in node compatibility mode or this is not an ESM
  // file that has been converted to a CommonJS file using a Babel-
  // compatible transform (i.e. "__esModule" has not been set), then set
  // "default" to the CommonJS "module.exports" for node compatibility.
  isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
  mod
));
var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);
var cobblemon_exports = {};
__export(cobblemon_exports, {
  Cobblemon: () => Cobblemon
});
module.exports = __toCommonJS(cobblemon_exports);
var Reg = __toESM(require("./registries"));
var Cobblemon;
((Cobblemon2) => {
  Cobblemon2.modId = "cobblemon";
  Cobblemon2.registryKeys = ["ability", "bagItem", "heldItem", "move", "species"];
  Cobblemon2.registries = {
    ability: new Reg.AbilityRegistry(),
    bagItem: new Reg.BagItemRegistry(),
    heldItem: new Reg.HeldItemRegistry(),
    move: new Reg.MoveRegistry(),
    species: new Reg.SpeciesRegistry()
  };
  function getRegistry(key) {
    if (Cobblemon2.registryKeys.includes(key)) {
      return Cobblemon2.registries[key];
    }
    return void 0;
  }
  Cobblemon2.getRegistry = getRegistry;
})(Cobblemon || (Cobblemon = {}));
//# sourceMappingURL=cobblemon.js.map
