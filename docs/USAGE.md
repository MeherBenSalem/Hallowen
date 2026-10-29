# Hallowed: usage guide

Everything Hallowed adds, how it works, and every recipe, config key and command. Numbers are the 1.0.0 defaults;
most can be changed in `config/hallowed/hallowed.json`.

## Hallowed Nights

A night belongs to the day it starts on and runs from dusk (time 12500) to dawn (23500). Whether a night is
Hallowed depends on `season.mode`:

| Mode | Hallowed Nights |
| --- | --- |
| `seasonal` (default) | Every night from `season.start` (10-15) to `season.end` (11-05), by the server's real date. Outside that window, a full moon is Hallowed with `offseason_full_moon_chance` (0.25). The window may wrap the new year (for example 12-20 to 01-10). |
| `always` | Every night. |
| `full_moons` | Only full moons, with `offseason_full_moon_chance`, whatever the date. |
| `manual` | Only nights started with `/hallowed night start`. |

The off-season roll is fixed per world and night, so every player and every restart agree on it. Hallowed Nights
happen in dimensions with a sky and a day cycle (the Overworld), not in the Nether or the End.

**On a Hallowed Night:**

- At dusk a banner reads **A HALLOWED NIGHT · Light your lanterns** (`night.announce`).
- The sky, fog and light turn violet, the moon grows into an orange **Harvest Moon**, stars brighten, embers and
  autumn leaves drift by, and owls, wind, distant chimes and whispers play. These ramp up through the evening and
  peak in the Witching Hour. Each can be switched off in `client.json`.
- **The Witching Hour** runs from midnight (18000) to 3 AM (21000). It opens with twelve tolls of a bell
  (`night.midnight_bell`) and a banner. Spirits come twice as often, a soft vignette closes in, and Cursed
  Pumpkins wake. Hearing the bell grants *The Witching Hour*.
- At dawn the night ends with a message, and spirits that came with it fade.
- Zombies, husks, drowned, skeletons, strays, bogged, zombie villagers and wither skeletons (the
  `hallowed:costume_wearers` entity tag) spawn in costume with `spawns.costumed_mob_chance` (0.35): a carved
  pumpkin, a jack o'lantern or a soul jack o'lantern with one of the new faces. They drop it 10% of the time.

## Trick-or-treating

Craft a **Treat Pail** and use it on a villager or wandering trader during a Hallowed Night. Each one answers the
door once per night.

- **Treat** with `trick_or_treat.treat_chance` (0.72), or `costume_treat_chance` (0.9) if you wear a costume. A
  treat is one or two random candies, one more in costume. Candy goes into the pail; what does not fit goes to your
  inventory.
- **Wandering traders** always treat, with two extra candies and a Soul Toffee.
- **Trick** otherwise, one of the enabled tricks (`trick_or_treat.tricks.*`):

| Trick | What happens |
| --- | --- |
| `bat_burst` | A cloud of smoke and five bats burst out of the door. They leave after ten seconds. |
| `pumpkin_head` | A carved pumpkin appears on your head (only if your head is bare), and falls off after twenty seconds. |
| `levitation` | You float up for two seconds and drift down slowly. |
| `spook` | A ghost flashes across the screen (turn the flash off with `jumpscares` in `client.json`) and you are Spooked for eight seconds. |
| `vanishing_act` | The villager vanishes in smoke and reappears a few blocks away. |
| `sticky_fingers` | The villager keeps one candy from your pail (only when it has some). |

A **costume** is anything in the `hallowed:costumes` item tag worn on your head: vanilla and Hallowed carved
pumpkins and jack o'lanterns, mob heads, the Witch Hat and the Horseman's Head.

Advancements: *Trick or Treat!*, *Tricked!*, *The Whole Neighbourhood* (ten treats), *Dressed for the Occasion*.

## Candy

| Candy | Food | Effect |
| --- | --- | --- |
| Candy Corn | 1 | 8 s of Sugar Rush |
| Caramel Apple | 6 | Regeneration I 5 s, Absorption I 30 s, 4 s of Sugar Rush |
| Ghost Gummy | 2 | Invisibility and Slow Falling, 20 s |
| Pumpkin Truffle | 4 | Removes Spooked and Sugar Crash; Resistance I and Fire Resistance, 20 s |
| Witch's Lollipop | 2 | A random hex. 75%: Speed II, Jump Boost II, Haste II, Night Vision, Regeneration, Luck, Water Breathing or Fire Resistance. 25%: a short Levitation, Nausea, Glowing or Slowness |
| Bat Wing Licorice | 2 | Night Vision 30 s; monsters within 24 blocks glow for 10 s |
| Eyeball Jawbreaker | 3 | Strength I 45 s (takes 3.2 s to eat) |
| Soul Toffee | 4 | Spirit Ward 2 min, Regeneration 3 s |

