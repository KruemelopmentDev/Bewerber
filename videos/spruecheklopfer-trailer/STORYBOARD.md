---
format: 1080x1920
duration: 39s
message: "Ein Wort, ein Tap, sofort ein absurder Werbespruch – direkt als Poster teilen."
arc: "Die Pointen-Maschine (Demo Loop + Feature-Benefit Cascade): Hook → Live-Demo → 248+ Vorlagen → Share-Poster → Eigene Vorlagen → Homescreen-Widget → Google Play CTA"
audience: Android-Nutzer im Google Play Store
mode: autonomous
music: none
---

## Video direction

- **Palette system** — from `frame.md` (Broadside remixed onto Bewerber Emerald): Dark register (`ink-black` `#0E1511` ground, `cream` `#DDE4DD` text, `fire-orange` `#28F078` lone accent, `border-dark` `#262827` 1px hairlines) for frames 1–6; full Accent register (`fire-orange` `#28F078` ground, `ink-black` `#0E1511` text, `ink-on-orange-muted` / `ink-on-orange-hint`) on the closing punchline flash and Frame 7 CTA. Never put cream text on `#28F078`; never add a second accent hue.
- **Typography** — Barlow lowercase weight 700–900 negative-tracked for display/headlines, IBM Plex Mono uppercase tracked `0.14em` for chrome kickers/tags/counters. Container law: `container-type: size` on frame ground, all type in `cqw`, legibility floor ≥ 2.4cqw on 9:16 portrait.
- **Motion grammar + reveal model** — long-tail smooth decel curves (`power3.out` default; crisp percussive cuts on beat-synced slogan swaps); sequential reveals spread across each frame's full duration (never front-load everything at t=0); no lazy breathing, no slow back-half camera drift. At most low-amplitude subtle jitter (`sine-wave-loop`) or live SVG internals during holds.
- **Rhythm / held-frame allocation** — Frames 1–5 run high-energy beat-synced reveals; **Frame 6 (Homescreen-Widget)** is the deliberate calm breather/hold frame before the neon-emerald **Frame 7 CTA** climax.
- **Caption-band keep-out** — keep all key visuals and text inside the top 83% safe zone (even though captions are skipped, this preserves consistent Google Play Store framing).
- **Negative list** — no slideshow front-loading-then-freezing; no floating screensaver cards; no bouncy `elastic.out`/`bounce.out` default entrances; no generic gradients or drop shadows (flat plane + 1px hairlines); no pure `#000`/`#fff`; no `repeat`/`yoyo` or `Math.random`.

## Frame 1 — Pointen-Hook

- scene: Würfel-Button klickt im Takt und feuert drei absurde Werbesprüche nacheinander im Vollbild ab
- voiceover: ""
- duration: 5.5s
- poster: 4.5s
- transition_in: cut
- status: animated
- src: compositions/frames/01-hook-pointen.html
- type: hook
- persuasion: Instant comedic payoff & curiosity
- beat: amusement + excitement
- blueprint: kinetic-type-beats (Adapt)
- asset_candidates:
- focal: none
- roles: typography = hero
- sfx: click-pop, impact-soft

narrativeRole: Sofortiger Lacher in Sekunde 1–5 (funktioniert im Play Store auch stumm) — beweist das Prinzip der App ohne Erklärung.
keyMessage: Jedes banale Wort wird in 1 Sekunde zum absurden Werbespruch.

Adapt: keep the in-place kinetic beat-slam signature; anchor a compact neon-emerald dice-tap pill above the massive lowercase Barlow 900 slogan slot so each tap visually triggers the next slogan.
Scene 1 (0.0–1.5s): Dark register (`ink-black` `#0E1511` ground, 1px top mono kicker `01 / POINTEN-MASCHINE`); centered dice-tap pill compresses and first slogan „20 % auf alles außer feierabend.“ slams in via **kinetic beat-slam** (`kinetic-beat-slam`) — Centered layout, ~65% canvas, 3 depth layers (dark ground + hairline rule + foreground type).
Scene 2 (1.5–3.3s): Second dice tap triggers **hard-cut / flash word-swap** (`discrete-text-sequence`) replacing the slogan with „duschen ohne döner macht keinen spaß.“ with `döner` highlighted in `fire-orange` (`#28F078`) — Centered layout, massive lowercase Barlow 900.
Scene 3 (3.3–5.5s): Third dice tap flips the register to full `fire-orange` (`#28F078`) ground with `ink-black` (`#0E1511`) massive stacked display „quadratisch. praktisch. käse.“ arriving via **kinetic beat-slam** (`kinetic-beat-slam`) and holding clean and still for the last 0.8s.

