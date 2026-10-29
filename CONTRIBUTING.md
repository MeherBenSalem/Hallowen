# Contributing

Thanks for helping improve Hallowed.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Prefer small, focused changes with a clear purpose.
3. Match the existing code style and MultiLoader layout: shared logic lives in `26.3/common`, and the
   `fabric` and `neoforge` modules only wire it into each loader.
4. Add or update tests when practical (`common` module JUnit, for logic that does not need a running game).
5. Open a pull request describing what changed and why.

## Development

- Java 25, Minecraft 26.3, Fabric and NeoForge.
- Build with `./gradlew build` from `26.3/`. Run the tests with `./gradlew :common:test`.
- Textures, models, blockstates, sounds, the music disc, language files, recipes, loot tables, tags and
  advancements are generated: edit `tools/assets/` and run `python3 tools/assets/build.py 26.3` instead of editing
  the files in `resources/`. Add `--no-sounds` to skip synthesising the audio.
- Check changes in game with the dev server and client: `tools/qa/prepare_runs.sh 26.3 <loader> tools/qa/options.txt`,
  then `tools/qa/devrun.sh start-server 26.3 <loader>` and `tools/qa/devrun.sh start-client 26.3 <loader>`.
  `/hallowed witching` and `/hallowed summon` get you to the interesting parts quickly.

## Licensing

By contributing, you agree that your contributions are licensed under the
Apache License, Version 2.0, unless you state otherwise in the pull request.
