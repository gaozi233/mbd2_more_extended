# Installation and Dependencies

## Requirements

| Item | Requirement |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+ (required)** |
| LDLib | Hard dependency of MBD2, installed with MBD2 |
| Ars Nouveau | 4.12+ (optional, source features) |
| KubeJS | 2001.6.5+ (optional, script API) |
| JEI | Optional, no version restriction from this mod |

## Client installation

1. Install Minecraft 1.20.1 and Forge 47.4.10.
2. Install **Multiblocked2 1.0.37+** and the LDLib version it requires.
3. Optionally install Ars Nouveau or KubeJS.
4. Place the following file in `mods/`:

```text
mbd2_more_extended-1.1.0.jar
```

## Server installation

- MBD2, LDLib and this mod must also be installed on the server.
- Ars Nouveau is required on the server when using source features.
- KubeJS is required on the server when using KubeJS recipe scripts.
- Editor enhancements are client-side; a dedicated server does not load the client editor config.

## Version compatibility

- Only the **official Multiblocked2** is supported.
- `Multiblocked2 Unofficial` is a separate fork with incompatible internals and project-file format. Do not mix them.
- JEI is not a runtime dependency of this mod; older JEI versions can still load.
- ProbeJS 6.0.0 tries to download remote documentation on startup, which may delay startup on slow networks.

## Upgrade notes

- Upgrading from 1.0.0 to 1.1.0 does not require migrating machine projects.
- `overstack_item_slot` is a new independent trait; it does not replace the built-in `item_slot`.
- Stacks above 127 use this mod's networking and NBT compatibility handling; client and server should run the same version.
