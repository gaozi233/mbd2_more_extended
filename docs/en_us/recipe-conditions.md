# Recipe Conditions

This mod extends MBD2's recipe condition system and provides recipe UI condition slots.

## `nbt_compare`

Reads a **top-level numeric key** from the machine's `customData` and compares it with a target value.

Supported operators:

```text
>=  >  <=  <  ==  !=
```

### JSON

Place it in the recipe's `recipeConditions`:

```json
{
  "type": "nbt_compare",
  "data": {
    "key": "magnetizer_level",
    "operator": ">=",
    "value": 36.0
  }
}
```

### KubeJS

```js
m.addCondition(nbtCompare('magnetizer_level', '>=', 36))
```

### Notes

- Only top-level `customData` keys are read.
- Only numeric values are supported.
- Values are compared as `double`.
- `==` / `!=` are exact comparisons.
- An unrecognized operator falls back to `>=`.
- The condition inherits MBD2's `reverse` toggle.

## Built-in `block` condition: tags

The built-in `block` condition now has a `tags` field. No extra condition type is needed.

### JSON

```json
{
  "type": "block",
  "data": {
    "minCount": 1,
    "maxCount": 8,
    "blocks": ["minecraft:stone"],
    "tags": ["forge:stone"]
  }
}
```

`blocks` and `tags` can be used together.

### Editor

In the block-condition config panel:

- Block filter: select specific blocks
- Tag filter: select block tags
- Tooltips include tag conditions

### KubeJS

The original `blocksInStructure(...)` is unchanged.

The tag-aware variant uses a distinct method name to avoid overload ambiguity:

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone', '#forge:stone')
```

`entries` accepts:

- `Block`
- Block ID string
- `#tag` string
- `TagKey<Block>`

## Condition visibility `showInGui`

Every MBD2 recipe condition can control whether it appears in the recipe GUI's **condition description**.

- Default: `true`
- Toggle available in the condition editor
- Serialized to JSON / network / NBT

### KubeJS

On the condition object:

```js
m.dimension('minecraft:overworld').showInGui(false)
```

Or chained:

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone').showInGui(false)
```

## Recipe UI condition slots

Set an item slot's id in the recipe UI template to:

```text
@block_condition_0
@block_condition_1
@block_condition_2
```

The number is an index into `conditions`.

### Display behavior

- Cycles through the blocks/tag items of that condition
- Shows the condition tooltip on hover
- Adds a count-range lore line:

```text
Count: x ~ y
```

- A tag expands to at most 256 display items to avoid UI lag

### Index fallback

If the global condition at `@block_condition_0` is not a block condition, the slot uses the **first block condition** instead.

For example:

```text
conditions[0] = dimension
conditions[1] = block
conditions[2] = block
```

Then:

- `@block_condition_0` -> `conditions[1]`
- `@block_condition_1` -> `conditions[2]`

### JEI

Condition slots are exposed to JEI as `CATALYST` and are not recipe inputs or outputs.

They can therefore be used to look up recipes by the displayed block through JEI Uses.

### Important limitations

- Condition slots are not recipe inputs.
- They do not participate in recipe matching or consumption.
- Items cannot be inserted into or extracted from them.
