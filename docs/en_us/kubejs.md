# KubeJS API

Requires KubeJS.

The mod provides these bindings:

| Binding | Purpose |
|---|---|
| `nbtCompare` | Create an `nbt_compare` condition |
| `MBD2Helper` | Machine facing, position and customData helpers |
| `MBD2RecipeHelper` | Read-only recipe queries |
| `blocksInStructureAny` | Block condition with tags |
| `showInGui` | Condition description visibility |

## `nbtCompare`

```js
m.addCondition(nbtCompare('magnetizer_level', '>=', 36))
```

Arguments:

```text
nbtCompare(key, operator, value)
```

- `key`: top-level key in the machine's `customData`
- `operator`: `>=`, `>`, `<=`, `<`, `==`, `!=`
- `value`: numeric value

## `blocksInStructureAny`

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone', '#forge:stone')
```

Arguments:

```text
blocksInStructureAny(minCount, maxCount, ...entries)
```

`entries` accepts:

- `Block`
- Block ID string
- `#tag` string
- `TagKey<Block>`

The original `blocksInStructure(...)` is unchanged.

## `showInGui`

On a condition object:

```js
m.dimension('minecraft:overworld').showInGui(false)
```

Chained:

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone').showInGui(false)
```

## `MBD2Helper`

### Get a machine

```js
const machine = MBD2Helper.getMachineAt(level, pos)
```

Returns `null` if the block is not an MBD2 machine.

### Relative directions

```js
const directions = MBD2Helper.getRelativeDirections(machine)
const direction = MBD2Helper.getRelativeDirection(machine, 'back')
```

The returned map contains:

```text
front back left right up down
```

Only horizontal facing is supported; a machine without horizontal facing returns an empty map or `null`.

A `Direction` can also be passed directly:

```js
MBD2Helper.getRelativeDirections(direction)
MBD2Helper.getRelativeDirection(direction, 'left')
```

### Position offsets

Relative to machine facing:

```js
const target = MBD2Helper.offsetRelative(pos, machine, 'back', 2)
```

Absolute direction:

```js
const target = MBD2Helper.offsetAbsolute(pos, 'north', 3)
const target2 = MBD2Helper.offsetAbsolute(pos, direction, 3)
```

Returns `null` for an invalid direction.

### customData

Read:

```js
const value = MBD2Helper.getCustomData(machine, 'key', defaultValue)
```

Write:

```js
MBD2Helper.setCustomData(machine, 'key', value)
```

Delete:

```js
MBD2Helper.setCustomData(machine, 'key', null)
```

Write only when changed; returns whether a change occurred:

```js
const changed = MBD2Helper.setCustomDataIfChanged(machine, 'key', value)
```

Supported values:

- Number
- Boolean
- String
- NBT Tag
- Map
- Iterable

Numbers, strings and booleans are returned as normal Java values; compound/list tags are returned as the corresponding NBT tag.

## `MBD2RecipeHelper`

Read-only recipe queries. It does not accept a machine or event and does not modify recipe builders.

### capability

```js
const cap = MBD2RecipeHelper.capability('item')
```

Returns `null` when the capability does not exist.

### Content lists

```js
const inputs = MBD2RecipeHelper.inputContents(recipe, 'item')
const outputs = MBD2RecipeHelper.outputContents(recipe, 'item', true)
```

The third `perTick` argument is optional; when omitted, all contents are returned.

### First content

```js
const input = MBD2RecipeHelper.firstInput(recipe, 'ars_source', false)
const output = MBD2RecipeHelper.firstOutput(recipe, 'ars_source', false)
```

Returns `null` when none exists.

### Presence checks

```js
MBD2RecipeHelper.hasInput(recipe, 'item')
MBD2RecipeHelper.hasOutput(recipe, 'ars_source')
```

### Numeric totals

```js
const inputAmount = MBD2RecipeHelper.inputAmount(recipe, 'ars_source', false)
const outputAmount = MBD2RecipeHelper.outputAmount(recipe, 'ars_source', false)
```

Only numeric contents are counted; non-numeric contents are ignored.

Returned lists are read-only copies; missing entries return an empty list, `null` or `0`.
