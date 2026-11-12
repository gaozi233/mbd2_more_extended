# Editor Enhancements

Editor enhancements are **client-side only** and do not affect server logic.

## File-list scrollbar

The MBD editor's file/project selection list now provides:

- A visible scrollbar
- Mouse-wheel scrolling
- Track-click jumping
- Scrollbar dragging

It applies to:

- Open Project
- Save As
- Import Resource
- Export Resource

## Recipe type search

The recipe type selector in machine configuration supports text filtering.

Type part of a recipe type ID, for example:

```text
create
mbd2
machine
```

Candidates come from:

- Recipe types registered in MBD2
- `MBD2.getLocation()/recipe_type/*.rt`

## Client settings

### Opening the dialog

Run this on the client:

```text
/mbd2_more_extended config
```

It is a client command:

- Does not send a network packet
- Does not require server permission
- Does not affect server logic

### Settings

| Setting | Purpose |
|---|---|
| Global toggle | Controls whether all editor enhancements are active |
| File-list scrollbar | Whether the scrollbar is shown/enabled |
| Recipe type search | Whether to use the filterable selector |

### Global toggle behavior

The global toggle is an independent master switch and does not modify per-feature saved values.

When global is off:

- Both feature buttons are greyed out
- Their saved values are retained
- Neither enhancement is active

When global is turned back on:

- Each feature returns to its saved value

Effective state:

```text
globalEnabled && individualEnabled
```

### Config location

```text
config/mbd2_more_extended-client.properties
```

Keys:

```properties
editor.globalEnabled=true
editor.fileListScrollbar=true
editor.recipeTypeSearch=true
```

The legacy single `editorEnhancements` key is migrated to the three settings.
