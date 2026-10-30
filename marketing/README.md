# Marketing assets: 1.0 "The First Hallowed Night"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod JARs.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (223 characters) |
| [`../patchnotes/1.0.0.md`](../patchnotes/1.0.0.md) | File changelog for 1.0.0 |

Image and doc links in the description point at the `claude/zealous-brown-t1w0pv` branch (`raw.githubusercontent.com/MeherBenSalem/Hallowen/claude/zealous-brown-t1w0pv/marketing/images/...`), where the files are today. After merging to `main`, replace `claude/zealous-brown-t1w0pv` with `main` in `curseforge/description.md` and paste it again, before deleting the branch. To host on CurseForge instead, upload the images to the project gallery and replace each URL.

The description leads with the pitch, then Hallowed Nights, trick-or-treating, the candy, carved faces and the lantern ward, spirits and wisps, the Hollow Horseman, his spoils, configuration, everything else, downloads and compatibility, the FAQ and a gallery.

## Adding a Minecraft version

The supported versions are listed in exactly two places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['Fabric', 'NeoForge'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row.

No other copy or artwork names a Minecraft version. A new release name or number goes in `RELEASE` in `render.cjs` and in the first lines of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 963 KB | Description header: the Hollow Horseman in a village on a Hallowed Night, the mod icon, "1.0 · The First Hallowed Night", loader pills and version |
| `images/icon.png` | 512×512, 9 KB | Project avatar: the mod icon (the Horseman's Head before a Harvest Moon), pixel-exact |
| `images/card-nights.png` | 1600×900 | The Witching Hour banner and the Harvest Moon; season dates, Witching Hour, off-season chance |
| `images/card-trick.png` | 1600×900 | A treat and the spook trick at the same door |
| `images/card-candy.png` | 1600×900 | All eight candies with their in-game textures and effects |
| `images/card-carving.png` | 1600×900 | All 24 face blocks: eight faces carved, lit and soul |
| `images/card-ward.png` | 1600×900 | A line of lit jack o'lanterns holding spirits back; ward radii |
| `images/card-spirits.png` | 1600×900 | Three spirits close up; health, per-player cap |
| `images/card-wisps.png` | 1600×900 | A wisp and a cursed wisp leading to a cache, and a cache bursting open |
| `images/card-horseman.png` | 1600×900 | The Horseman on his nightmare steed; health, armor, phase two |
| `images/card-summon.png` | 1600×900 | The awakened Cursed Pumpkin and the Horseman rising with his banner and boss bar |
| `images/card-fight.png` | 1600×900 | The Horseman on foot with his spirits; his drops |
| `images/card-spoils.png` | 1600×900 | The Reaper Scythe reaping a wheat field, and the inventory with the scythe tooltip |
| `images/card-config.png` | 1600×900 | Excerpts of `hallowed.json` and `client.json` |
| `images/header-*.png` | 1400×120, transparent, 80–106 KB | 12 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/gifs/carving.gif` | 560×420, 2.3 MB | Shears cycling a pumpkin through its faces, then a torch lighting it |
| `images/gifs/horseman-rises.gif` | 720×405, 1.9 MB | A Cursed Pumpkin wakes and the Horseman rises |
| `images/screenshots/*.jpg` | 1920×1080, 167–428 KB | 18 captures: gallery uploads and card art |

### How the screenshots were captured

Every screenshot is an unedited capture of the **26.3 NeoForge development client** (`tools/qa/devrun.sh start-client 26.3 neoforge`, Hallowed 1.0.0, default configuration) with software rendering at 1920×1080 and GUI scale 3. [`source/capture.py`](source/capture.py) takes them all; run it with the dev server and client up to take them again.

- **World:** the QA world (seed `hallowed-qa`), in the plains village around its bell at -729 72 -995, the field east of it and a farm south-east. The script dresses the bell yard with jack o'lanterns and pumpkins first.
- **Time:** frozen (`advance_time` off) on a Hallowed Night started with `/hallowed night start`, mostly at 18600, inside the Witching Hour.
- **Actors** are summoned with NoAI so they hold still, and natural spawning is off while shooting. Banners, boss bars, the jumpscare, the cache burst and the Reap all come from the mod as it runs.
- **Doorstep shots:** the treat and the trick are forced by setting `treat_chance` to 1 (or 0, with only the spook trick enabled) in `hallowed.json`; the script pauses for you to do that.
- **HUD:** hidden (F1) except where it is part of the shot: the Witching Hour banner, the Horseman's arrival, trick-or-treating, the Reap, the inventory and the advancements.
- **GIFs** are recorded from the X display with `ffmpeg -f x11grab` and encoded with a generated palette.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the mod's own textures and the bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- `source/backdrop.jpg` is the blurred background scene, a 1280×720 copy of `screenshots/village-witching-hour.jpg`.
- `source/logo.png` is the mod icon (`common/src/main/resources/logo.png`, drawn by `tools/assets/misc.py`).
- The candy sprites and the pumpkin ornament are read straight from the generated textures in `26.3/common/src/main/resources/assets/hallowed/textures/`, so run `python3 tools/assets/build.py 26.3` first.
- `source/fonts/` holds Grenze Gotisch (titles) and Spectral (text) with their SIL Open Font License 1.1 texts. The script uses only these files.

Numbers on the cards (ward radii, health, drop counts, season dates) are written in `render.cjs`, copied from the default config and the code. Update them when those change. Card crops are pixel regions of the 1920×1080 captures (`region()` calls), so new captures may need their crops adjusted.

## Wording

Every number in the copy and on the cards is a 1.0.0 default from the generated config (`config/hallowed/`), a recipe, a loot table or the code. Update the copy when those defaults change. The boss is **the Hollow Horseman**, the event is a **Hallowed Night**, and its midnight stretch is **the Witching Hour**.