## Frame 2 — Live-App-Demo

- scene: Echte Sprücheklopfer-App im Smartphone-Frame zeigt das echte 7s-Emulator-Video mit Würfel-Dreh und Quick-Chips
- voiceover: ""
- duration: 6.5s
- poster: 3.5s
- transition_in: zoom-through
- status: animated
- src: compositions/frames/02-app-intro-live.html
- type: product_intro
- persuasion: Show-don't-tell proof
- beat: curiosity + delight
- blueprint: device-surface-showcase (Adapt)
- asset_candidates: assets/demo-dice-loop.mp4 — real 7s emulator screen recording of live Quick-Chips and spinning dice button; assets/app-icon.png — official Sprücheklopfer megaphone icon
- focal: assets/demo-dice-loop.mp4
- roles: demo-dice-loop = cutout · app-icon = supporting
- sfx: whoosh, click-pop

narrativeRole: Enthüllt die echte App hinter den Sprüchen und beweist das 1-Tap-Erlebnis im echten UI.
keyMessage: Ein Wort, ein Tap — Sprücheklopfer macht den Rest.

Adapt: keep the held device-hero signature in 9:16 portrait; place a clean hairline-framed phone bezel in the lower-center ~62% of the safe area with the real `demo-dice-loop.mp4` playing inside, topped by the app icon + lowercase statement.
Scene 1 (0.0–1.8s): Dark register (`ink-black` `#0E1511`); top mono kicker `02 / SPRÜCHEKLOPFER` + `app-icon.png` enter at top-left, while massive lowercase headline „ein wort. ein tap.“ arrives via **per-word staggered reveal** (`dynamic-content-sequencing`) — Stacked portrait layout, upper third.
Scene 2 (1.8–4.4s): The real `demo-dice-loop.mp4` phone device enters smoothly from below via **spring-pop entrance** (`spring-pop-entrance`, smooth `power3` settle) framed by a crisp 1px `#28F078` hairline border, playing live as chips and the dice button fire inside the real Android UI — Centered hero device, ~58% height, 3 depth layers (dark ground + subtle 1px grid lines + device frame).
Scene 3 (4.4–6.5s): A neon-emerald `1-TAP QUICK-CHIPS` mono callout strip + accent bar reveals beside the lower edge of the header via **bars / progress / star wipe** (`stat-bars-and-fills`) while the live app video keeps running cleanly to the cut.

## Frame 3 — 248+ Vorlagen

- scene: Riesiger Zähler 248+ zählt hoch, begleitet vom echten Käse-Screenshot und 1-Tap Quick-Chip-Badges
- voiceover: ""
- duration: 5.0s
- poster: 3.2s
- transition_in: push-slide LEFT
- status: animated
- src: compositions/frames/03-248-sprueche.html
- type: feature_showcase
- persuasion: Scale proof + concrete humor
- beat: excitement + amusement
- blueprint: dataviz-countup (Adapt)
- asset_candidates: assets/screen-hero-1.png — real Emerald Dark Mode main screen with Käse slogan
- focal: assets/screen-hero-1.png
- roles: screen-hero-1 = cutout
- sfx: riser, impact-soft

narrativeRole: Belegt die Tiefe der App (248 Vorlagen) und zeigt den ikonischen Material-3-Expressive-Hauptscreen in gestochen scharfer Qualität.
keyMessage: 248 fertige Werbespruch-Vorlagen — endlose Kombinationen per 1-Tap.

Adapt: keep the value-scaled count-up signature in the upper zone, then reveal the real `screen-hero-1.png` card + quick-word tags below in 9:16 stacked hierarchy.
Scene 1 (0.0–1.8s): Dark register (`ink-black` `#0E1511`); top-border stat card fills upper third as „248+“ counts up from 0 via **value-scaled counter** (`counting-dynamic-scale`) in `fire-orange` (`#28F078`) with mono label `FERTIGE WERBESPRÜCHE • 0 % SERIOSITÄT` — Stacked portrait editorial layout.
Scene 2 (1.8–3.8s): Real emulator screenshot `screen-hero-1.png` („Zur Nacht gönne ich mir gerne mal Käse“) slides cleanly into the lower-center focal zone via **spring-pop entrance** (`spring-pop-entrance`), while three mono quick-word tags (`#KÄSE`, `#COLA`, `#DÖNER`) expand outward along the hairline divider via **cluster→outward expansion** (`center-outward-expansion`).
Scene 3 (3.8–5.0s): Accent rule sweeps full width via **bars / progress / star wipe** (`stat-bars-and-fills`) and the frame holds sharp and still so the viewer reads the real Käse slogan.

