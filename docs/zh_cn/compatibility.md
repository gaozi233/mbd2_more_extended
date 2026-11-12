# 兼容性与限制

## 目标版本

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Multiblocked2 | 1.0.37+ |
| 本模组 | 1.1.0 |

## Multiblocked2

只支持**官方 Multiblocked2**。

`Multiblocked2 Unofficial` 是另一个 fork：

- 内部 API 不同
- 工程文件格式不同
- 不要和官方版混用

## JEI

- 本模组不声明 JEI 运行时依赖
- 旧版本 JEI 也可以加载
- 配方查看器集成来自 MBD2 / LDLib

## `nbt_compare`

- 只读取 `customData` 的**顶层键**
- 只支持数值
- 按 `double` 比较
- `==` / `!=` 是精确比较
- 未知运算符回退为 `>=`

## 过载物品槽

- 仅对普通可堆叠物品按公式放大
- 外部管道/箱子通常仍按目标槽位上限拆堆
- 输出到其他容器时，大堆叠可能需要拆成多个普通堆叠
- 超过 127 的堆叠依赖本模组的同步与 NBT 兼容处理
- 客户端和服务端应安装相同版本

## 多人游戏

- 客户端和服务端都需要安装本模组
- 魔源功能需要服务端也安装 Ars Nouveau
- KubeJS 脚本在服务端执行时需要服务端安装 KubeJS
- 编辑器增强和 `/mbd2_more_extended config` 是客户端功能

## ProbeJS

ProbeJS 6.0.0 启动时会从 `static.wolfgirl.moe` 下载远程文档。

## 已知不兼容

- 不要与 `Multiblocked2 Unofficial` 混用
- 不要混用不同版本的客户端和服务端 jar
