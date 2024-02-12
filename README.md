# Mirai 早报插件

每天早上向已设置的群聊推送早报。

## 命令一览

`/` 符号前缀可省略

`(/)早报 开启` ：开启当前群聊的每日早报功能

`(/)早报 关闭` ：关闭当前群聊的每日早报功能

`(/)早报 配置 [API链接]` ：配置早报 API 链接（`[]` 符号无需输入）

`(/)早报 刷新` ：手动从 API 获取一次早报并发送

## 简单使用

### 安装

1. 从 [Release](https://github.com/Yos-X/mirai-morning-report-plugin/releases) 下载最新发行版
2. 在 `MCL 停止运行` 时 放入 `MCL/plugins` 文件夹

### 权限配置

本插件权限 ID 为：`yos.code.morning-report:yos-permission`

**注意：**

1. **插件内的权限判断逻辑已包含所有群的群主 / 管理员，不建议重复添加**
2. **插件仅能在群聊环境中使用**

### API 配置

发送 `(/)早报 配置 [API链接]` 即可

对于 API 的要求：直接返回早报图片，而不是图片直链

已知一些可用的 API：

```
# 每日早报
https://api.03c3.cn/api/zb
https://zj.v.api.aa1.cn/api/60s/
https://zj.v.api.aa1.cn/api/60s-old/

# 摸鱼人日历
https://api.vvhan.com/api/moyu
```

### 开启早报

在需要开启每日早报的群聊发送 `(/)早报 开启` 即可

### 开启早报

在需要关闭每日早报的群聊发送 `(/)早报 关闭` 即可

## 拓展

不止是早报，只要是每日定时更新的图片 API 都适用