Candy can always be eaten, even when you are full.

- **Sugar Rush:** +15% movement speed, +10% attack speed and +15% mining speed. Each candy adds its seconds, up to
  `candy.sugar_rush_max_seconds` (60). Above half of that it becomes **Sugar Rush II**.
- **Sugar Crash** (`candy.sugar_crash`): when Sugar Rush II runs out, 12 s of −20% speed and −15% attack speed,
  and you get hungry. The Pumpkin Truffle cures it. First crash: *What Goes Up*.
- **Candy drops:** monsters killed by a player on a Hallowed Night drop a candy with `candy.drop_chance` (0.2), or
  `costumed_drop_chance` (1.0) if they were in costume.
- **Spirit Ward:** spirits do not target you.
- **Spooked:** −10% movement speed, a heartbeat and a little camera shake. From spirits, the spook trick and the
  Horseman.

The `hallowed:candy` item tag lists all eight. Eating your first candy grants *Hallowed*; eating all eight,
*Sweet Tooth*.

## Carved faces and the lantern ward

- Use **shears** on any carved pumpkin (vanilla or Hallowed) to carve the next of eight faces: Classic, Grin,
  Fangs, Cat, Scream, Wink, Skull, Bat. Sneak to carve the previous one. The Classic face is the vanilla block.
