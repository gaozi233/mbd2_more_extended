# 配方条件

本模组扩展了 MBD2 的配方条件系统，并提供配方 UI 条件槽位。

## `nbt_compare`

从机器的 `customData` 读取一个**顶层数值键**，与目标值比较。

支持运算符：

```text
>=  >  <=  <  ==  !=
```

### JSON

放在配方的 `recipeConditions` 中：

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

### 注意

- 只读取 `customData` 的顶层键。
- 只支持数值。
- 比较统一按 `double` 处理。
- `==` / `!=` 是精确比较。
- 无法识别的运算符会回退为 `>=`。
- 继承 MBD2 条件自带的“反转（reverse）”开关。

## 原版 `block` 条件支持标签

原版 `block` 条件新增 `tags` 字段，不需要安装额外条件类型。

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

`blocks` 和 `tags` 可以同时使用。

### 编辑器

在方块条件配置面板中：

- 方块过滤器：选择具体方块
- 标签过滤器：选择方块标签
- tooltip 会显示标签条件

### KubeJS

原有 `blocksInStructure(...)` 保持不变。

带标签的版本使用独立方法名，避免重载歧义：

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone', '#forge:stone')
```

`entries` 支持：

- `Block`
- 方块 ID 字符串
- `#标签` 字符串
- `TagKey<Block>`

## 条件描述显隐 `showInGui`

所有 MBD2 配方条件都可以控制是否出现在配方 GUI 的**条件描述**中。

- 默认值：`true`
- 条件编辑器中有对应开关
- 写入 JSON / 网络 / NBT

### KubeJS

条件对象本身：

```js
m.dimension('minecraft:overworld').showInGui(false)
```

或者链式写法：

```js
m.blocksInStructureAny(1, 8, 'minecraft:stone').showInGui(false)
```

## 配方 UI 条件槽位

在配方 UI 模板中，把物品槽 id 设置为：

```text
@block_condition_0
@block_condition_1
@block_condition_2
```

数字对应 `conditions` 中的索引。

### 显示行为

- 循环显示该条件中的方块或标签对应物品
- 悬停显示条件 tooltip
- 显示数量范围 Lore：

```text
数量范围：x ~ y
```

- 标签最多展开 256 个显示物品，避免 UI 卡顿

### 索引兜底

如果 `@block_condition_0` 对应的全局条件不是方块条件，会改为寻找**第 0 个方块条件**。

例如：

```text
conditions[0] = dimension
conditions[1] = block
conditions[2] = block
```

此时：

- `@block_condition_0` -> `conditions[1]`
- `@block_condition_1` -> `conditions[2]`

### JEI

条件槽位在 JEI 中作为 `CATALYST`，不参与配方输入输出。

因此可以用条件方块查询“用途（Uses）”。

### 重要限制

- 条件槽位不是配方输入。
- 不参与配方匹配和消耗。
- 不能放入/取出物品。
