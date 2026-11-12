# Machine Traits

This mod adds the following machine traits:

- `ars_source_storage` — Source storage
- `overstack_item_slot` — Overstack item slot

## `ars_source_storage`

Requires Ars Nouveau.

Add it in the machine editor:

```text
Ars Nouveau - Source Storage
```

### Options

| Option | Description |
|---|---|
| Source capacity | Maximum source stored by the machine |
| Transfer rate | Maximum source moved per transfer |
| Ars I/O mode | `IN` / `OUT` / `BOTH` |
| Recipe handler direction | `IN` / `OUT` / `BOTH` |
| Slot names | Match the recipe content's `slotName` |
| Distinct | MBD2's `distinct` semantics |

### Ars I/O modes

- `IN`
  - Sourcelinks can push in
  - Rituals cannot drain it
- `OUT`
  - Rituals can drain it
  - Pushes to accepting targets within 5 blocks every 10 ticks
- `BOTH`
  - Similar to a source jar
  - Accepts pushes and can be drained
  - Does not actively push

### Persistence and UI

- Source amount is saved and synced to the client
- Provides a source bar UI
- Registers a `SourceManager` provider and cleans it up on machine unload

## `overstack_item_slot`

Allows normal stackable items to exceed the vanilla 64 limit.

It is a separate trait and does not replace the built-in `item_slot`. Add it to a machine when overstacking is needed:

```text
Overstack Item Slot
```

### Inherited options

- Slot count
- Slot limit
- Item filter
- Auto input/output
- Item renderer settings

### Trait-specific options

#### Allow overstack

```text
allowOverstack
```

Enabled by default.

When disabled, behavior is the same as a normal item slot.

#### Overstack value

```text
slotLimit
```

The editor allows **1 to Integer.MAX_VALUE**, no longer limited to 64.

#### Overstack mode

```text
EXACT
MULTIPLIER
```

### EXACT mode

Let the configured value be `n` and the item's normal max stack size be `x`:

```text
limit = floor(x * n / 64)
```

Therefore:

```text
normal 64-stack item -> n
16-stack item        -> floor(16 * n / 64)
1-stack item         -> floor(1 * n / 64)
```

Example with `n = 1000`:

```text
64-stack item -> 1000
16-stack item -> 250
1-stack item  -> 15
```

### MULTIPLIER mode

Let the configured value be `n`:

```text
limit = floor(x * n)
```

Example with `n = 2`:

```text
64-stack item -> 128
16-stack item -> 32
1-stack item  -> 2
```

### Input methods

All of these use the same overstack limit:

- Logistics input (hoppers, pipes, `insertItem`)
- GUI mouse clicks
- Shift quick move

### Counts above 127

Vanilla `ItemStack` NBT `Count` and network encoding use a byte, so values above 127 become negative.

This mod adds:

- Network synchronization support for counts above 127
- An extra NBT field in machine storage for the real count
- Preservation after leaving and re-entering the world

### Notes

- Other mods' pipes and chests usually still split stacks according to their own slot limits.
- Large stacks output to external containers may be split into multiple normal stacks.
- All stackable items are scaled by the selected formula; tools, potions and other low-stack items receive a correspondingly smaller limit.
