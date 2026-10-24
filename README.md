# MBD2 More Extended

[中文](#中文) | [English](#english)

MBD2 More Extended 是 [Multiblocked2](https://github.com/Low-Drag-MC/Multiblocked2)（MBD2）的功能拓展模组。
它**不新增任何物品、方块或创造模式标签页**，只通过 MBD2 API 扩展多方块机器的行为。

MBD2 More Extended is a feature add-on for [Multiblocked2](https://github.com/Low-Drag-MC/Multiblocked2).
It adds **no items, blocks or creative tabs** and extends multiblock behavior purely through the MBD2 API.

---

## 中文

### 功能

- **NBT 数值比较配方条件 `nbt_compare`**
  - 从机器的 `customData` 读取一个顶层数值键，按 `>=`、`>`、`<=`、`<`、`==`、`!=` 与目标值比较
  - 支持 JSON / 网络 / NBT 序列化，配方编辑器中有 key、运算符、数值配置
  - 继承 `RecipeCondition` 的“反转（reverse）”开关
- **魔源存储特质 `ars_source_storage`（需要 Ars Nouveau）**
  - 容量、传输速率可配置，source 存档持久化并同步客户端
  - Ars 交互方向 **IN / OUT / BOTH**，不区分机器面
    - `IN`：魔源链接可以推入，仪式不能抽取
    - `OUT`：仪式可以抽取，并每 10 tick 向 5 格内可接收目标推送
    - `BOTH`：类似魔源罐，可推入也可被抽取，不主动推送
  - 注册 `SourceManager` provider，随机器加载/卸载自动注册与注销
- **魔源配方能力 `ars_source`（整数）**
  - 配方输入消耗魔源，输出产出魔源
  - 配方处理器方向 **IN / OUT / BOTH**，支持槽位名与“是否唯一（distinct）”
- **KubeJS 可选绑定**
  - `nbtCompare(key, operator, value)`

### 环境要求

| 项目 | 要求 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+（必需）** |
| LDLib | MBD2 的硬依赖 |
| Ars Nouveau | 4.12+（可选，魔源功能） |
| KubeJS | 2001.6.5+（可选，`nbtCompare`） |
| JEI | 可选，**不限制版本**（本模组不声明 JEI 依赖） |

### 安装

1. 安装 Minecraft 1.20.1 + Forge 47.4.10
2. 安装 **Multiblocked2 1.0.37+** 及其依赖 LDLib
3. 可选：安装 Ars Nouveau（魔源功能）、KubeJS（脚本绑定）
4. 把 `mbd2_more_extended-1.0.0.jar` 放进 `mods/`

### 使用

#### 1. `nbt_compare` 配方条件

配方 JSON（`recipeConditions` 内）：

```json
{
  "type": "nbt_compare",
  "data": { "key": "magnetizer_level", "operator": ">=", "value": 36.0 }
}
```

KubeJS 服务端脚本：

```js
m.addCondition(nbtCompare('magnetizer_level', '>=', level * 36))
```

#### 2. 魔源特质

在机器编辑器里添加“新生魔艺 - 魔源存储”，按需配置：

- 容量 / 传输速率
- Ars 交互方向：`IN` / `OUT` / `BOTH`
- 配方处理器方向：`IN` / `OUT` / `BOTH`
- 槽位名：逗号分隔，对应配方内容的 `slotName`
- 是否唯一：对应 MBD2 的 `distinct`

#### 3. `ars_source` 配方

```json
"inputs": {
  "ars_source": [ { "content": 500 } ]
},
"outputs": {
  "ars_source": [ { "content": 1000 } ]
}
```

机器上的魔源特质方向需要支持对应方向：消耗（inputs）用 `IN` 或 `BOTH`，产出（outputs）用 `OUT` 或 `BOTH`。

### 兼容性 / 已知限制

- 仅针对**官方 Multiblocked2**（当前 1.0.37）。`Multiblocked2 Unofficial` 是 fork，内部 API 与工程文件格式
  都不兼容，**不要混用**
- `nbt_compare` 只读取 `customData` 的**顶层键**，且只支持数值
- 比较统一按 `double` 处理，`==` / `!=` 为精确比较；无法识别的运算符回退为 `>=`
- JEI 不参与运行时依赖校验，旧版本 JEI（如 15.20.0.133）可正常加载
- ProbeJS 6.0.0 启动时会从 `static.wolfgirl.moe` 下载远程文档，国内网络可能较慢

### 从源码构建

需要 JDK 17+，仓库中的 `libs/` 已包含本地开发用的 JEI / ProbeJS jar：

```bash
./gradlew clean build
# 产物：build/libs/mbd2_more_extended-1.0.0.jar
```

发布 jar 的 `META-INF/` 内包含 `LICENSE.txt`、`changelog.txt`、`CREDITS.txt`。
Ars Nouveau / GeckoLib / Curios 只是开发运行时的 `runtimeOnly` 依赖，不会打包进 jar。

### 链接

- 源码 / Issues：<https://github.com/gaozi233/mbd2_more_extended>
- 更新日志：[changelog.txt](changelog.txt)
- 致谢：[CREDITS.txt](CREDITS.txt)

### 许可

All Rights Reserved，详见 [LICENSE.txt](LICENSE.txt)。

---

## English

### Features

- **`nbt_compare` recipe condition**
  - Reads a top-level numeric key from the machine's `customData` and compares it with `>=`, `>`, `<=`, `<`, `==`, `!=`
  - JSON / network / NBT serialization; editor fields for key, operator and value
  - Inherited `RecipeCondition` reverse toggle
- **`ars_source_storage` trait (requires Ars Nouveau)**
  - Configurable capacity / transfer rate; source is persisted and synced
  - Ars I/O mode **IN / OUT / BOTH**, with no per-face configuration
    - `IN`: sourcelinks may push in; rituals cannot drain it
    - `OUT`: rituals may drain it; it pushes to accepting targets within 5 blocks every 10 ticks
    - `BOTH`: behaves like a source jar — accepts pushes and can be drained, but does not push
  - Registers a `SourceManager` provider and cleans it up on machine unload
- **`ars_source` recipe capability (integer)**
  - Recipe inputs consume source, outputs produce source
  - Recipe handler direction **IN / OUT / BOTH**, plus slot names and the distinct flag
- **Optional KubeJS binding**
  - `nbtCompare(key, operator, value)`

### Requirements

| Item | Requirement |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+ (required)** |
| LDLib | hard dependency of MBD2 |
| Ars Nouveau | 4.12+ (optional, source features) |
| KubeJS | 2001.6.5+ (optional, `nbtCompare`) |
| JEI | optional, **any version** (no JEI dependency is declared) |

### Installation

1. Install Minecraft 1.20.1 + Forge 47.4.10
2. Install **Multiblocked2 1.0.37+** and its LDLib dependency
3. Optional: Ars Nouveau (source features), KubeJS (script binding)
4. Put `mbd2_more_extended-1.0.0.jar` into `mods/`

### Usage

#### 1. `nbt_compare` recipe condition

Recipe JSON (`recipeConditions` entry):

```json
{
  "type": "nbt_compare",
  "data": { "key": "magnetizer_level", "operator": ">=", "value": 36.0 }
}
```

KubeJS server script:

```js
m.addCondition(nbtCompare('magnetizer_level', '>=', level * 36))
```

#### 2. Source trait

Add "Ars Nouveau - Source Storage" to the machine in the editor and configure:

- capacity / transfer rate
- Ars I/O mode: `IN` / `OUT` / `BOTH`
- recipe handler direction: `IN` / `OUT` / `BOTH`
- slot names: comma-separated, matching the recipe content `slotName`
- distinct flag: MBD2's `distinct` semantics

#### 3. `ars_source` recipes

```json
"inputs": {
  "ars_source": [ { "content": 500 } ]
},
"outputs": {
  "ars_source": [ { "content": 1000 } ]
}
```

The machine's source trait direction must support the matching side: `IN` or `BOTH` for inputs,
`OUT` or `BOTH` for outputs.

### Compatibility / limitations

- Targets the **official Multiblocked2** (currently 1.0.37). `Multiblocked2 Unofficial` is an
  incompatible fork with changed internals and project-file format — do not mix them
- `nbt_compare` only reads **top-level keys** of `customData`, and only numeric values
- Values are compared as `double`; `==` / `!=` are exact; unknown operators fall back to `>=`
- JEI is not a runtime dependency; older JEI versions (e.g. 15.20.0.133) are accepted
- ProbeJS 6.0.0 downloads remote docs from `static.wolfgirl.moe` on startup and may be slow

### Build from source

Requires JDK 17+. The repository's `libs/` folder contains the local JEI / ProbeJS jars:

```bash
./gradlew clean build
# output: build/libs/mbd2_more_extended-1.0.0.jar
```

The release jar contains `LICENSE.txt`, `changelog.txt` and `CREDITS.txt` under `META-INF/`.
Ars Nouveau / GeckoLib / Curios are `runtimeOnly` dev dependencies and are not bundled.

### Links

- Source / issues: <https://github.com/gaozi233/mbd2_more_extended>
- Changelog: [changelog.txt](changelog.txt)
- Credits: [CREDITS.txt](CREDITS.txt)

### License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