- Use a **torch** on a carved face to light it (a jack o'lantern), or a **soul torch** for a soul jack o'lantern.
  The torch is used up. Shears re-carve lit and soul faces too, and they stay lit.
- Carved and lit faces can be worn on the head. Breaking one keeps its face.
- **Lantern ward:** spirits never spawn within `spawns.lantern_ward_radius` (12) blocks of a lit jack o'lantern
  (vanilla or Hallowed), or `soul_lantern_ward_radius` (20) of a soul jack o'lantern. A spirit that comes within 8
  blocks of one turns and flees.

Carving a new face grants *Carpe Pumpkin*.

## Spirits

- Spawn on Hallowed Nights near each player (not in spectator, not on Peaceful), 18 to 34 blocks away under the
  open sky, every `spawns.spirits.interval_seconds` (20). Up to half of `max_per_player` (4) at a time within 48
  blocks, the full number during the Witching Hour, when they also come twice as often.
- 16 health, 3 attack damage. They fly through blocks, circle, then swoop. A hit leaves you Spooked for six seconds.
- They do not target players with Spirit Ward.
- Drop 0 to 2 Ectoplasm (more with Looting). The first one you lay to rest grants *Ectoplasmic*.
- Spirits that came with the night fade at dawn. Spirits from spawn eggs or commands, or with a name, stay.

## Will-o'-the-wisps and Hallowed Caches

- Every `spawns.wisps.interval_seconds` (120) a wisp appears near each player with `chance` (0.65), if there is no
  other wisp within 80 blocks. With `cursed_chance` (0.15) it is cursed (violet).
- Come within 14 blocks and it sets off, leading you up to 60 blocks over open ground. It moves faster while you
  keep up, and waits if you fall more than 15 blocks behind. Left alone for a minute, it fades.
- Where it stops it sinks into the ground and leaves a **Hallowed Cache**. Open it (use or break it) to burst it:
  3 to 5 candies and one of Ectoplasm, pumpkin seeds, a gold nugget, an emerald, a Witch Hat, a Treat Pail or a
  diamond. Opening one grants *Follow the Light*.
- A cursed wisp's cache is guarded by three spirits, and adds 2 to 3 more candies and one of Soul Toffee,
  Ectoplasm, a diamond, the music disc or a Cursed Pumpkin.
- Hit a wisp and it pops.

## The Hollow Horseman

**Summoning.** Place a **Cursed Pumpkin** in a dimension with a sky and a day cycle. During the Witching Hour of a
Hallowed Night it wakes (with `horseman.summon_needs_witching_hour` off: any night), pulses for a few seconds, and
the Horseman rises beside it. It will not wake on Peaceful, or while a Horseman is within 160 blocks. Use the pumpkin
to read what it is waiting for. Crafting one grants *Something Wicked*.

He has a red boss bar, and everyone within 96 blocks sees his banner.

| Stat | Default |
| --- | --- |
| Health | `horseman.health` 320 |
| Attack damage | `horseman.damage` 11 |
| Armor | `horseman.armor` 8, toughness 4 |
| Knockback resistance | full while mounted, 70% on foot |

**Phase one, mounted.** He circles you on his nightmare steed.

- **Scythe sweep** at close range: hits everything in front of him, and sometimes leaves you Spooked.
- **Flaming jack o'lanterns**, lobbed from range, burst for area damage (radius 3.2). They set fires only with
  `horseman.sets_fires`, and never inside spawn protection.
- **Charge:** he rears up for a second, then charges in a straight line. If he runs into a wall he is **stunned**
  for 2.5 seconds and takes 35% more damage.

**Phase two, below half health.** His steed collapses (he cannot be hurt while it falls) and he fights on foot,
slower but relentless.

- He **calls two or three spirits** to his side (up to four nearby). They fade after 45 seconds.
- He **throws his own head**: a burning projectile that homes in on you, bursts, and returns to him. While he is
  headless he takes 50% more damage.

He and his spirits never hurt each other, and his attacks spare other monsters. If he gets stuck far from his
target, he teleports closer. At dawn he rides away (`horseman.rides_away_at_dawn`), if he has seen the night.

**Drops:** the Reaper Scythe or the Horseman's Head, 4 to 8 Ectoplasm, the music disc (35%), a Cursed Pumpkin
(15%), and a burst of 22 to 29 candies. Defeating him grants *Headless No More*.

## Spoils

- **Reaper Scythe** (Netherite tier, 9 attack damage, 1.2 speed). **Use** it to Reap: a sweep that hits everything
  within 3.6 blocks for 75% damage, and harvests every fully grown crop in a 7×3×7 area, replanting it. 2.5 s
  cooldown; costs durability. Killing a monster with it heals you 1.5 hearts. Reaping nine crops in one swing grants
  *Grim Harvest*.
- **Horseman's Head:** wear it for Night Vision and Spirit Ward. It burns.
- **Witch Hat:** a costume.
- **Music Disc, Witching Hour** (NightBeam): a 2:09 waltz. Comparator output 13.
- **Ectoplasm:** a crafting material.

## Recipes

| Result | Recipe |
| --- | --- |
| Treat Pail | String in the top corners, Pumpkins in the middle corners, a Carved Pumpkin bottom centre |
| Witch Hat | Leather top centre, Black Wool middle centre and bottom corners, a Gold Nugget bottom centre |
| Soul Jack o'Lantern | A Carved Pumpkin over a Soul Torch |
| Cursed Pumpkin | A Soul Jack o'Lantern surrounded by 6 Ectoplasm and 2 Bones (bones top and bottom centre) |
| 4 Candy Corn | Sugar, Wheat, Orange Dye (shapeless) |
| Caramel Apple | Sugar over an Apple over a Stick |
| 3 Ghost Gummies | Ectoplasm, Sugar, Slimeball (shapeless) |
| 2 Pumpkin Truffles | Pumpkin Pie, Cocoa Beans, Sugar (shapeless) |
| 2 Eyeball Jawbreakers | Spider Eye, 2 Sugar (shapeless) |
| 2 Soul Toffee | Ectoplasm, Sugar, Honey Bottle, Soul Sand (shapeless) |

The Witch's Lollipop and Bat Wing Licorice come only from treats, drops and caches.

## Commands

All under `/hallowed`. Everything except the status needs operator permission (level 2).

| Command | Effect |
| --- | --- |
| `/hallowed` or `/hallowed status` | Whether tonight is Hallowed, the current phase and time, and the season mode |
| `/hallowed night start` | Makes tonight (or the coming night) a Hallowed Night |
| `/hallowed night stop` | Makes tonight an ordinary night |
| `/hallowed witching` | Makes tonight Hallowed and skips to just before midnight |
| `/hallowed summon horseman` | Raises the Horseman at your position |
| `/hallowed summon spirit` | Summons a spirit |
| `/hallowed summon wisp [cursed]` | Summons a wisp, or a cursed one |
| `/hallowed reload` | Reloads `hallowed.json` |

Forced and cancelled nights are saved in `hallowed.json` inside the world folder.

## Configuration

Both files are created on first launch with comments. Missing keys are added, your values are kept, and unknown
keys are left alone. A file that cannot be read is left untouched and the defaults are used.

### `config/hallowed/hallowed.json` (server)

| Key | Default | Meaning |
| --- | --- | --- |
| `season.mode` | `seasonal` | `seasonal`, `always`, `full_moons` or `manual` (see above) |
| `season.start`, `season.end` | `10-15`, `11-05` | The season, as month-day |
| `season.offseason_full_moon_chance` | 0.25 | Chance a full moon outside the season is Hallowed |
| `night.announce` | true | The dusk and midnight banners |
| `night.midnight_bell` | true | Twelve tolls at midnight |
| `spawns.costumed_mob_chance` | 0.35 | Costume chance for costume wearers spawning on a Hallowed Night |
| `spawns.spirits.enabled` | true | Spirit spawning |
| `spawns.spirits.max_per_player` | 4 | Spirits per player in the Witching Hour (half before it) |
| `spawns.spirits.interval_seconds` | 20 | Seconds between spawn attempts (halved in the Witching Hour) |
| `spawns.wisps.enabled` | true | Wisp spawning |
| `spawns.wisps.interval_seconds` | 120 | Seconds between wisp attempts |
| `spawns.wisps.chance` | 0.65 | Chance per attempt |
| `spawns.wisps.cursed_chance` | 0.15 | Chance a wisp is cursed |
| `spawns.lantern_ward_radius` | 12 | Spirit-free radius around a lit jack o'lantern |
| `spawns.soul_lantern_ward_radius` | 20 | The same for soul jack o'lanterns |
| `candy.drop_chance` | 0.2 | Candy drop chance for monsters on a Hallowed Night |
| `candy.costumed_drop_chance` | 1.0 | The same for costumed monsters |
| `candy.sugar_rush_max_seconds` | 60 | Sugar Rush cap; above half it becomes Sugar Rush II |
| `candy.sugar_crash` | true | The crash after Sugar Rush II |
| `trick_or_treat.enabled` | true | Trick-or-treating |
| `trick_or_treat.treat_chance` | 0.72 | Treat chance |
| `trick_or_treat.costume_treat_chance` | 0.9 | Treat chance in costume |
| `trick_or_treat.tricks.*` | true | `bat_burst`, `pumpkin_head`, `levitation`, `spook`, `vanishing_act`, `sticky_fingers` |
| `horseman.health` | 320 | Maximum health |
| `horseman.damage` | 11 | Attack damage (his bombs and head scale from it) |
| `horseman.armor` | 8 | Armor |
| `horseman.sets_fires` | false | Bombs leave fire (never in spawn protection) |
| `horseman.rides_away_at_dawn` | true | He leaves at dawn |
| `horseman.summon_needs_witching_hour` | true | Cursed Pumpkins wake only in the Witching Hour |

### `config/hallowed/client.json` (each player)

| Key | Default | Meaning |
| --- | --- | --- |
| `atmosphere.sky_tint` | true | The violet sky and light |
| `atmosphere.fog` | true | The violet fog |
| `atmosphere.harvest_moon` | true | The swollen orange moon |
| `atmosphere.ambient_particles` | true | Embers, leaves and soul motes |
| `atmosphere.ambient_sounds` | true | Owls, wind, chimes and whispers |
| `atmosphere.vignette` | true | The Witching Hour vignette |
| `atmosphere.intensity` | 1.0 | Scales all of the above (0 off, 1.5 stronger) |
| `effects.banners` | true | The title banners |
| `effects.jumpscares` | true | The spook trick's flash (the sound plays either way) |
| `effects.camera_shake` | true | Shake from charges and Spooked |

## Datapacks

- Item tags: `hallowed:candy`, `hallowed:costumes`. Entity tags: `hallowed:costume_wearers`, `hallowed:no_candy`
  (monsters that never drop candy).
- Loot tables: `hallowed:entities/hollow_horseman`, `hallowed:entities/spirit`, `hallowed:blocks/hallowed_cache`
  (the cursed pools check the block's `cursed` state).
- The Hallowed carved pumpkin is in `minecraft:gaze_disguise_equipment`, so endermen ignore you while you wear one,
  like the vanilla carved pumpkin.
