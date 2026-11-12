# Recipe Capabilities

## `ars_source`

Requires Ars Nouveau.

`ars_source` is an integer recipe capability:

- Recipe inputs consume source
- Recipe outputs produce source
- Recipe preview and JEI/EMI use the purple source texture

### Recipe JSON

```json
"inputs": {
  "ars_source": [
    { "content": 500 }
  ]
},
"outputs": {
  "ars_source": [
    { "content": 1000 }
  ]
}
```

### Direction requirements

The machine's `ars_source_storage` trait must support the matching direction:

| Recipe use | Trait direction |
|---|---|
| Input consumption | `IN` or `BOTH` |
| Output production | `OUT` or `BOTH` |

### Recipe handler settings

The source trait also has a recipe handler direction:

- `IN`: recipe input only
- `OUT`: recipe output only
- `BOTH`: both input and output

Additional settings:

- Slot names: comma-separated, matching the recipe content's `slotName`
- Distinct: MBD2's `distinct` semantics
