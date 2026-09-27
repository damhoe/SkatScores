# Play Console release checklist

What has to happen outside the code before SkatScores can go to production. The code side of
Phase 3 (target API 36, release signing) is done in `app/build.gradle.kts`.

## 1. Upload key and Play App Signing

1. Create the upload key once (keep the file and both passwords backed up outside the repo):

   ```
   keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 4096 \
           -validity 10000 -alias upload
   ```

2. Copy `keystore.properties.example` to `keystore.properties` in the project root and fill it
   in. Both files are git-ignored. On a build server, set `SKATSCORES_UPLOAD_STORE_FILE`,
   `SKATSCORES_UPLOAD_STORE_PASSWORD`, `SKATSCORES_UPLOAD_KEY_ALIAS` and
   `SKATSCORES_UPLOAD_KEY_PASSWORD` instead.
3. Build: `gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
   Without a key the bundle is built unsigned, which Play rejects.
4. In Play Console, create the app and accept **Play App Signing** (Google holds the app signing
   key; yours is only the upload key, and can be reset through Play support if lost).

## 2. Target API level

`targetSdk = 36`. Play requires new apps and updates to target the level of the previous year's
Android release; check the current deadline under *Policy → App content* or the
"Target API level requirements" help page before uploading.

Behaviour that API 36 changes, to check on an Android 16 device or emulator:

- Predictive back is on: back from the round sheet, settings sheets and the player screens
  should animate and close them. The app has no custom back handling.
- On large screens (≥ 600 dp) orientation and resizability locks are ignored. The app sets
  none, so only layout quality on tablets/foldables is at stake (Phase 4).
- Edge-to-edge cannot be opted out of; the app already draws edge-to-edge.

## 3. App content (Policy → App content)

### Privacy policy

Required even though the app collects nothing. Fill in the placeholders in
`docs/privacy-policy.md` (name, address, contact email, date) and publish it at a public URL,
for example with GitHub Pages from the `docs/` folder of `damhoe/SkatScores`
(Settings → Pages → Branch `main`, folder `/docs`), giving
`https://damhoe.github.io/SkatScores/privacy-policy`. Enter that URL in the console and,
ideally, link it from the About screen (Phase 4).

### Data safety

Answers that match the app as it is (no INTERNET permission, no SDKs that collect data, data
stored on the device only):

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | Not applicable (nothing collected) |
| Do you provide a way for users to request that their data is deleted? | Not applicable; data is local and removed by deleting it in the app, clearing app data or uninstalling |

Notes:

- Player names entered by the user stay on the device; data that never leaves the device is
  not "collected" in Play's sense.
- Sharing a list is started by the user through the Android share sheet, which Play does not
  count as sharing by the developer.
- Android's own Auto Backup copies the database into the user's Google backup. Check the
  current Data safety help text on system backup before submitting; as of writing, data sent
  through Android's backup service is not considered collected by the developer.
- If a crash reporter, analytics or a sync backend is ever added, these answers change.

### Content rating (IARC questionnaire)

- Category: **Utility, Productivity, Communication, or Other** (it is a score sheet, not a game).
- Violence, sexuality, language, controlled substances: **No** to all.
- Gambling: the app does **not** offer real-money or simulated gambling; it only records the
  results of card games played in person. Answer **No** to simulated gambling.
- User interaction / sharing content: **No** user-to-user interaction, **no** location sharing.
  Sharing a text summary via the system share sheet is not in-app communication.

Expected rating: PEGI 3 / USK 0 / Everyone.

### Target audience and content

- Target age groups: **18 and over** (or 13+). Avoid selecting under-13 groups; that would put
  the app under the Families policy for no benefit.
- Appeals to children: **No**.

### Other declarations

- Ads: **No ads**.
- App access: **All functionality is available without special access** (no login).
- News app, COVID-19, government, financial features, health: **No**.

## 4. Testing before production

Personal developer accounts created after 13 November 2023 have to run a **closed test with at
least 12 testers who stay opted in for 14 consecutive days** before they can apply for
production access. Organisation accounts are exempt.

1. Create a closed testing track, upload the signed bundle, add testers by email list or
   Google Group.
2. Share the opt-in link; testers must install from Play and stay opted in for 14 days.
3. Then apply for production under *Dashboard → Apply for production* and answer the questions
   about the test.

Use the pre-launch report of the first upload: it runs the app on real devices and flags
crashes, accessibility issues (touch targets, contrast) and API 36 problems.

## 5. Store listing

- App name (≤ 30 characters), short description (≤ 80), full description (≤ 4000) in German
  (primary market) and English.
- App icon 512 × 512 PNG; feature graphic 1024 × 500.
- At least 2 phone screenshots (ideally 4–8: home, Skat list, round entry, chart, player
  statistics, Doppelkopf list); tablet screenshots if tablets are targeted.
- Category: *Cards & Casino* is for games; for a score keeper *Tools* or *Productivity* fits
  better and avoids gambling-related review.
- Contact email (shown publicly) and optionally a website.
