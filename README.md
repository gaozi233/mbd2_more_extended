# MBD2 More Extended

MBD2 More Extended 是 [Multiblocked2](https://github.com/Low-Drag-MC/Multiblocked2)（MBD2）的功能拓展模组。

它**不新增任何物品、方块或创造模式标签页**，只通过 MBD2 API 扩展多方块机器的行为。

> 中文用户文档：[docs/zh_cn/index.md](docs/zh_cn/index.md)  
> English user guide: 待补充（中文审核完成后编写）

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
