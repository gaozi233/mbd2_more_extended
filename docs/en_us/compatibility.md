# Compatibility and Limitations

## Target versions

| Item | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | 1.0.37+ |
| This mod | 1.1.0 |

## Multiblocked2

Only the **official Multiblocked2** is supported.

`Multiblocked2 Unofficial` is a separate fork:

- Different internal API
- Different project-file format
- Do not mix it with the official version

## JEI

- This mod does not declare a JEI runtime dependency
- Older JEI versions can still load
- Recipe viewer integration comes from MBD2 / LDLib

## `nbt_compare`

- Only **top-level** `customData` keys are read
- Only numeric values are supported
- Values are compared as `double`
- `==` / `!=` are exact comparisons
- Unknown operators fall back to `>=`

## Overstack item slot

- Stackable items are scaled by the selected formula
- External pipes and chests usually still split stacks according to their own slot limits
- Output to other containers may split large stacks into multiple normal stacks
- Counts above 127 rely on this mod's networking and NBT compatibility handling
- Client and server should run the same version

## Multiplayer

- This mod must be installed on both client and server
- Source features also require Ars Nouveau on the server
- KubeJS recipe scripts require KubeJS on the server
- Editor enhancements and `/mbd2_more_extended config` are client-side

## ProbeJS

ProbeJS 6.0.0 downloads remote documentation from `static.wolfgirl.moe` on startup.

## Known incompatibilities

- Do not mix with `Multiblocked2 Unofficial`
- Do not mix different client and server jar versions
