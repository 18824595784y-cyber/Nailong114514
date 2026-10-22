# 任务清单统计

一个极简、离线的原生 Android 任务清单。记录目标，也允许如实记录失败。

黑色背景、深灰任务卡片、黄色添加按钮。没有登录、联网、标签、打卡或复杂图表。

## 功能

- 添加任务只需输入名称；通过右侧菜单编辑、删除，删除前需要确认。
- 点击左侧方框，选择「✓ 完成」或「× 失败」；点击菜单外即可取消。
- 历史记录默认折叠，可展开查看、纠正状态或恢复待办。
- 累计成功数、失败数和成功率；待办不参与统计。成功率以整数百分比显示。
- 使用 Room 本地保存，关闭并重启应用后数据仍保留。删除记录会相应更新统计。

## 下载与安装

前往 [Releases](https://github.com/18824595784y-cyber/Nailong114514/releases) 下载 APK，传到手机后点击安装，按系统提示允许文件管理器安装未知应用。需要 Android 8.0 或更高版本。

**旧测试版用户请注意：** 正式发布密钥与此前调试密钥不同，正式版不能直接覆盖调试版。请先保留旧应用与数据，不要为安装而直接卸载。当前应用没有数据导出或迁移功能。

## 界面截图

真实界面截图待补充。应用采用黑灰配色、白色文字与黄色圆形加号；待办位于上方，累计统计与折叠历史位于下方。

## 本地编译

使用 JDK 17、Android SDK 34，推荐在 Android Studio 中打开项目并等待 Gradle 同步完成。

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
```

输出：`app/build/outputs/apk/debug/app-debug.apk`。

项目使用 Gradle Wrapper 8.7、Android Gradle Plugin 8.5.2、Kotlin 1.9.24、Jetpack Compose Material 3 和 Room 2.6.1。无需修改系统 Gradle 版本。

公开发布构建：

```powershell
.\gradlew.bat assembleRelease
```

签名通过环境变量 `TODO_KEYSTORE`、`TODO_STORE_PASSWORD`、`TODO_KEY_ALIAS`、`TODO_KEY_PASSWORD` 注入。未配置时只会生成未签名 Release；发布前必须验证签名。请自行安全保管密钥，切勿提交私钥和密码。

## 版本说明

首个 GitHub Release 标签计划为 `v1.0.0`。为保持与已安装版本的应用元数据一致，本次 APK 内部版本仍为 `1.1`（versionCode 2）。

## 开源许可

采用 [MIT License](LICENSE)。第三方依赖遵循各自许可。
