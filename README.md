# Crazy Diamond + Dvizhukha — Fabric 1.20.1

Requires Fabric Loader 0.15+ and Fabric API (0.92.2+1.20.1).

## Items
| Item | How to get | Use |
|------|-----------|-----|
| **Stand Arrow** | Craft: diamond (top), blaze rod (middle), feather (bottom), in one column; or creative tab "Combat" | Right click: awakens the Crazy Diamond stand |
| **Dvizhukha Arrow** | Craft: 3 diamond blocks (top row), netherite block + Stand Arrow + netherite block (middle), 3 gold blocks (bottom); or creative tab "Combat" | Right click: you become Dvizhukha (new skin + 4 abilities) |

Creative mode players can summon the stand without an arrow. Set
`StandNetworking.REQUIRE_STAND_ARROW = false` to let everybody do that.

Commands (need cheats / op): `/crazydiamond arrows` gives both arrows, `/crazydiamond mango` gives 16 mangoes, `/crazydiamond reset` removes the
stand and the Dvizhukha form again.

## Mango
Craft: melon slice + sweet berries + orange dye -> 2 mangoes (or creative tab "Combat"). Right click throws it
and plays the "mango" sound. The file is a placeholder beep for now: replace
`assets/crazydiamond/sounds/mango.ogg` with the real sound (ogg vorbis, mono).

## Awakening track
`assets/crazydiamond/sounds/dvizhukha_theme.ogg` is your audio (27.5 s). The beat drops at about 18.3 s (tick ~366).
It is registered as the sound event `crazydiamond:dvizhukha_theme` for the future arrow cutscene.

## Controls (rebindable in Options → Controls → "Crazy Diamond")
| Input | What |
|-------|------|
| V | Summon / dismiss the stand |
| Left mouse button (click or hold) | Stand punch (while the stand is summoned) |
| R / Z / X / C / G / B | Barrage / Return Block / Heal Mode / Stone Shot / Disassemble / Repair Item |
| H | Dvizhukha: summon 5 clones |
| J | Dvizhukha: power strike (Warden-style sonic boom in white-blue-red) |
| K | Dvizhukha: pocket dimension (max 1:30, press K again to leave early) |
| Y | Dvizhukha: stop time (3 s, cooldown 20 s) |

## Dvizhukha abilities
- **Clones** – 5 copies of you (30 s) that fight monsters and whatever you attack. Cooldown 30 s.
- **Power strike** – 16-block sonic beam, 14 damage ignoring armour, big knockback. Cooldown 8 s.
- **Pocket dimension** – a private void dimension with a white/blue/red platform. Max 1:30, cooldown 45 s.
  (Defined in `data/crazydiamond/dimension/pocket.json`.)
- **Time stop** – everything nearby except you, your stand and your clones freezes for 3 s. Cooldown 20 s.

## Stand abilities
Punch, Barrage (a swirl of fists around the stand + "ORA" text), Return Block (restores blocks the stand
smashed and takes the dropped items back), Heal Mode, Stone Shot, Disassemble, Repair Item.

## Files you may want to edit
- Stand skin: `assets/crazydiamond/textures/entity/crazy_diamond.png`
- Dvizhukha skin (player + clones): `assets/crazydiamond/textures/entity/dvizhukha.png`
- Arrow icons: `assets/crazydiamond/textures/item/`
- Recipes: `data/crazydiamond/recipes/`
- Pose / animations: `client/StandModel.java`

## Build
Open the folder in IntelliJ IDEA and run the Gradle `build` task, or use `gradle build` (JDK 17).
The jar is in `build/libs/` (use the one without `-sources`).
