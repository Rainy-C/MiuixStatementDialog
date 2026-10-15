# Miuix Statement Dialog

纯 Kotlin + Jetpack Compose + Miuix 的声明弹窗演示项目。

## 功能

- 参考图片的完整作者信息、使用声明、三个操作按钮
- 弹性入场与退出动画
- Miuix Theme / Button / Text 等真实控件
- 自动明暗主题、沉浸式系统栏
- 不再显示选项写入 SharedPreferences；主页可随时重新预览或重置
- 图形全部在 Compose Canvas 绘制，无 XML 布局、无图片素材文件

## GitHub Actions 编译

将压缩包内容直接放入 GitHub 仓库根目录（包括 `.github` 隐藏目录），推送到 `main` 或 `master`，或到 `Actions` 页面手动运行 `Build Android APK`。

工作流使用 JDK 17、Gradle 8.13、Android SDK 36，运行 `:app:assembleDebug`。在工作流完成后，进入 `Artifacts` 下载 `MiuixStatementDialog-Debug`，解压可获得可安装的 `app-debug.apk`。

这个 Demo 不包含 Gradle Wrapper，GitHub Actions 会由 `setup-gradle` 安装指定版本的 Gradle。

## 技术说明

- `miuix-android:0.8.8`（v0.8.x 的旧坐标。不要直接换成 v0.9.x，后者已拆分模块）
- `minSdk=26`, `compileSdk=36`, `targetSdk=35`
- XML 仅保留 Android 平台必需的 `AndroidManifest.xml`，不使用任何布局或 Drawable XML
- “交流学习”目前演示 Toast，因为没有提供实际交流群链接
- **只进行了源码静态审查，未运行编译或设备测试**
