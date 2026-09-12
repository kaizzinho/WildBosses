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
var cobbled_debug_server_exports = {};
__export(cobbled_debug_server_exports, {
  startServer: () => startServer
});
module.exports = __toCommonJS(cobbled_debug_server_exports);
var import_battle_stream = require("../battle-stream");
var import_dex = require("../dex");
var Net = __toESM(require("net"));
var import_cobblemon = require("./cobblemon");
function startServer(port) {
  const server = Net.createServer();
  const battleMap2 = /* @__PURE__ */ new Map();
  server.listen(port, () => {
    console.log("Server listening for connection requests on socket localhost: " + port);
  });
  server.on("connection", (socket2) => onConnection(socket2, battleMap2));
}
function onData(socket, chunk, battleMap) {
  const data = chunk.toString();
  const lines = data.split("\n");
  lines.forEach((line) => {
    console.log("Data received from client: " + line);
    const parts = line.split(" ");
    const command = parts[0];
    switch (command) {
      case ">startbattle": {
        const battleId = parts[1];
        if (battleId) {
          battleMap.set(battleId, new import_battle_stream.BattleStream());
          socket.write("ACK");
        } else {
          console.error("Command '>startbattle' requires a battleId.");
          socket.write("ERR");
        }
        break;
      }
      case ">receiveData": {
        const type = parts[1];
        const registry = import_cobblemon.Cobblemon.getRegistry(type);
        try {
          if (!registry)
            throw new Error();
          const data = line.substring(command.length + type.length + 2);
          const obj = () => {
            try {
              return JSON.parse(data);
            } catch {
              return eval(`(${data})`);
            }
          };
          for (const [key, value] of Object.entries(obj())) {
            registry.register(value, (0, import_dex.toID)(key));
          }
          ;
          registry.invalidate();
          socket.write("ACK");
        } catch (e) {
          console.error(`Error processing >receiveData for type ${type}:`, e);
          socket.write("ERR");
        }
        break;
      }
      case ">receiveEntry": {
        const type = parts[1];
        const registry = import_cobblemon.Cobblemon.getRegistry(type);
        try {
          if (!registry)
            throw new Error();
          const id = parts[2];
          const data = line.substring(command.length + type.length + id.length + 2);
          const obj = () => {
            try {
              return JSON.parse(data);
            } catch {
              return eval(`(${data})`);
            }
          };
          registry.register(obj(), (0, import_dex.toID)(id));
          registry.invalidate();
          socket.write("ACK");
        } catch (e) {
          console.error(`Error processing >receiveData for type ${type}:`, e);
          socket.write("ERR");
        }
        break;
      }
      case ">getData": {
        const type2 = parts[1];
        var registry = import_cobblemon.Cobblemon.getRegistry(type2);
        try {
          if (!registry)
            throw new Error();
          const payload = JSON.stringify(registry.all());
          socket.write(padNumber(payload.length, 8) + payload);
        } catch (e) {
          console.error(`Error processing >receiveData for type ${type2}:`, e);
          socket.write("ERR");
        }
        break;
      }
      case ">resetAll": {
        for (const key of import_cobblemon.Cobblemon.registryKeys) {
          import_cobblemon.Cobblemon.registries[key].reset();
        }
      }
      case ">resetData": {
        const type2 = parts[1];
        var registry = import_cobblemon.Cobblemon.getRegistry(type2);
        try {
          if (!registry)
            throw new Error();
          registry.reset();
          socket.write("ACK");
        } catch (e) {
          console.error(`Invalid registry type for >getData: ${type2}`);
          socket.write("ERR");
        }
        break;
      }
      case ">getTypeChart": {
        const payload = JSON.stringify(import_dex.Dex.data.TypeChart);
        socket.write(padNumber(payload.length, 8) + payload);
        break;
      }
      case ">afterSpeciesInit": {
        import_dex.Dex.modsLoaded = false;
        import_dex.Dex.includeMods();
        socket.write("ACK");
        break;
      }
      default: {
        const [battleId, showdownMsg] = line.split("~");
        const battleStream = battleMap.get(battleId);
        if (battleStream) {
          try {
            void battleStream.write(showdownMsg);
          } catch (err) {
            console.error(err.stack);
          }
          writeBattleOutput(socket, battleStream);
        }
        break;
      }
    }
  });
}
function writeBattleOutput(socket2, battleStream) {
  const messages = battleStream.buf;
  if (messages.length !== 0) {
    socket2.write(padNumber(messages.length, 8));
    for (const message of messages) {
      socket2.write(padNumber(message.length, 8) + message);
    }
  } else {
    writeVoid(socket2);
  }
  battleStream.buf = [];
}
function writeVoid(socket2) {
  socket2.write("00000000");
}
function onConnection(socket2, battleMap2) {
  socket2.on("data", (chunk2) => {
    try {
      onData(socket2, chunk2, battleMap2);
    } catch (error) {
      console.error(error);
    }
  });
  socket2.on("end", () => console.log("Closing connection with the client"));
  socket2.on("error", (err) => console.error(err.stack));
}
function padNumber(num, size) {
  let numStr = num.toString();
  while (numStr.length < size)
    numStr = "0" + numStr;
  return numStr;
}
//# sourceMappingURL=cobbled-debug-server.js.map