## Frame 4 — Share-Poster-Export

- scene: Echter Share-BottomSheet-Screenshot zeigt das 1080×1080 Material 3 Expressive Poster zum Teilen in 1 Tap
- voiceover: ""
- duration: 6.0s
- poster: 4.0s
- transition_in: push-slide UP
- status: animated
- src: compositions/frames/04-share-poster.html
- type: feature_showcase
- persuasion: Social shareability & instant payoff
- beat: delight + status
- blueprint: video-text-pivot (Adapt)
- asset_candidates: assets/screen-share-sheet.png — real Share-Poster BottomSheet preview; assets/screen-hero-2.png — real Cola hero screen
- focal: assets/screen-share-sheet.png
- roles: screen-share-sheet = cutout · screen-hero-2 = supporting
- sfx: whoosh, chime

narrativeRole: Zeigt den größten Teilen-Anreiz für Gruppenchats: fertige 1080×1080 Share-Poster als Bild oder Text.
keyMessage: Direkt als 1080×1080 Poster in WhatsApp & Galerie teilen.

Adapt: keep the hero-pivots-aside-to-reveal-payoff signature; open on `screen-hero-2.png`, pivot smoothly into `screen-share-sheet.png` showing the square 1080×1080 poster preview, then land the bold export tags.
Scene 1 (0.0–1.8s): Dark register (`ink-black` `#0E1511`); mono kicker `04 / SHARE-POSTER` and headline „als poster teilen.“ enter top-left via **per-word staggered reveal** (`dynamic-content-sequencing`), while `screen-hero-2.png` sits anchored center-below.
Scene 2 (1.8–4.2s): `screen-hero-2.png` pivots left and recedes while `screen-share-sheet.png` (showing the real 1080×1080 Material 3 Expressive Share-Poster & green „Als Bild teilen“ button) swaps to center hero via **scale-swap** (`scale-swap-transition`) — Asymmetric layered-depth layout, 3 depth layers.
Scene 3 (4.2–6.0s): Three crisp mono export pills (`/ ALS BILD`, `/ ALS TEXT`, `/ GALERIE 1080×1080`) stagger in cleanly via **per-word staggered reveal** (`dynamic-content-sequencing`) and hold still for the read.

## Frame 5 — Katalog & Eigene Vorlagen

- scene: Echter Sprüche-Katalog-Screenshot mit Platzhalter-Chips _ und # für eigene Werbespruch-Vorlagen
- voiceover: ""
- duration: 5.5s
- poster: 3.5s
- transition_in: push-slide LEFT
- status: animated
- src: compositions/frames/05-katalog-vorlagen.html
- type: feature_showcase
- persuasion: Creative ownership & personalization
- beat: control + curiosity
- blueprint: grid-card-assemble (Adapt)
- asset_candidates: assets/screen-catalog.png — real Sprüche-Katalog & Eigene Vorlagen screen
- focal: assets/screen-catalog.png
- roles: screen-catalog = cutout
- sfx: click-pop, whoosh

narrativeRole: Zeigt, dass Nutzer nicht auf vorgegebene Sprüche beschränkt sind, sondern eigene Insider-Vorlagen mit Platzhaltern bauen können.
keyMessage: Eigene Vorlagen mit _ und # — perfekt für Insider-Witze.

Adapt: keep the staggered card-assemble signature; pair the real `screen-catalog.png` screenshot with two assembling placeholder formula cards (`_ = dein begriff` and `# = zufallswort`) that compose a live example line.
Scene 1 (0.0–1.6s): Dark register (`ink-black` `#0E1511`); mono kicker `05 / EIGENE VORLAGEN` and lowercase headline „bau deine eigenen sprüche.“ enter at top via **per-word staggered reveal** (`dynamic-content-sequencing`).
Scene 2 (1.6–3.8s): Two sharp hairline formula cards (`+ _ BEGRIFF` and `+ # ZUFALLSWORT`) assemble side-by-side via **cluster→outward expansion** (`center-outward-expansion`), followed by the real `screen-catalog.png` screen rising cleanly underneath via **spring-pop entrance** (`spring-pop-entrance`) — Stacked grid + hero screen layout.
Scene 3 (3.8–5.5s): Example formula bar „ohne _ ist # nur halb so gut!“ reveals with `_` and `#` lit in `fire-orange` (`#28F078`) via **keyword glow** (`asr-keyword-glow`) and holds clean.

