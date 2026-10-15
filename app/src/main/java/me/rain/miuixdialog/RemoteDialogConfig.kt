package me.rain.miuixdialog

import android.content.Context
import android.net.Uri
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

private const val REMOTE_NOTE_URL = "https://sharechain.qq.com/df1d47499a5944a23f9631b5145daefb?qq_aio_chat_type=2"
private const val CACHE_KEY = "cached_remote_dialog_json"

internal data class DialogHeader(
    val showIcon: Boolean = true,
    val icon: String = "whale",
    val author: String = "神秘人",
    val title: String = "软件使用声明",
    val subtitle: String = "请在继续使用前阅读以下说明"
)

internal data class DialogNotice(
    val showNotice: Boolean = true,
    val noticeTitle: String = "重要说明",
    val text: String = "郑重声明：本软件来源于网络，非本人开发。此软件仅用于学习和研究软件的设计思想与原理，绝不可用于任何商业或非法目的。"
)

internal data class DialogButton(
    val enabled: Boolean = true,
    val text: String,
    val action: String,
    val url: String? = null
)

internal data class DialogButtons(
    val learn: DialogButton = DialogButton(text = "交流学习", action = "open_url"),
    val reject: DialogButton = DialogButton(text = "不同意", action = "dismiss"),
    val accept: DialogButton = DialogButton(text = "同意并不再显示", action = "accept_and_remember")
)

internal data class DialogAppearance(
    val theme: String = "system",
    val cornerRadiusDp: Int = 34,
    val accentColor: String = "#292929",
    val animation: String = "spring_scale",
    val animationEnabled: Boolean = true
) {
    fun accent(): Color = try {
        Color(AndroidColor.parseColor(accentColor))
    } catch (_: IllegalArgumentException) {
        Color(0xFF292929)
    }
}

internal data class RemoteDialogConfig(
    val enabled: Boolean = true,
    val showOnLaunch: Boolean = true,
    val rememberAcceptance: Boolean = true,
    val dismissOnBack: Boolean = true,
    val dismissOnOutside: Boolean = false,
    val header: DialogHeader = DialogHeader(),
    val content: DialogNotice = DialogNotice(),
    val buttons: DialogButtons = DialogButtons(),
    val appearance: DialogAppearance = DialogAppearance()
)

internal object RemoteDialogRepository {
    private fun JSONObject.section(name: String): JSONObject = optJSONObject(name) ?: JSONObject()

    fun parse(json: String): RemoteDialogConfig {
        val root = JSONObject(json)
        require(root.optInt("version", -1) == 1) { "不支持的配置版本" }
        val d = root.getJSONObject("dialog")
        val header = d.section("header")
        val content = d.section("content")
        val buttons = d.section("buttons")
        val appearance = d.section("appearance")
        val learn = buttons.section("learn")
        val reject = buttons.section("reject")
        val accept = buttons.section("accept")

        fun button(obj: JSONObject, fallback: DialogButton): DialogButton = DialogButton(
            enabled = obj.optBoolean("enabled", fallback.enabled),
            text = obj.optString("text", fallback.text).take(100),
            action = obj.optString("action", fallback.action),
            url = obj.optString("url", "").takeIf { it.startsWith("https://") || it.startsWith("http://") }
        )

        return RemoteDialogConfig(
            enabled = d.optBoolean("enabled", true),
            showOnLaunch = d.optBoolean("showOnLaunch", true),
            rememberAcceptance = d.optBoolean("rememberAcceptance", true),
            dismissOnBack = d.optBoolean("dismissOnBack", true),
            dismissOnOutside = d.optBoolean("dismissOnOutside", false),
            header = DialogHeader(
                showIcon = header.optBoolean("showIcon", true),
                icon = header.optString("icon", "whale"),
                author = header.optString("author", "神秘人").take(120),
                title = header.optString("title", "软件使用声明").take(160),
                subtitle = header.optString("subtitle", "请在继续使用前阅读以下说明").take(300)
            ),
            content = DialogNotice(
                showNotice = content.optBoolean("showNotice", true),
                noticeTitle = content.optString("noticeTitle", "重要说明").take(120),
                text = content.optString("text", "").take(20000)
            ),
            buttons = DialogButtons(
                learn = button(learn, DialogButtons().learn),
                reject = button(reject, DialogButtons().reject),
                accept = button(accept, DialogButtons().accept)
            ),
            appearance = DialogAppearance(
                theme = appearance.optString("theme", "system").takeIf { it in listOf("system", "light", "dark") } ?: "system",
                cornerRadiusDp = appearance.optInt("cornerRadiusDp", 34).coerceIn(18, 48),
                accentColor = appearance.optString("accentColor", "#292929"),
                animation = appearance.optString("animation", "spring_scale"),
                animationEnabled = appearance.optBoolean("animationEnabled", true)
            )
        )
    }

    fun bundled(context: Context): RemoteDialogConfig = runCatching {
        context.assets.open("dialog_config.json").bufferedReader(Charsets.UTF_8)
            .use { parse(it.readText()) }
    }.getOrDefault(RemoteDialogConfig())

    fun cached(context: Context): RemoteDialogConfig? = runCatching {
        context.getSharedPreferences("statement_dialog_demo", Context.MODE_PRIVATE)
            .getString(CACHE_KEY, null)?.let(::parse)
    }.getOrNull()

    suspend fun fetchAndCache(context: Context): RemoteDialogConfig = withContext(Dispatchers.IO) {
        val document = Jsoup.connect(REMOTE_NOTE_URL)
            .userAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/125.0 Mobile Safari/537.36")
            .referrer("https://sharechain.qq.com/")
            .timeout(12000)
            .maxBodySize(2_000_000)
            .followRedirects(true)
            .get()

        val note = document.selectFirst("article.note-body .note-content")
            ?: document.selectFirst(".note-content")

        // QQ 的不同页面模板可能把收藏内容放进 window.syncData.shareData.html_content。
        val rawText = if (note != null) {
            note.wholeText()
        } else {
            val script = document.select("script")
                .firstOrNull { it.data().contains("window.syncData =") }
                ?.data() ?: error("页面缺少 QQ 收藏内容，请检查分享链接的公开权限")
            val jsonPart = script.substringAfter("window.syncData =", "")
                .substringBefore(";")
            require(jsonPart.isNotBlank()) { "QQ 收藏数据为空" }
            val html = JSONObject(jsonPart)
                .getJSONObject("shareData")
                .getString("html_content")
            Jsoup.parseBodyFragment(html).body().wholeText()
        }

        var clean = rawText.replace('\u00a0', ' ').trim()
        repeat(2) {
            if (clean.contains("&quot;") || clean.contains("&#") || clean.contains("&nbsp;")) {
                clean = Parser.unescapeEntities(clean, false).replace('\u00a0', ' ').trim()
            }
        }
        val start = clean.indexOf('{')
        val end = clean.lastIndexOf('}')
        require(start >= 0 && end > start) { "收藏正文没有 JSON 对象" }
        val json = clean.substring(start, end + 1)
        require(json.length <= 128_000) { "远程配置过大" }
        val parsed = runCatching { parse(json) }.getOrElse {
            // 有些富文本编辑器会在下划线前插入 Markdown 转义。
            parse(json.replace("\\_", "_"))
        }
        context.getSharedPreferences("statement_dialog_demo", Context.MODE_PRIVATE)
            .edit().putString(CACHE_KEY, json).apply()
        parsed
    }

    fun isSafeWebUrl(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return runCatching {
            val uri = Uri.parse(value)
            (uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }
}
