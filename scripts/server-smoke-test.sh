#!/usr/bin/env bash
# Starts a dev server for one Minecraft version and loader with Hallowed loaded, forces a Hallowed Night and the
# Witching Hour, summons a spirit, a wisp and the Hollow Horseman with /hallowed, fires a spirit and a wisp spawn egg
# from dispensers (they must spawn their entity, not drop the egg) and stops after 30 seconds. Fails if the server
# crashes, a mixin fails, the mod's commands never run or a dispenser throws an egg out as an item.
#   usage: scripts/server-smoke-test.sh <minecraft version folder> <fabric|forge|neoforge>
# Written for 1.20.1 (pack_format and item NBT of that version); CI runs it for 1.20.1 Fabric and Forge.
set -u
VERSION="$1"; LOADER="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/$VERSION"
RUN="$LOADER/runs/server"
rm -rf "$RUN/world" "$RUN/crash-reports" "$RUN/logs"
PACK="$RUN/world/datapacks/hallowed_smoke"
mkdir -p "$PACK"
echo "eula=true" > "$RUN/eula.txt"
cat > "$RUN/server.properties" <<PROPS
online-mode=false
difficulty=normal
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-seed=20261123
initial-enabled-packs=vanilla,file/hallowed_smoke
PROPS
if [ "$LOADER" = forge ]; then
  # In the dev environment Forge's LAN advertiser loads a client-only class and can stop the server.
  mkdir -p "$RUN/defaultconfigs"
  printf '[server]\n\tadvertiseDedicatedServerToLan = false\n' > "$RUN/defaultconfigs/forge-server.toml"
fi
FORMAT="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["pack"]["pack_format"])' common/src/main/resources/pack.mcmeta)"
printf '{"pack":{"description":"Hallowed smoke test","pack_format":%s}}\n' "$FORMAT" > "$PACK/pack.mcmeta"

for dir in function functions; do
  mkdir -p "$PACK/data/hallowed_smoke/$dir" "$PACK/data/minecraft/tags/$dir"
  echo '{"values":["hallowed_smoke:start"]}' > "$PACK/data/minecraft/tags/$dir/load.json"
  printf 'forceload add -48 -48 48 48\nschedule function hallowed_smoke:run 100t\n' > "$PACK/data/hallowed_smoke/$dir/start.mcfunction"
  cat > "$PACK/data/hallowed_smoke/$dir/run.mcfunction" <<'MCF'
hallowed status
hallowed witching
hallowed night start
hallowed reload
execute positioned -32 0 -32 positioned over world_surface positioned ~ ~2 ~ run hallowed summon spirit
execute positioned -32 0 -24 positioned over world_surface positioned ~ ~2 ~ run hallowed summon wisp
execute positioned -24 0 -40 positioned over world_surface run hallowed summon horseman
execute positioned -40 0 -32 positioned over world_surface run summon hallowed:spirit ~ ~2 ~ {Tags:["hw_smoke"],PersistenceRequired:1b}
execute positioned -40 0 -24 positioned over world_surface run summon hallowed:will_o_wisp ~ ~2 ~ {Tags:["hw_smoke"]}
setblock 16 120 16 dispenser[facing=up]{Items:[{Slot:0b,id:"hallowed:spirit_spawn_egg",Count:1b}]}
setblock 32 120 16 dispenser[facing=up]{Items:[{Slot:0b,id:"hallowed:will_o_wisp_spawn_egg",Count:1b}]}
setblock 17 120 16 minecraft:redstone_block
setblock 33 120 16 minecraft:redstone_block
say HALLOWED_SMOKE_RAN
schedule function hallowed_smoke:check 40t
schedule function hallowed_smoke:finish 600t
MCF
  cat > "$PACK/data/hallowed_smoke/$dir/check.mcfunction" <<'MCF'
execute if entity @e[type=minecraft:item,nbt={Item:{id:"hallowed:spirit_spawn_egg"}}] run say HALLOWED_SMOKE_EGG_THROWN spirit
execute if entity @e[type=minecraft:item,nbt={Item:{id:"hallowed:will_o_wisp_spawn_egg"}}] run say HALLOWED_SMOKE_EGG_THROWN wisp
execute positioned 16 121 16 if entity @e[type=hallowed:spirit,tag=!hw_smoke,distance=..12] run say HALLOWED_SMOKE_DISPENSED spirit
execute positioned 32 121 16 if entity @e[type=hallowed:will_o_wisp,tag=!hw_smoke,distance=..12] run say HALLOWED_SMOKE_DISPENSED wisp
MCF
  printf 'kill @e[type=hallowed:hollow_horseman]\nkill @e[type=hallowed:spirit]\nkill @e[type=hallowed:will_o_wisp]\nsay HALLOWED_SMOKE_OK\nstop\n' \
    > "$PACK/data/hallowed_smoke/$dir/finish.mcfunction"
done

LOG="$ROOT/server-smoke-$VERSION-$LOADER.log"
./gradlew ":$LOADER:runServer" --stacktrace -Porg.gradle.java.installations.fromEnv=JAVA_HOME_17_X64,JAVA_HOME_21_X64 \
  < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 180); do
  sleep 5
  if grep -q "HALLOWED_SMOKE_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Mixin apply.*failed|InvalidInjectionException" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -E "HALLOWED_SMOKE|hallowed|Hallowed" "$LOG" | grep -v -E "Download|Task :" | head -60
for f in "$RUN"/crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by|[Mm]ixin.*(fail|error)" "$LOG" | head -60
echo "Result: $result"
if [ "$result" != ok ] || grep -q -E "This crash report has been saved|InvalidInjectionException" "$LOG"; then exit 1; fi
grep -q "HALLOWED_SMOKE_RAN" "$LOG" || { echo "The smoke function never ran"; exit 1; }
# A function with a command that doesn't parse (for example /hallowed or hallowed:spirit when the mod never loaded)
# fails to load, so the RAN marker also proves the mod's commands and entity types exist.
if grep -q "HALLOWED_SMOKE_EGG_THROWN" "$LOG"; then echo "A dispenser threw a Hallowed spawn egg out as an item"; exit 1; fi
for what in spirit wisp; do
  grep -q "HALLOWED_SMOKE_DISPENSED $what" "$LOG" || { echo "A dispenser did not spawn a $what from its spawn egg"; exit 1; }
done
echo "Hallowed loaded on $VERSION $LOADER: commands ran, entities spawned, dispensers hatched the spawn eggs"
