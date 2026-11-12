# 编辑器增强

编辑器增强是**纯客户端**功能，不影响服务端逻辑。

## 文件列表滚动条

MBD 编辑器的文件/项目选择列表新增：

- 可见滚动条
- 鼠标滚轮滚动
- 点击轨道跳转
- 拖动滑块

适用于：

- 打开项目
- 另存为
- 导入资源
- 导出资源

## 配方类型搜索

机器配置中的“配方类型”选择器支持输入过滤。

可以输入配方类型 ID 的一部分，例如：

```text
create
mbd2
machine
```

候选项来自：

- MBD2 注册的配方类型
- `MBD2.getLocation()/recipe_type/*.rt`

## 客户端设置

### 打开方式

在客户端执行：

```text
/mbd2_more_extended config
```

这是客户端指令：

- 不发送网络包
- 不需要服务端权限
- 不影响服务端逻辑

### 设置项

| 设置 | 作用 |
|---|---|
| 全局开关 | 控制所有编辑器增强是否生效 |
| 文件列表滚动条 | 是否显示/启用滚动条 |
| 配方类型搜索 | 是否使用可输入过滤的选择器 |

### 全局开关语义

全局开关是**独立总闸**，不会修改子项保存的值。

全局关闭时：

- 两个子项按钮置灰
- 子项原来保存的开关值保留
- 两个增强都不生效

重新打开全局后：

- 子项按各自保存的值恢复

实际是否生效：

```text
globalEnabled && individualEnabled
```

### 配置保存位置

```text
config/mbd2_more_extended-client.properties
```

配置键：

```properties
editor.globalEnabled=true
editor.fileListScrollbar=true
editor.recipeTypeSearch=true
```

旧版单开关 `editorEnhancements` 会兼容迁移到三个新配置。
