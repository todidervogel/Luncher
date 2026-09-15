# Luncher

Ein minimalistischer Android-Launcher. Der Startbildschirm ist leer, der
App-Start ist absichtlich unbequem: kein Icon-Raster, keine Suchvorschläge,
keine App-Liste. Wer eine App öffnen will, tippt ihren Namen aus dem Kopf.

Gebaut für ein Samsung Galaxy S9 (Android 9, API 28), nicht gerootet.
Privatprojekt, keine Veröffentlichung.

## Bedienung

| Geste | Wirkung |
|---|---|
| Swipe nach unten | Eingabefeld für den App-Start (Tastatur erst beim Antippen) |
| Swipe nach oben | in V1 ohne Funktion, reserviert |
| Langer Druck | Einstellungen |
| Zurück-Taste | auf Home: nichts. Sonst zurück auf Home |

Der Name muss exakt stimmen — Groß- und Kleinschreibung ist egal, Umlaute
nicht. Es gibt bewusst **kein** Fuzzy-, Präfix- oder Teilstring-Matching und
keine Vorschläge, was gemeint gewesen sein könnte. Für sperrige Namen
("Samsung Internet Browser") gibt es Aliase in den Einstellungen.

## APK bauen

Der Workflow [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml)
baut die APK auf GitHub — lokal ist kein Android SDK nötig.

* Bei jedem Push läuft er automatisch. Die fertigen APKs (Debug und Release)
  hängen als Artefakt `luncher-apk` am Workflow-Lauf.
* Manuell: Actions → *APK bauen* → *Run workflow*.
* Ein Tag `v*` (z. B. `git tag v1.0 && git push origin v1.0`) hängt die APKs
  zusätzlich an ein GitHub-Release — das lässt sich direkt vom Handy
  herunterladen.

Die Release-APK ist mit dem Debug-Schlüssel signiert, damit sie sich ohne
eigenen Keystore installieren lässt. Für ein rein privates Gerät reicht das;
ein Update über eine anders signierte APK geht dann aber nur nach
Deinstallation.

Lokal (mit installiertem Android SDK):

```bash
./gradlew testDebugUnitTest   # Namensabgleich
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

## Installieren

1. APK aufs Gerät laden und installieren (Installation aus unbekannten
   Quellen erlauben).
2. Einmal Home drücken und Luncher als Standard wählen — oder in den
   Einstellungen des Launchers *Als Standard-Launcher setzen* antippen
   (öffnet die System-Einstellung, Android 9 hat keine direkte API dafür).

## Aufbau

```
app/src/main/java/de/tobi/luncher/
  MainActivity.kt          Single Activity, HOME-Intent-Filter, Screen-Umschaltung
  LauncherViewModel.kt     Zustand als StateFlow
  ui/
    HomeScreen.kt          Uhr, Datum, Gestenerkennung
    LauncherScreen.kt      Eingabefeld, Fehlerzustand
    SettingsScreen.kt      Alias-Verwaltung, Standard-Launcher, Version
    theme/                 Farben, Typografie, dunkles Theme
  launcher/
    AppRepository.kt       installierte Apps laden und cachen
    AppMatcher.kt          Namensabgleich, reine Funktion, unit-getestet
    AppLauncher.kt         Intent-Start
    InstalledApp.kt        label, packageName, activityName
  settings/AliasStore.kt   DataStore-Zugriff für Aliase
  time/                    ACTION_TIME_TICK, Formatierung
```

Einzige Berechtigung: `VIBRATE`. Kein DI, keine Room-DB, kein Netzwerk.
