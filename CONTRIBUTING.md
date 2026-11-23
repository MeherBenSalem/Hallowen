# Contributing

Thanks for helping improve Hallowed.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Prefer small, focused changes with a clear purpose.
3. Make the change in every Minecraft version workspace it applies to (`1.20.1/`, `1.21.1/`, `26.2/` and `26.3/`).
   Each is a MultiLoader build: shared logic lives in `common/`, and the loader modules (`fabric/` and `neoforge/`,
   or `fabric/` and `forge/` on 1.20.1) only wire it into each loader. The `common/` code is nearly the same in all
   four; only the version-specific APIs differ.
4. Build the workspaces you changed (`./gradlew build` from that folder: Gradle on Java 21 for 1.20.1 and 1.21.1,
   Java 25 for 26.x), or all four with `./gradlew buildAll` from the root (the 8 loader jars land in `releases/`).
   This also runs the unit tests. Add or update tests when practical (`common` module JUnit, for logic that does
   not need a running game).
5. Open a pull request describing what changed, why, and which versions and loaders you checked.

## Development

- Every workspace's `gradle.properties` has the same `version=`; change it with
  `./gradlew syncVersion -PmodVersion=x.y.z`. `python3 scripts/check_targets.py workspaces` checks the 8 targets, the
  shared version and the Minecraft ranges, as CI does.
- Textures, models, blockstates, sounds, the music disc, language files, recipes, loot tables, tags and
  advancements are generated: edit `tools/assets/` and run `python3 tools/assets/build.py 26.3` instead of editing
  the files in `resources/`. Add `--no-sounds` to skip synthesising the audio. The 1.21.1 and 1.20.1 workspaces keep
  their own converted data and assets.
- Check changes in game with the dev server and client: `tools/qa/prepare_runs.sh 26.3 <loader> tools/qa/options.txt`,
  then `tools/qa/devrun.sh start-server 26.3 <loader>` and `tools/qa/devrun.sh start-client 26.3 <loader>`.
  `/hallowed witching` and `/hallowed summon` get you to the interesting parts quickly.
- `scripts/server-smoke-test.sh 1.20.1 fabric` boots a 1.20.1 dev server with the mod, runs its commands and fires
  its spawn eggs from dispensers; CI runs it for 1.20.1 Fabric (and Forge, report only).

## Licensing

By contributing, you agree that your contributions are licensed under the
Apache License, Version 2.0, unless you state otherwise in the pull request.
