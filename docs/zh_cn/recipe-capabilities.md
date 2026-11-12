# 配方能力

## `ars_source`

需要安装 Ars Nouveau。

`ars_source` 是整数型 recipe capability：

- 配方输入消耗魔源
- 配方输出产出魔源
- 配方预览和 JEI/EMI 使用紫色魔源纹理

### 配方 JSON

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

### 方向要求

机器上的 `ars_source_storage` 特质需要支持对应方向：

| 配方用途 | 特质方向 |
|---|---|
| 输入消耗 | `IN` 或 `BOTH` |
| 输出产出 | `OUT` 或 `BOTH` |

### 配方处理器配置

魔源特质同时有“配方处理器方向”：

- `IN`：只作为配方输入
- `OUT`：只作为配方输出
- `BOTH`：输入和输出都允许

还可以配置：

- 槽位名：逗号分隔，对应配方内容的 `slotName`
- 是否唯一：对应 MBD2 的 `distinct`
