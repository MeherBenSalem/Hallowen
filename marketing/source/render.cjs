// Renders the listing artwork in marketing/images/ from real in-game screenshots, the mod's own textures and the
// bundled OFL fonts.
// Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility table of curseforge/description.md).
const RELEASE = { version: '1.0', name: 'The First Hallowed Night' };
const VERSIONS = [
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
// The mod's textures, straight from the generated resources of the reference workspace.
const textures = path.join(root, '26.3', 'common', 'src', 'main', 'resources', 'assets', 'hallowed', 'textures');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));
const tex = (name) => url(path.join(textures, name + '.png'));

const css = `
@font-face { font-family: HwSerif; src: url('${url(path.join(fonts, 'spectral.ttf'))}'); }
@font-face { font-family: HwSerif; font-style: italic; src: url('${url(path.join(fonts, 'spectral-italic.ttf'))}'); }
@font-face { font-family: HwSerif; font-weight: 600; src: url('${url(path.join(fonts, 'spectral-semibold.ttf'))}'); }
@font-face { font-family: HwDisplay; font-weight: 100 900; src: url('${url(path.join(fonts, 'grenze-gotisch.ttf'))}'); }
:root { --text:#e8dccb; --dim:#a8979f; --faint:#6f5f6c; --header:#f6e7cf; --orange:#ff8a2c; --amber:#ffb84d;
        --ember:#ff6a1a; --violet:#a483e6; --soul:#7fe3e0; --bg:#0b0710; --line:rgba(255,160,80,.35); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: HwSerif, serif; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 30% 40%, #22122c 0%, #0b0710 72%); }
.display { font-family: HwDisplay, serif; font-weight: 700; letter-spacing: .01em; }
.flame { background: linear-gradient(180deg, #fff1c4 0%, #ffc760 32%, #ff8a2c 64%, #c2410e 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.glow { filter: drop-shadow(0 0 20px rgba(255,110,30,.42)); }
.kicker { font-family: HwSerif, serif; font-weight: 600; color: var(--amber); letter-spacing: .24em; font-size: 21px; text-transform: uppercase;
          display: flex; align-items: center; gap: 14px; }
.orn { width: 28px; height: 28px; image-rendering: pixelated; flex: none; }
.rule { height: 1px; background: linear-gradient(90deg, rgba(255,140,50,0), rgba(255,140,50,.9) 20%, rgba(255,140,50,.9) 70%, rgba(255,140,50,0)); }
.frame { position: relative; border: 1px solid rgba(255,170,100,.45); box-shadow: 0 0 0 1px rgba(0,0,0,.6), 0 30px 80px rgba(0,0,0,.7), 0 0 70px rgba(255,110,30,.16); background: #000; }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 45%, rgba(0,0,0,.78) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(16px) brightness(.36) saturate(1.15); }
.pill { display: inline-block; border: 1px solid rgba(255,190,120,.55); padding: 6px 15px 5px; margin-right: 10px;
        font-family: HwSerif, serif; font-weight: 600; font-size: 19px; letter-spacing: .14em; text-transform: uppercase; color: var(--header); background: rgba(0,0,0,.35); }
.label { position: absolute; font-family: HwSerif, serif; font-weight: 600; font-size: 19px; letter-spacing: .14em; text-transform: uppercase; color: var(--header);
         background: rgba(12,7,14,.88); border: 1px solid rgba(255,140,50,.8); padding: 7px 14px 6px; white-space: nowrap;
         box-shadow: 0 8px 26px rgba(0,0,0,.6); z-index: 2; }
.label small { display: block; font-weight: 400; letter-spacing: 0; text-transform: none; font-size: 18px; color: var(--dim); margin-top: 1px; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 34px; margin: 0 0 14px; font-size: 26px; line-height: 1.33; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 7px; width: 20px; height: 20px; background: url('${tex('gui/ornament')}') center / contain no-repeat; image-rendering: pixelated; }
.stats { display: flex; margin-top: 22px; border: 1px solid rgba(255,170,100,.35); background: rgba(12,7,14,.84); box-shadow: 0 18px 50px rgba(0,0,0,.55); }
.stats > div { flex: 1; padding: 14px 24px 12px; border-left: 1px solid rgba(255,170,100,.2); }
.stats > div:first-child { border-left: 0; }
.stats .t { font-family: HwSerif, serif; font-weight: 600; font-size: 15px; letter-spacing: .2em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: HwSerif, serif; font-weight: 600; font-size: 30px; color: var(--header); margin-top: 4px; white-space: nowrap; }
b.hi { color: var(--header); font-weight: 600; }
.px { image-rendering: pixelated; }
`;

