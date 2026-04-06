# Sign Leak Shield

This is a Fabric client mod for Minecraft 1.21.1 that defensively patches forced sign-editor translation and keybind leak probes.

Bypass's Anti Mod systems on Donut SMP and otehr like it.


Build:

1. Install Java 21.
2. Open the project in a terminal.
3. Run `./gradlew build` on Linux/macOS or `gradlew.bat build` on Windows.
4. The built jar will be placed in `build/libs`.

Notes:

- The project targets Fabric for Minecraft 1.21.1.
- The implementation is scoped to forced sign editor traffic and only rewrites matching outgoing sign update packets.
