#!/usr/bin/env bash
# Prepares dev run directories of a workspace for QA: server.properties (RCON on 25575), eula, the QA player as op,
# and client options that skip first-run screens.
#   tools/qa/prepare_runs.sh <workspace> <loader> [options.txt to copy]
set -eu
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
SERVER="$ROOT/$1/$2/runs/server"
CLIENT="$ROOT/$1/$2/runs/client"
mkdir -p "$SERVER" "$CLIENT"
cat > "$SERVER/server.properties" <<'PROPS'
broadcast-rcon-to-ops=false
difficulty=normal
enable-rcon=true
enforce-whitelist=false
force-gamemode=false
function-permission-level=2
gamemode=survival
level-name=qa_world
level-seed=hallowed-qa
max-tick-time=60000
motd=Hallowed QA
online-mode=false
op-permission-level=4
rcon.password=qa
rcon.port=25575
simulation-distance=8
spawn-protection=0
view-distance=8
white-list=false
PROPS
echo "eula=true" > "$SERVER/eula.txt"
cat > "$SERVER/ops.json" <<'OPS'
[{"uuid": "PLACEHOLDER", "name": "Trickster", "level": 4, "bypassesPlayerLimit": false}]
OPS
# Offline-mode UUID of the QA player.
UUID=$(python3 -c "import hashlib,uuid; h=bytearray(hashlib.md5(b'OfflinePlayer:Trickster').digest()); h[6]=h[6]&0x0f|0x30; h[8]=h[8]&0x3f|0x80; print(uuid.UUID(bytes=bytes(h)))")
sed -i "s/PLACEHOLDER/$UUID/" "$SERVER/ops.json"
if [ -n "${3:-}" ] && [ -f "$3" ]; then cp "$3" "$CLIENT/options.txt"; fi