// Embers rising and a few pale soul motes, the two ambient particles of a Hallowed Night. Deterministic.
const motes = `<canvas class="motes"></canvas><script>
(() => { const c = document.querySelector('canvas.motes'); const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = 11;
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  for (let i = 0; i < 80; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .2 + rnd() * .5, z = 1 + rnd() * 2;
    const soul = rnd() < .18; const col = soul ? '160,235,240' : '255,150,60';
    const h = g.createRadialGradient(x, y, 0, x, y, z * 5); h.addColorStop(0, 'rgba(' + col + ',' + a * .5 + ')'); h.addColorStop(1, 'rgba(' + col + ',0)');
    g.fillStyle = h; g.beginPath(); g.arc(x, y, z * 5, 0, Math.PI * 2); g.fill();
    g.fillStyle = soul ? 'rgba(225,255,255,' + a + ')' : 'rgba(255,214,150,' + a + ')'; g.fillRect(Math.round(x - z / 2), Math.round(y - z / 2), Math.ceil(z), Math.ceil(z)); } })();
</script>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const ornament = (size = 28, style = '') => `<img class="orn" src="${tex('gui/ornament')}" style="width:${size}px;height:${size}px;${style}">`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:26px;background:linear-gradient(90deg,rgba(0,0,0,0),rgba(20,10,22,.9) 30%,rgba(20,10,22,.9) 70%,rgba(0,0,0,0))">
    <div class="rule" style="width:300px;transform:scaleX(-1)"></div>
    ${ornament(30)}
    <div class="display flame glow" style="font-size:58px;white-space:nowrap;line-height:1">${title}</div>
    ${ornament(30)}
    <div class="rule" style="width:300px"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.6"></div>
  <div class="vignette"></div>${motes}
  <div style="position:absolute;left:80px;top:0;bottom:0;width:${width - artWidth - 150}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 24 ? ' style="font-size:18px;letter-spacing:.16em"' : ''}>${ornament(26)}${kicker}</div>
    <div class="display flame glow" style="font-size:74px;line-height:1.0;margin:18px 0 24px">${title}</div>
    <div class="rule" style="width:80%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:60px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in 1920x1080 pixels) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * 1920 / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '', color = '') => `<div class="frame" style="${color ? `border-color:${color}99;` : ''}${style}">${inner}</div>`;
const label = (text, style, small = '') => `<div class="label" style="${style}">${text}${small ? `<small>${small}</small>` : ''}</div>`;
const stats = (items, color) => `<div class="stats"${color ? ` style="border-top:2px solid ${color}"` : ''}>${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="t">${t}</div><div class="v">${v}</div></div>`).join('')}</div>`;
// Two overlapping captures.
const pair = (first, second, firstLabel, secondLabel, firstSpot = 'left:20px;top:20px', height = 700) => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:74%"><img src="${shot(first)}" style="display:block;width:100%"></div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:74%"><img src="${shot(second)}" style="display:block;width:100%"></div>
    ${label(firstLabel, firstSpot)}${label(secondLabel, 'right:20px;bottom:20px')}</div>`;
// A large capture with a smaller one overlapping its corner.
const inset = (main, small, crop, mainLabel, smallLabel, smallWidth = 420) => `<div style="position:relative">
    ${framed(`<img src="${shot(main)}">`)}
    <div class="frame" style="position:absolute;right:-24px;bottom:-40px;width:${smallWidth}px">${region(small, ...crop, smallWidth)}</div>
    ${mainLabel ? label(mainLabel, 'left:20px;top:20px') : ''}${smallLabel ? label(smallLabel, `right:-24px;bottom:${Math.round(smallWidth * crop[3] / crop[2]) - 20}px`) : ''}</div>`;

// The eight candies, their textures and what eating one does (CandyItem.java, default durations).
const CANDIES = [
  ['candy_corn', 'Candy Corn', 'Sugar Rush that stacks'],
  ['caramel_apple', 'Caramel Apple', 'Regeneration, Absorption, Sugar Rush'],
  ['ghost_gummy', 'Ghost Gummy', 'Invisibility and Slow Falling, 20 s'],
  ['pumpkin_truffle', 'Pumpkin Truffle', 'Cures Spooked and Sugar Crash'],
  ['witch_lollipop', 'Witch’s Lollipop', 'A random hex, usually a good one'],
  ['bat_wing_licorice', 'Bat Wing Licorice', 'Night Vision; monsters glow'],
  ['eyeball_jawbreaker', 'Eyeball Jawbreaker', 'Strength for 45 s'],
  ['soul_toffee', 'Soul Toffee', 'Spirit Ward for 2 minutes'],
];

const versionText = VERSIONS.map((v) => 'Minecraft ' + v.minecraft).join(' · ');

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('horseman.jpg')}" style="position:absolute;left:560px;top:-250px;width:1500px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(11,7,16,.97) 0%,rgba(11,7,16,.9) 30%,rgba(11,7,16,.35) 56%,rgba(11,7,16,0) 76%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(11,7,16,.75) 0%,rgba(11,7,16,0) 32%)"></div>
    <div class="vignette"></div>${motes}
    <img class="px" src="${source('logo.png')}" style="position:absolute;left:84px;top:96px;width:192px;height:192px;box-shadow:0 20px 60px rgba(0,0,0,.6),0 0 60px rgba(255,110,30,.25)">
    <div style="position:absolute;left:310px;top:74px;width:900px">
      <div class="kicker">${ornament(24)}${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display flame glow" style="font-size:150px;line-height:1;margin:6px 0 10px;white-space:nowrap">Hallowed</div>
      <div class="rule" style="width:620px;margin-bottom:22px"></div>
      <div style="font-size:34px;color:var(--header);line-height:1.3;font-style:italic">Light your lanterns. Mind the Witching Hour.</div>
    </div>
    <div style="position:absolute;left:88px;bottom:70px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}
      <span style="font-size:24px;color:var(--dim);margin-left:8px">${versionText}</span></div>`),

  icon: () => page(512, 512, `<img class="px" src="${source('logo.png')}" style="position:absolute;inset:0;width:512px;height:512px">`, true),

  'card-nights': () => card({
    kicker: 'When the season turns', title: 'Hallowed<br>Nights',
    points: ['From October 15 to November 5, every night is a Hallowed Night', 'Out of season, one full moon in four turns Hallowed',
      'A violet sky, drifting embers and leaves, and a swollen Harvest Moon', 'At midnight a bell strikes twelve: the Witching Hour',
      'Spirits gather, and the Cursed Pumpkin can wake'],
    art: inset('witching-hour.jpg', 'harvest-moon.jpg', [420, 120, 1000, 640], '', 'Harvest Moon', 380)
      + `<div style="height:56px"></div>` + stats([['Season', 'Oct 15 – Nov 5', 1.4], ['Witching Hour', '12 – 3 AM'], ['Off season', '1 moon in 4']]) }),

  'card-trick': () => card({
    kicker: 'On every doorstep', title: 'Trick or<br>Treat',
    points: ['Craft a Treat Pail and hold it out to a villager on a Hallowed Night', '72% of doors give candy, 90% if you come in costume',
      'Any carved pumpkin, mob head or the Witch Hat counts', 'Six harmless tricks: bats, a pumpkin on your head, a spook and more',
      'Each villager answers the door once a night'],
    art: pair('trick-or-treat.jpg', 'spook.jpg', 'Treat', 'Trick') }),

  'card-candy': () => card({
    kicker: 'Eight candies', title: 'Sweets with<br>a bite',
    points: ['Every candy does something different', 'Sugar Rush stacks: faster on foot, faster swings, faster mining',
      'Past half the cap it turns into Sugar Rush II, then a Sugar Crash', 'Monsters killed on a Hallowed Night drop candy, costumed ones always',
      'Eat all eight for Sweet Tooth'],
    art: `<div class="frame" style="background:rgba(12,7,14,.92);padding:34px 40px;display:grid;grid-template-columns:1fr 1fr;gap:26px 36px">
      ${CANDIES.map(([id, name, effect]) => `<div style="display:flex;align-items:center;gap:22px">
        <div style="width:88px;height:88px;flex:none;display:flex;align-items:center;justify-content:center;background:radial-gradient(circle,rgba(255,140,50,.22),rgba(255,140,50,0) 70%)">
          <img class="px" src="${tex('item/' + id)}" style="width:80px;height:80px"></div>
        <div><div class="display" style="font-size:36px;color:var(--header);line-height:1.05">${name}</div>
          <div style="font-size:21px;color:var(--dim);margin-top:4px">${effect}</div></div></div>`).join('')}</div>
      ${stats([['Sugar Rush cap', '60 s'], ['Rush II', 'above 30 s'], ['Candy drops', '20% · 100%']])}` }),

  'card-carving': () => card({
    kicker: 'Shears and a torch', title: 'Carve a<br>face',
    points: ['Use shears on a carved pumpkin to carve the next face', 'Sneak to go back one', 'Classic, Grin, Fangs, Cat, Scream, Wink, Skull and Bat',
      'A torch lights it. A soul torch gives it a cold blue flame', 'Wear any of them as a costume'],
    art: framed(region('carved-faces.jpg', 400, 250, 1240, 640, 978)) + stats([['Faces', '8'], ['Styles', 'carved · lit · soul', 1.6], ['Costume', 'yes']]) }),

  'card-ward': () => card({
    kicker: 'Keep the dark out', title: 'Lanterns<br>ward the night',
    points: ['No spirit appears within 12 blocks of a lit jack o’lantern', 'Soul jack o’lanterns guard 20 blocks',
      'Spirits that wander close turn and flee', 'Vanilla jack o’lanterns work too, so villages are safer', 'Line your walls and sleep easy'],
    art: framed(region('lantern-ward.jpg', 240, 180, 1440, 810, 978)) + stats([['Jack o’lantern', '12 blocks'], ['Soul lantern', '20 blocks'], ['Spirits', 'flee']]) }),

  'card-spirits': () => card({
    kicker: 'They come at midnight', title: 'Spirits',
    points: ['They drift out on Hallowed Nights, thickest in the Witching Hour', 'They swoop straight through walls and leave you Spooked',
      'Spooked slows you, and your heart pounds', 'They drop Ectoplasm, the start of every cursed thing',
      'Soul Toffee makes them take you for one of their own'],
    art: framed(region('spirits.jpg', 400, 120, 1280, 720, 978)) + stats([['Health', '16'], ['Per player', 'up to 4'], ['At dawn', 'they fade']]) }),

  'card-wisps': () => card({
    kicker: 'Follow the light', title: 'Will-o’-<br>the-wisps',
    points: ['Some Hallowed Nights, a wisp of foxfire appears near you', 'Follow it: it leads the way and waits if you fall behind',
      'Where it stops it sinks into the ground and leaves a Hallowed Cache', 'Candy, treasure and sometimes a music disc inside',
      'Violet wisps are cursed: three spirits guard a richer cache'],
    art: `<div style="position:relative">${framed(region('will-o-wisp.jpg', 200, 40, 1440, 810, 978))}
      <div class="frame" style="position:absolute;right:-24px;bottom:-40px;width:400px">${region('cache-burst.jpg', 260, 0, 1400, 960, 400)}</div>
      ${label('Hallowed Cache', 'right:-24px;bottom:254px')}</div>` }),

  'card-horseman': () => card({
    kicker: 'The boss of the night', title: 'The Hollow<br>Horseman',
    points: ['A headless rider on a burning nightmare steed', 'Scythe sweeps, flaming jack o’lantern bombs and thundering charges',
      'Lure a charge into a wall and he is stunned: 35% more damage', 'He has his own boss bar and banner', 'He rides away at dawn if you have not felled him'],
    art: framed(region('horseman.jpg', 300, 220, 1300, 731, 978), '', '#ff6a1a') + stats([['Health', '320'], ['Armor', '8'], ['Phase two', '50%']], '#ff6a1a') }),

  'card-summon': () => card({
    kicker: 'Summoning', title: 'Something<br>wicked',
    points: ['Craft it from a Soul Jack o’Lantern, six Ectoplasm and two Bones', 'Place it in the Overworld and wait for a Hallowed Night',
      'When the bell strikes twelve it wakes and pulses green', 'Then the Horseman rises from the earth beside it',
      'Not on Peaceful, and only one Horseman rides within 160 blocks'],
    art: pair('cursed-pumpkin.jpg', 'horseman-arrives.jpg', 'Cursed Pumpkin', 'The Horseman rises') }),

  'card-fight': () => card({
    kicker: 'The second phase', title: 'Unhorsed',
    points: ['At half health his steed collapses and he fights on foot', 'He calls spirits to his side',
      'He throws his own burning head at you, and it comes back', 'Headless, he takes 50% more damage',
      'Felled: a piñata of candy, his scythe or his head, and Ectoplasm'],
    art: framed(region('horseman-phase-two.jpg', 380, 220, 1200, 675, 978), '', '#ff6a1a')
      + stats([['Candy', '22 – 29'], ['Music disc', '35%'], ['Cursed Pumpkin', '15%']], '#ff6a1a') }),

  'card-spoils': () => card({
    kicker: 'Spoils of the night', title: 'Scythe, head<br>and hat',
    points: ['<b class="hi">Reaper Scythe</b>: Reap cuts everything around you and harvests ripe crops, replanting them', 'Killing blows with the scythe heal you',
      '<b class="hi">Horseman’s Head</b>: worn, Night Vision, and spirits leave you be', '<b class="hi">Witch Hat</b>: a costume for the doorstep',
      '<b class="hi">Witching Hour</b>: an original waltz on a music disc'],
    art: inset('reaper-scythe.jpg', 'inventory.jpg', [690, 280, 1100, 530], 'Reap', 'Your haul', 500) }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Every night<br>your way',
    points: ['Seasonal, every night, full moons only, or by command', 'Spawn rates, ward radii, candy drops and every trick',
      'The Horseman’s health, damage and armor', 'Client options for the sky, fog, banners and jumpscares', 'Commented files, reload with <b class="hi">/hallowed reload</b>'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">${[
      ['config/hallowed/hallowed.json', [['mode', '"seasonal"'], ['start', '"10-15"'], ['end', '"11-05"'], ['offseason_full_moon_chance', '0.25'],
        ['lantern_ward_radius', '12']], true],
      ['config/hallowed/client.json', [['sky_tint', 'true'], ['harvest_moon', 'true'], ['banners', 'true'], ['jumpscares', 'true'], ['intensity', '1.0']], true]]
      .map(([file, rows, more]) => `<div class="frame" style="background:rgba(12,7,14,.93);padding:24px 36px;font-family:monospace;font-size:23px;line-height:1.5;color:var(--text)">
        <div style="color:var(--dim)">${file}</div>
        <pre style="margin:10px 0 0;font-family:inherit">{
${rows.map(([k, v], i) => `  <span style="color:var(--amber)">"${k}"</span>: <span style="color:${v.startsWith('"') ? 'var(--soul)' : 'var(--violet)'}">${v}</span>${i < rows.length - 1 || more ? ',' : ''}`).join('\n')}${more ? '\n  <span style="color:var(--dim)">…</span>' : ''}
}</pre></div>`).join('')}</div>` }),
};

const headers = { 'header-nights': 'Hallowed Nights', 'header-trick': 'Trick or Treat', 'header-candy': 'The Candy', 'header-carving': 'Carved Faces',
  'header-spirits': 'Spirits & Wisps', 'header-horseman': 'The Hollow Horseman', 'header-spoils': 'Spoils', 'header-config': 'Configuration',
  'header-everything': 'Everything Else', 'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:18px">
  <div class="rule" style="width:520px;transform:scaleX(-1)"></div>${ornament(22)}<div class="rule" style="width:520px"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch();
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name === 'icon' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