## Frame 6 — Homescreen-Widget

- scene: Ruhiger Breather-Beat mit dem echten Homescreen-Widget und 1-Tap-Refresh-Hinweis vor dem Finale
- voiceover: ""
- duration: 5.0s
- poster: 2.8s
- transition_in: blur-crossfade
- status: animated
- src: compositions/frames/06-widget-homescreen.html
- type: benefit_highlight
- persuasion: Daily habit & effortless delight
- beat: ease + calm anticipation
- blueprint: titlecard-reveal (Adapt)
- asset_candidates: assets/widget-preview.png — real Glance Homescreen Widget preview
- focal: assets/widget-preview.png
- roles: widget-preview = cutout
- sfx: chime

narrativeRole: Bewusster Ruhe-Beat (Rhythmus-Kontrast vor dem Finale) — zeigt das interaktive Android-Homescreen-Widget für tägliche Lacher.
keyMessage: Jeden Tag ein neuer Spruch — direkt auf deinem Homescreen.

Adapt: keep the restrained single-move titlecard-reveal signature; let the real `widget-preview.png` card and clean two-line statement settle with generous negative space and hold still.
Scene 1 (0.0–1.8s): Dark register (`ink-black` `#0E1511`, ~50% negative space); mono kicker `06 / HOMESCREEN-WIDGET` and calm lowercase statement „jeden tag ein spruch. direkt am homescreen.“ enter with one smooth slide-up via **spring-pop entrance** (`spring-pop-entrance`, long-tail `power3`).
Scene 2 (1.8–3.6s): Real `widget-preview.png` lands centered inside a crisp 1px hairline card frame with a `1-TAP REFRESH • DYNAMIC COLORS` mono sub-bar revealed via **bars / progress / star wipe** (`stat-bars-and-fills`).
Scene 3 (3.6–5.0s): Deliberate calm hold — crisp, motionless read (at most subtle low-amplitude jitter via `sine-wave-loop` on the refresh icon badge) building clean contrast into the neon-emerald CTA.

## Frame 7 — Google Play CTA

- scene: Knalliges Neon-Emerald-Finale mit echtem Megafon-Icon, massivem Sprücheklopfer-Titel und Google Play Store Call-to-Action
- voiceover: ""
- duration: 5.5s
- poster: 3.5s
- transition_in: zoom-through
- status: animated
- src: compositions/frames/07-cta-playstore.html
- type: cta
- persuasion: Clear action & zero friction (free on Google Play)
- beat: triumph + urgency-to-act
- blueprint: logo-assemble-lockup (Adapt)
- asset_candidates: assets/app-icon.png — official Sprücheklopfer megaphone launcher icon
- focal: assets/app-icon.png
- roles: app-icon = cutout
- sfx: impact-soft, chime

narrativeRole: Unverwechselbarer Marken-Abschluss im vollen Neon-Emerald-Register mit klarem Download-Aufruf für den Google Play Store.
keyMessage: Sprücheklopfer — jetzt kostenlos im Google Play Store holen.

Adapt: keep the centered logo-assemble-lockup signature on Broadside's full Accent register (`fire-orange` `#28F078` ground, `ink-black` `#0E1511` ink) so the finale visually pops against the 6 dark frames.
Scene 1 (0.0–1.6s): Full Accent register (`fire-orange` `#28F078` ground, `ink-black` `#0E1511` ink); official `app-icon.png` settles dead-center via **spring-pop entrance** (`spring-pop-entrance`) framed by a crisp 1px dark hairline ring drawing on via **SVG self-draw** (`svg-path-draw`) — Centered hero lockup.
Scene 2 (1.6–3.6s): Massive lowercase Barlow 900 wordmark „sprücheklopfer“ lands below the icon via **spring-pop entrance** (`spring-pop-entrance`), followed by an inverted `ink-black` CTA bar „JETZT KOSTENLOS IM GOOGLE PLAY STORE“ with `fire-orange` text settling cleanly.
Scene 3 (3.6–5.5s): Mono footer `KRÜMELOPMENT DEV • ANDROID` settles beneath a 1px dark rule; clean, confident held lockup through the final frame.
