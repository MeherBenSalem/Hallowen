# Hallowed

![Hallowed](marketing/images/banner.png)

Halloween, done properly. From October 15 to November 5 every night is a **Hallowed Night**: a violet sky and a
swollen Harvest Moon, villagers who answer the door with candy or a trick, spirits that drift out of the dark and
will-o'-the-wisps that lead to buried treasure. At midnight the bell strikes twelve, and if a Cursed Pumpkin has
been left out, the **Hollow Horseman** rides. Mod id: `hallowed`.

## Features

- **Hallowed Nights** in season, and on one full moon in four the rest of the year. A dusk banner, a violet sky and
  fog, a Harvest Moon, drifting embers and leaves, and ambient owls, wind, chimes and whispers.
- **The Witching Hour**: twelve tolls of a bell at midnight, a banner, and until 3 AM twice the spirits.
- **Trick-or-treating**: hold a Treat Pail out to a villager. Treats go into the pail, costumes earn more, and six
  harmless tricks keep you guessing. Costumed zombies and skeletons roam the night.
- **Eight candies** with real effects, from stacking **Sugar Rush** (and the Sugar Crash after it) to Ghost Gummies,
  Witch's Lollipops and Soul Toffee.
- **Carved faces**: shears carve eight faces into any pumpkin, a torch lights it, a soul torch gives it a blue flame.
  Every face can be worn.
- **Lantern ward**: lit jack o'lanterns (vanilla ones too) keep spirits away within 12 blocks, soul ones 20.
- **Spirits** that swoop through walls and leave you Spooked, and **will-o'-the-wisps** that lead you to a
  **Hallowed Cache**, or into a cursed ambush with a richer one.
- **The Hollow Horseman**, a two-phase boss on a burning nightmare steed: scythe sweeps, flaming jack o'lanterns,
  charges you can stun against a wall, then on foot with his spirits and his own thrown head. He drops the
  **Reaper Scythe** (Reap harvests and replants crops around you) or the **Horseman's Head**, a music disc and more.
- An original waltz on a music disc, 14 advancements, `/hallowed` commands and two commented JSON configs with
  `/hallowed reload`.

## Requirements

These are the only supported targets (the NightBeam convention): 8 jars per release, all with the same version.

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

Fabric needs Fabric API. Install the mod on both the server and the client: the server runs the nights, spawns and
the boss, and the client draws the sky, banners and effects.

## Documentation

- [docs/USAGE.md](docs/USAGE.md): how every system works, every recipe, config key and command
- [PATCH_NOTES.md](PATCH_NOTES.md) and [patchnotes/](patchnotes/): what changed in each version
- [marketing/](marketing/): CurseForge page, summary, artwork and screenshots

## Building

Each Minecraft version is a separate MultiLoader Gradle workspace with its own wrapper; the root build drives all four.

```bash
./gradlew buildAll                    # every workspace, then the 8 loader jars into releases/
cd 1.20.1 && ./gradlew build          # Gradle on Java 21 (Java 17 toolchain)
cd 1.21.1 && ./gradlew build          # Java 21
cd 26.3 && ./gradlew build            # Java 25 (the same for 26.2)
cd 26.3 && ./gradlew :common:test     # config and night-clock unit tests
```

The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and the others
`JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`.
Jars land in `<workspace>/<loader>/build/libs/hallowed-<loader>-<minecraft>-<version>.jar`. Every workspace's
`gradle.properties` carries the same `version=` (`./gradlew checkVersions`; set it with
`./gradlew syncVersion -PmodVersion=x.y.z`). `python3 scripts/check_targets.py workspaces` checks the targets, the
shared version and the metadata ranges. Release notes for a version live in `patchnotes/<version>.md`.
`releases/` keeps the jars of published versions (1.0.0) as history; newer versions are built by CI and Publish.

## Releasing

CI (`.github/workflows/ci.yml`) builds all 8 jars on every push to `main` that touches the build, runs the unit tests
and the publisher script tests, and boots a 1.20.1 dev server with the mod loaded
(`scripts/server-smoke-test.sh 1.20.1 fabric`). Publishing to CurseForge is
`.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag; it never runs on a plain
push. It refuses a second release of the mod on the same day (Africa/Tunis), takes the jars from `releases/`, the
GitHub Release or a build from source, and fails unless exactly the 8 jars are found. It needs the secrets
`CURSEFORGE_TOKEN` (upload token) and `CURSEFORGE_API_KEY` (Core API key) and the variable `CURSEFORGE_ID` (it falls
back to the Hallowed project, `1773771`). NightBeam publishes to CurseForge only; the Modrinth page stays up but gets
no new versions. Prove a change with a dry run first:

```bash
gh workflow run publish.yml -f version=1.0.1 -f dry_run=true
```

## Tools

- `python3 tools/assets/build.py 26.3` regenerates every texture, model, blockstate, sound, the music disc,
  language file, recipe, loot table, tag and advancement from code. Add `--no-sounds` to keep the audio as it is
  (synthesising the music takes a while). It writes the 26.x formats: the 1.21.1 and 1.20.1 workspaces keep their
  own converted data and assets, so do not point it at them.
- `tools/assets/preview.py` has the contact-sheet helper used to review generated art at a glance.
- `tools/qa/prepare_runs.sh 26.3 <loader> tools/qa/options.txt`, then `tools/qa/devrun.sh start-server 26.3 <loader>` and
  `tools/qa/devrun.sh start-client 26.3 <loader>` run a dev server and client (RCON on 25575) under Xvfb for in-game
  checks; `tools/qa/client.py` drives them.
- `python3 marketing/source/capture.py` and `node marketing/source/render.cjs` retake the screenshots and rebuild
  the listing artwork. See [marketing/README.md](marketing/README.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

## License

Licensed under the [Apache License 2.0](LICENSE). See [NOTICE](NOTICE).
