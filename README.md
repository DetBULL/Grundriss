# Grundriss – Planungs-Mod für Minecraft 26.3 (Fabric)

Mit **F6** löst sich deine Kamera vom Körper. Du fliegst über das Gelände (durch Bäume und Gestrüpp hindurch, aber nicht ins Erdreich) und setzt **halbtransparente, farbige Planblöcke**. Die gibt es nur bei dir, die echte Welt bleibt unverändert. Die Pläne sind auch außerhalb der Freecam sichtbar, so kannst du beim Bauen einfach „reinbauen“.

---

## Bauen (einmalig, ca. 5 Minuten)

1. **Java 25 installieren**, z. B. *Eclipse Temurin 25 (JDK)* von adoptium.net. Bei der Installation „Set JAVA_HOME“ anhaken.
2. Diesen Ordner entpacken, z. B. nach `C:\mods\grundriss`.
3. Im Ordner ein Terminal öffnen. Unter Windows: im Explorer in die Adresszeile `cmd` tippen und Enter drücken.
4. Diesen Befehl ausführen:
   - Windows: `gradlew.bat build`
   - Mac/Linux: `./gradlew build`

   Beim ersten Mal lädt Gradle Minecraft und Fabric herunter. Das dauert ein paar Minuten.
5. Die fertige Mod liegt danach unter **`build/libs/grundriss-1.0.0.jar`**.

## Installieren

1. Für Minecraft **26.3** brauchst du den **Fabric Loader** (fabricmc.net/use) und die **Fabric API** (von Modrinth, Version für 26.3).
2. Lege `grundriss-1.0.0.jar` und die Fabric-API-Jar in deinen `mods`-Ordner.

---

## Steuerung

| Taste | Funktion |
|---|---|
| **F6** | Planungs-Kamera an/aus |
| **WASD / Leertaste / Shift** | fliegen (vor, zurück, seitlich, hoch, runter) |
| **Strg** (Sprinten) | schneller fliegen |
| **Rechtsklick** | Planblock setzen |
| **Linksklick** | Planblock entfernen |
| **Mittelklick** | Farbe vom anvisierten Planblock übernehmen |
| **1–8** oder **Mausrad** | Farbe wählen |
| **G** | Werkzeug wechseln: Einzelblock → Linie → Fläche → Wände |
| **Alt halten** | Luft-Modus: Block schwebt vor dir, statt an Oberflächen zu kleben |
| **Alt + Mausrad** | Abstand im Luft-Modus ändern |
| **Rücktaste** | Rückgängig (bis zu 100 Schritte) |
| **H** | Pläne ein-/ausblenden (geht auch ohne Freecam) |
| **Bild↑ / Bild↓** | Ebenen-Ansicht: nur eine Höhe deutlich zeigen |
| **Ende** | wieder alle Ebenen zeigen |

Alle Tasten lassen sich unter *Optionen → Steuerung → Tastenbelegung → Grundriss* ändern.

### Werkzeuge
- **Einzelblock:** ein Klick, ein Block. Gedrückt halten malt weiter.
- **Linie:** Erst den Start anklicken, dann das Ende. Ideal für Mauern und Wege.
- **Fläche (gefüllt):** Zwei Ecken anklicken, alles dazwischen wird gefüllt. Ideal für Böden und Fundamente.
- **Wände (hohl):** Zwei Ecken anklicken, nur der Rand wird gesetzt. Liegen beide Ecken auf gleicher Höhe, entsteht ein **Grundriss-Umriss**. Für höhere Wände setzt du die zweite Ecke mit **Alt** in die Luft.

Bei Zwei-Klick-Werkzeugen siehst du vor dem zweiten Klick eine Vorschau. Mit der jeweils anderen Maustaste brichst du ab. Mit Linksklick statt Rechtsklick **löschst** du in derselben Form.

### Freecam-Kollision
- **Fest:** Erde, Stein, Sand, Kies, Erze, gebaute Blöcke und alles andere Massive.
- **Durchfliegbar:** Baumstämme, Blätter, Gras, Blumen, Büsche, Ranken, Schnee-Schichten, Kakteen, Bambus, Riesenpilze, Wasser und Lava.

Auch der Mauszeiger „sieht“ durch Bäume und Pflanzen hindurch. So kannst du direkt auf den Waldboden planen.

### Speichern
Pläne werden automatisch gespeichert, **pro Welt/Server und Dimension**, unter `.minecraft/config/grundriss/`. Um einen Plan komplett zu löschen, entferne die passende `.json`-Datei, während du nicht in der Welt bist.

---

## Hinweise
- Freecam gilt auf vielen öffentlichen Servern als Cheat. Im Singleplayer und mit Freunden ist das kein Problem.
- Planblöcke werden im Umkreis von ca. 190 Blöcken angezeigt.
- Eine Aktion kann höchstens 32.768 Blöcke auf einmal setzen.

Teile der Freecam basieren auf [MinecraftFreecam/Freecam](https://github.com/MinecraftFreecam/Freecam) (MIT-Lizenz).
