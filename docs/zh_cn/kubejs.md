# KubeJS API

需要安装 KubeJS。

本模组提供以下绑定：

| 绑定 | 用途 |
|---|---|
| `nbtCompare` | 创建 `nbt_compare` 条件 |
| `MBD2Helper` | 机器朝向、坐标、customData 辅助 |
| `MBD2RecipeHelper` | 配方只读查询 |
| `blocksInStructureAny` | 带方块标签的方块条件 |
| `showInGui` | 条件描述显隐 |

## `nbtCompare`

```js
m.addCondition(nbtCompare('magnetizer_level', '>=', 36))
```

参数：

```text
nbtCompare(key, operator, value)
```

- `key`：机器 `customData` 顶层键
- `operator`：`>=`、`>`、`<=`、`<`、`==`、`!=`
- `value`：数值

## `blocksInStructureAny`

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone', '#forge:stone')
```

参数：

```text
blocksInStructureAny(minCount, maxCount, ...entries)
```

`entries` 支持：

- `Block`
- 方块 ID 字符串
- `#标签` 字符串
- `TagKey<Block>`

原有的 `blocksInStructure(...)` 不变。

## `showInGui`

条件对象：

```js
m.dimension('minecraft:overworld').showInGui(false)
```

链式：

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone').showInGui(false)
```

## `MBD2Helper`

### 查询机器

```js
const machine = MBD2Helper.getMachineAt(level, pos)
```

不是 MBD2 机器时返回 `null`。

### 相对方向

```js
const directions = MBD2Helper.getRelativeDirections(machine)
const direction = MBD2Helper.getRelativeDirection(machine, 'back')
```

返回的映射包含：

```text
front back left right up down
```

只对水平朝向有效；机器没有水平朝向时返回空 Map / `null`。

也可以直接传 `Direction`：

```js
MBD2Helper.getRelativeDirections(direction)
MBD2Helper.getRelativeDirection(direction, 'left')
```

### 坐标偏移

相对机器朝向：

```js
const target = MBD2Helper.offsetRelative(pos, machine, 'back', 2)
```

按绝对方向：

```js
const target = MBD2Helper.offsetAbsolute(pos, 'north', 3)
const target2 = MBD2Helper.offsetAbsolute(pos, direction, 3)
```

方向无效时返回 `null`。

### customData

读取：

```js
const value = MBD2Helper.getCustomData(machine, 'key', defaultValue)
```

写入：

```js
MBD2Helper.setCustomData(machine, 'key', value)
```

删除：

```js
MBD2Helper.setCustomData(machine, 'key', null)
```

仅在变化时写入，返回是否修改：

```js
const changed = MBD2Helper.setCustomDataIfChanged(machine, 'key', value)
```

支持的值：

- Number
- Boolean
- String
- NBT Tag
- Map
- Iterable

数值、字符串、布尔读取时返回普通 Java 值；复合/列表返回对应 NBT Tag。

## `MBD2RecipeHelper`

只读配方查询，不接收机器或事件，也不会修改配方 builder。

### capability

```js
const cap = MBD2RecipeHelper.capability('item')
```

不存在时返回 `null`。

### 内容列表

```js
const inputs = MBD2RecipeHelper.inputContents(recipe, 'item')
const outputs = MBD2RecipeHelper.outputContents(recipe, 'item', true)
```

第三个参数 `perTick` 可选；不传时返回全部内容。

### 第一个内容

```js
const input = MBD2RecipeHelper.firstInput(recipe, 'ars_source', false)
const output = MBD2RecipeHelper.firstOutput(recipe, 'ars_source', false)
```

没有时返回 `null`。

### 是否存在

```js
MBD2RecipeHelper.hasInput(recipe, 'item')
MBD2RecipeHelper.hasOutput(recipe, 'ars_source')
```

### 数值总量

```js
const inputAmount = MBD2RecipeHelper.inputAmount(recipe, 'ars_source', false)
const outputAmount = MBD2RecipeHelper.outputAmount(recipe, 'ars_source', false)
```

仅统计数值型 content，非数值 content 会被忽略。

返回的列表是只读副本；缺失时返回空列表、`null` 或 `0`。
