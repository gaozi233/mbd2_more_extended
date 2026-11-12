# 安装与依赖

## 环境要求

| 项目 | 要求 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | **1.0.37+（必需）** |
| LDLib | MBD2 的硬依赖，随 MBD2 安装 |
| Ars Nouveau | 4.12+（可选，魔源功能） |
| KubeJS | 2001.6.5+（可选，脚本 API） |
| JEI | 可选，本模组不限制版本 |

## 客户端安装

1. 安装 Minecraft 1.20.1 和 Forge 47.4.10。
2. 安装 **Multiblocked2 1.0.37+** 以及它需要的 LDLib。
3. 可选安装 Ars Nouveau 或 KubeJS。
4. 将以下文件放入 `mods/`：

```text
mbd2_more_extended-1.1.0.jar
```

## 服务端安装

- MBD2、LDLib 和本模组需要同时安装在服务端。
- 使用魔源功能时，服务端也需要 Ars Nouveau。
- 使用 KubeJS 配方脚本时，服务端需要 KubeJS。
- 编辑器增强是客户端功能；专用服务端不会加载客户端编辑器配置。

## 版本兼容

- 仅支持**官方 Multiblocked2**。
- `Multiblocked2 Unofficial` 是另一个 fork，内部 API 和工程文件格式不兼容，不要混用。
- JEI 不是本模组的运行时依赖，旧版本 JEI 也可以正常加载。
- ProbeJS 6.0.0 启动时会尝试下载远程文档，网络较慢时可能延迟启动。

## 升级注意

- 从 1.0.0 升级到 1.1.0 不需要迁移机器工程。
- 过载物品槽是本模组新增的独立特质，不会替换原版 `item_slot`。
- 超过 127 的堆叠使用了本模组的同步和 NBT 兼容处理；客户端和服务端都应安装相同版本。
