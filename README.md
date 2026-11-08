# SkatScores

An Android score sheet for **Skat** and **Doppelkopf**. Enter each round in a few taps and the
app keeps the totals, the dealer and the round count, draws the course of the list as a chart,
and builds long-term statistics for every player profile.

- Skat: three-player lists with classic scoring or Seeger-Fabian tournament scoring
  (±50 per game, +40 for the defenders), including Null, Grand, Hand, Schneider, Schwarz,
  Ouvert and overbid games.
- Doppelkopf: four-player lists with normal games and solos, announcements, Absagen and,
  optionally, extra points (foxes, Doppelkopf, Karlchen).
- Player profiles shared across both games, with per-player statistics.
- Share a list as text. German and English, light and dark theme.

## Privacy

The app works entirely offline: no internet permission, no ads, no analytics. Everything is
stored on the device. See [docs/privacy-policy.md](docs/privacy-policy.md).

## Building

Requirements: Android Studio with JDK 17 or newer, Android SDK 37.

```
gradlew assembleDebug          # debug build
gradlew testDebugUnitTest      # unit tests, including database tests on Robolectric
gradlew bundleRelease          # release bundle (signed if keystore.properties is present)
```

Release signing reads the upload key from `keystore.properties` (see
`keystore.properties.example`); what else is needed for Google Play is in
[docs/play-console.md](docs/play-console.md).

## Structure

- `game/skat`, `game/doppelkopf`: domain, use cases and screens per game.
- `player`: profiles and statistics.
- `persistence`: SQLite schema (`Schema.kt`), versioned migrations (`Migrations.kt`) and the
  change signals screens observe.
