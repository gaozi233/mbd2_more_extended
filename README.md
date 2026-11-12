# MBD2 More Extended

[中文](#中文) | [English](#english)

## 中文

MBD2 More Extended 是 [Multiblocked2](https://github.com/Low-Drag-MC/Multiblocked2)（MBD2）的功能拓展模组。

它**不新增任何物品、方块或创造模式标签页**，只通过 MBD2 API 扩展多方块机器的行为。

> 中文用户文档：[docs/zh_cn/index.md](docs/zh_cn/index.md)  
> English user guide: [docs/en_us/index.md](docs/en_us/index.md)

---

## 概述

本模组围绕 MBD2 的配方条件、机器特质、配方能力和编辑器提供扩展：

- **配方条件**：`nbt_compare`、原版 `block` 条件标签、条件描述显隐、配方 UI 条件槽位
- **机器特质**：魔源存储、过载物品槽
- **配方能力**：`ars_source`
- **KubeJS**：配方辅助、机器朝向/坐标/customData 辅助
- **编辑器增强**：文件列表滚动条、配方类型搜索、客户端设置界面

详细用法、JSON 结构和 KubeJS 示例请见[中文用户文档](docs/zh_cn/index.md)。

## 环境要求

| 项目 | 要求 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+（必需）** |
| LDLib | MBD2 的硬依赖 |
| Ars Nouveau | 4.12+（可选，魔源功能） |
| KubeJS | 2001.6.5+（可选，脚本绑定） |
| JEI | 可选，不限制版本 |

## 安装

1. 安装 Minecraft 1.20.1 + Forge 47.4.10
2. 安装 **Multiblocked2 1.0.37+** 及其依赖 LDLib
3. 可选安装 Ars Nouveau（魔源功能）或 KubeJS（脚本绑定）
4. 将 `mbd2_more_extended-1.1.0.jar` 放入 `mods/`

## 文档

| 文档 | 内容 |
|---|---|
| [用户文档首页](docs/zh_cn/index.md) | 全部中文文档入口 |
| [安装与依赖](docs/zh_cn/installation.md) | 客户端/服务端安装、依赖说明 |
| [配方条件](docs/zh_cn/recipe-conditions.md) | `nbt_compare`、方块标签、`showInGui`、条件槽位 |
| [机器特质](docs/zh_cn/machine-traits.md) | 魔源存储、过载物品槽 |
| [配方能力](docs/zh_cn/recipe-capabilities.md) | `ars_source` |
| [KubeJS API](docs/zh_cn/kubejs.md) | 条件、机器、配方辅助方法 |
| [编辑器增强](docs/zh_cn/editor.md) | 滚动条、配方搜索、客户端设置 |
| [兼容性与限制](docs/zh_cn/compatibility.md) | 已知限制、多人游戏注意事项 |

版本更新记录：

- [1.1.0 更新日志](changelogs/1.1.0.md)
- [完整 changelog](changelog.txt)

## 从源码构建

需要 JDK 17+：

```bash
./gradlew clean build
# 产物：build/libs/mbd2_more_extended-1.1.0.jar
```

## 链接

- 源码 / Issues：<https://github.com/gaozi233/mbd2_more_extended>
- CurseForge：<https://www.curseforge.com/minecraft/mc-mods/mbd2-more-extended>
- 更新日志：[changelog.txt](changelog.txt)
- 致谢：[CREDITS.txt](CREDITS.txt)

## 许可

All Rights Reserved，详见 [LICENSE.txt](LICENSE.txt)。

---

## English

MBD2 More Extended is a feature add-on for [Multiblocked2](https://github.com/Low-Drag-MC/Multiblocked2) (MBD2).

It adds **no items, blocks or creative tabs** and extends multiblock behavior purely through the MBD2 API.

> English user guide: [docs/en_us/index.md](docs/en_us/index.md)

### Overview

The mod extends MBD2's recipe conditions, machine traits, recipe capabilities and editor:

- **Recipe conditions**: `nbt_compare`, block tags on the built-in `block` condition, `showInGui`, recipe UI condition slots
- **Machine traits**: Source storage, overstack item slot
- **Recipe capability**: `ars_source`
- **KubeJS**: recipe, machine facing/position and customData helpers
- **Editor enhancements**: file-list scrollbar, recipe type search, client settings dialog

See the [English user guide](docs/en_us/index.md) for full usage, JSON structures and KubeJS examples.

### Requirements

| Item | Requirement |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+ (required)** |
| LDLib | Hard dependency of MBD2 |
| Ars Nouveau | 4.12+ (optional, source features) |
| KubeJS | 2001.6.5+ (optional, script API) |
| JEI | Optional, no version restriction |

### Installation

1. Install Minecraft 1.20.1 + Forge 47.4.10.
2. Install **Multiblocked2 1.0.37+** and its LDLib dependency.
3. Optionally install Ars Nouveau (source features) or KubeJS (script API).
4. Place `mbd2_more_extended-1.1.0.jar` in `mods/`.

### Documentation

| Document | Contents |
|---|---|
| [User guide](docs/en_us/index.md) | English documentation index |
| [Installation](docs/en_us/installation.md) | Client/server installation and dependencies |
| [Recipe conditions](docs/en_us/recipe-conditions.md) | `nbt_compare`, block tags, `showInGui`, condition slots |
| [Machine traits](docs/en_us/machine-traits.md) | Source storage, overstack item slot |
| [Recipe capabilities](docs/en_us/recipe-capabilities.md) | `ars_source` |
| [KubeJS API](docs/en_us/kubejs.md) | Condition, machine and recipe helpers |
| [Editor enhancements](docs/en_us/editor.md) | Scrollbar, recipe search, client settings |
| [Compatibility](docs/en_us/compatibility.md) | Limitations and multiplayer notes |

Version history:

- [1.1.0 changelog](changelogs/1.1.0.md)
- [Full changelog](changelog.txt)

### Build from source

Requires JDK 17+:

```bash
./gradlew clean build
# output: build/libs/mbd2_more_extended-1.1.0.jar
```

### Links

- Source / issues: <https://github.com/gaozi233/mbd2_more_extended>
- CurseForge: <https://www.curseforge.com/minecraft/mc-mods/mbd2-more-extended>
- Changelog: [changelog.txt](changelog.txt)
- Credits: [CREDITS.txt](CREDITS.txt)

### License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).

