# Sign Leak Shield

> Disclosure:
>
> This mod was built with the help of AI.

![Main Release](https://dock-it.dev/OnniSystems/ModBlockBypass/actions/workflows/main-release.yml/badge.svg?branch=main)
![Dev Verify](https://dock-it.dev/OnniSystems/ModBlockBypass/actions/workflows/dev-verify.yml/badge.svg?branch=dev)

This is a Fabric client mod for Minecraft 1.21.1 that defensively patches forced sign-editor translation and keybind leak probes.

Build and CI:

1. Install Java 21.
2. Build locally with `./gradlew build` on Linux/macOS or `gradlew.bat build` on Windows.
3. The built jar will be placed in `build/libs`.
4. `dev` branch pushes and pull requests run the verification workflow in [`.gitea/workflows/dev-verify.yml`](/mnt/c/Users/spenc/Documents/GitHub/ModBlockBypass/.gitea/workflows/dev-verify.yml).
5. `main` branch pushes and `v*` tags run the release workflow in [`.gitea/workflows/main-release.yml`](/mnt/c/Users/spenc/Documents/GitHub/ModBlockBypass/.gitea/workflows/main-release.yml), which publishes a release and uploads the jar.

Notes:

- The project targets Fabric for Minecraft 1.21.1.
- The implementation is scoped to forced sign editor traffic and only rewrites matching outgoing sign update packets.
