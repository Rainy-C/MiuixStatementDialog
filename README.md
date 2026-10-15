# Miuix Statement Dialog · Remote Config

纯 Kotlin + Compose + Miuix 弹窗 Demo。无 XML 布局（AndroidManifest.xml 是 Android 必需的声明文件）。

## 远程配置

- QQ 收藏分享链接固定为 `https://sharechain.qq.com/df1d47499a5944a23f9631b5145daefb?qq_aio_chat_type=2`。
- `RemoteDialogConfig.kt` 使用 Jsoup 下载 HTML、提取 `.note-content`，还原 HTML 实体，再用 `org.json` 解析。
- 读取 `version: 1` 和 `dialog` 下各项配置；支持开关、标题、作者、正文、按钮、链接、圆角、颜色、系统/亮/暗主题和动画。
- 启动时从云端刷新；失败时优先使用最后一次有效缓存，再退回 `app/src/main/assets/dialog_config.json`。
- 首页可查看同步状态、手动重新同步、重置“不再显示”。用户同意选择存储在 SharedPreferences；远程配置不会执行任意脚本。
- 远程 JSON 中 `buttons.learn.url` 只允许 http/https。若空则提示尚未配置；按钮 `enabled=false` 会隐藏。
- `version` 是 JSON 格式版本，不会因为正文修改而强制对已同意的用户再次显示。

## GitHub Actions

1. 将项目全部文件放入 GitHub 仓库根目录。
2. 推送到 `main`；工作流自动运行 `gradle :app:assembleDebug`。
3. 在 Actions → Artifacts 下载 `MiuixStatementDialog-Debug`。

工作流使用 JDK 17、Android API 36、Gradle 8.13，并且已将 Kotlin JVM target 改为 compilerOptions DSL。

这份源码只进行了静态审查，没有在本地编译或运行。QQ 链接能否被 Android 端直接抓取，以实际网络/腾讯服务端访问策略为准。
