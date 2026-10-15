package me.rain.miuixdialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private const val PREFS = "statement_dialog_demo"
private const val PREF_HIDE = "hide_statement"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DialogDemo() }
    }
}

private data class Palette(
    val background: Color,
    val surface: Color,
    val mutedSurface: Color,
    val mainText: Color,
    val subText: Color,
    val line: Color
)

private fun palette(dark: Boolean) = if (dark) {
    Palette(Color(0xFF0E1013), Color(0xFF202226), Color(0xFF2A2D32),
        Color(0xFFF7F8FA), Color(0xFFADB0B8), Color(0xFF393C42))
} else {
    Palette(Color(0xFFF5F6F8), Color.White, Color(0xFFF4F5F7),
        Color(0xFF191B20), Color(0xFF777D87), Color(0xFFE9EBEF))
}

@Composable
private fun DialogDemo() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var config by remember { mutableStateOf(RemoteDialogRepository.cached(context) ?: RemoteDialogRepository.bundled(context)) }
    var syncStatus by remember { mutableStateOf("正在获取远程配置…") }
    var refreshing by remember { mutableIntStateOf(0) }
    var initialized by remember { mutableStateOf(false) }
    var neverShow by remember { mutableStateOf(prefs.getBoolean(PREF_HIDE, false)) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var lastAction by rememberSaveable { mutableStateOf("尚未选择") }

    val systemDark = isSystemInDarkTheme()
    val dark = when (config.appearance.theme) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
    val c = palette(dark)
    val accent = config.appearance.accent()

    LaunchedEffect(refreshing) {
        val fallback = RemoteDialogRepository.cached(context)
        try {
            config = RemoteDialogRepository.fetchAndCache(context)
            syncStatus = "云端同步成功"
        } catch (error: Exception) {
            config = fallback ?: RemoteDialogRepository.bundled(context)
            val reason = error.message?.take(55) ?: "未知错误"
            syncStatus = if (fallback != null) "使用本地缓存 · $reason" else "使用内置配置 · $reason"
        }
        if (!initialized) {
            initialized = true
            showDialog = config.enabled && config.showOnLaunch &&
                !(config.rememberAcceptance && prefs.getBoolean(PREF_HIDE, false))
        } else if (!config.enabled) {
            showDialog = false
        }
    }

    MiuixTheme(colors = if (dark) darkColorScheme() else lightColorScheme()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "MIUIX  /  COMPONENT LAB",
                color = accent,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "声明弹窗",
                color = c.mainText,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                letterSpacing = (-1).sp
            )
            Text(
                text = "远程配置 · 精致动效 · 无 XML 布局",
                color = c.subText,
                fontSize = 14.sp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(c.surface)
                    .border(1.dp, c.line, RoundedCornerShape(30.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                if (dark) listOf(Color(0xFF2A2C30), Color(0xFF181A1D))
                                else listOf(Color(0xFFF2F2F3), Color(0xFFE4E6E9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .background(accent.copy(alpha = if (dark) 0.22f else 0.09f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        WhaleLogo(Modifier.size(79.dp), accent)
                    }
                    Text(
                        "DIALOG / REMOTE",
                        color = c.subText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(config.header.title, color = c.mainText, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(syncStatus, color = c.subText, fontSize = 12.sp)
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = {
                        if (config.enabled) showDialog = true
                        else Toast.makeText(context, "远程配置已关闭弹窗", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    cornerRadius = 18.dp,
                    colors = ButtonDefaults.buttonColors(color = accent, contentColor = Color.White)
                ) {
                    Text("预览弹窗", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(23.dp))
                    .background(c.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("云端配置", color = c.mainText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (config.enabled) "已启用" else "已关闭", color = c.subText, fontSize = 12.sp)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("用户偏好", color = c.subText, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (neverShow) "不再自动显示" else "允许自动显示",
                        color = if (neverShow) accent else c.mainText, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("上次操作", color = c.subText, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Text(lastAction, color = c.mainText, fontSize = 13.sp)
                }
                Button(
                    onClick = { refreshing += 1 },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    cornerRadius = 15.dp
                ) { Text("重新同步云端", fontSize = 13.sp) }
                Button(
                    onClick = {
                        prefs.edit().remove(PREF_HIDE).apply()
                        neverShow = false
                        lastAction = "已重置"
                        Toast.makeText(context, "已清除不再显示记录", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    cornerRadius = 15.dp
                ) { Text("重置弹窗记录", fontSize = 13.sp) }
            }
            Spacer(Modifier.height(14.dp))
        }

        StatementDialog(
            show = showDialog && config.enabled,
            config = config,
            dark = dark,
            onDismiss = { showDialog = false },
            onLearn = {
                lastAction = "交流学习"
                when (config.buttons.learn.action) {
                    "dismiss" -> showDialog = false
                    "open_url" -> {
                        val url = config.buttons.learn.url
                        if (RemoteDialogRepository.isSafeWebUrl(url)) {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "尚未配置有效链接", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> Toast.makeText(context, "不支持的按钮操作", Toast.LENGTH_SHORT).show()
                }
            },
            onReject = {
                lastAction = "不同意"
                showDialog = false
            },
            onAccept = {
                val remember = config.rememberAcceptance &&
                    config.buttons.accept.action == "accept_and_remember"
                if (remember) {
                    prefs.edit().putBoolean(PREF_HIDE, true).apply()
                    neverShow = true
                }
                lastAction = if (remember) "同意并不再显示" else "同意"
                showDialog = false
            }
        )
    }
}

@Composable
private fun StatementDialog(
    show: Boolean,
    config: RemoteDialogConfig,
    dark: Boolean,
    onDismiss: () -> Unit,
    onLearn: () -> Unit,
    onReject: () -> Unit,
    onAccept: () -> Unit
) {
    val visible = remember { MutableTransitionState(false) }
    LaunchedEffect(show) { visible.targetState = show }
    if (show || visible.currentState || !visible.isIdle) {
        val c = palette(dark)
        val accent = config.appearance.accent()
        val corner = config.appearance.cornerRadiusDp.dp
        val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
        val enter = if (!config.appearance.animationEnabled || config.appearance.animation == "none") EnterTransition.None
            else if (config.appearance.animation == "spring_scale") {
                fadeIn(tween(220)) + scaleIn(initialScale = 0.88f,
                    animationSpec = spring(dampingRatio = 0.77f,
                        stiffness = Spring.StiffnessMediumLow))
            } else fadeIn(tween(220))
        val exit = if (!config.appearance.animationEnabled || config.appearance.animation == "none") ExitTransition.None
            else if (config.appearance.animation == "spring_scale") {
                fadeOut(tween(145)) + scaleOut(targetScale = 0.94f, animationSpec = tween(145))
            } else fadeOut(tween(145))

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = config.dismissOnOutside,
                dismissOnBackPress = config.dismissOnBack,
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(visibleState = visible, enter = enter, exit = exit) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                        .shadow(24.dp, RoundedCornerShape(corner))
                        .clip(RoundedCornerShape(corner))
                        .background(c.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (config.header.showIcon && config.header.icon == "whale") {
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(RoundedCornerShape(29.dp))
                                .background(accent.copy(alpha = if (dark) 0.22f else 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            WhaleLogo(Modifier.size(68.dp), accent)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (config.header.author.isNotBlank()) {
                        Text(config.header.author, color = c.subText, fontSize = 12.sp,
                            letterSpacing = 0.4.sp)
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(config.header.title, color = c.mainText, fontSize = 23.sp,
                        fontWeight = FontWeight.Bold)
                    if (config.header.subtitle.isNotBlank()) {
                        Spacer(Modifier.height(7.dp))
                        Text(config.header.subtitle, color = c.subText, fontSize = 12.5.sp)
                    }
                    if (config.content.showNotice) {
                        Spacer(Modifier.height(22.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(c.mutedSurface)
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(7.dp).background(accent, CircleShape))
                                Spacer(Modifier.width(9.dp))
                                Text(config.content.noticeTitle, fontSize = 12.sp,
                                    color = c.mainText, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(config.content.text,
                                color = c.mainText.copy(alpha = if (dark) 0.84f else 0.79f),
                                fontSize = 13.5.sp, lineHeight = 24.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (config.buttons.learn.enabled) {
                        Button(onClick = onLearn,
                            modifier = Modifier.fillMaxWidth().height(48.dp), cornerRadius = 16.dp) {
                            Text(config.buttons.learn.text, fontSize = 14.sp,
                                fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    if (config.buttons.reject.enabled) {
                        Button(
                            onClick = onReject,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            cornerRadius = 16.dp,
                            colors = ButtonDefaults.buttonColors(color = c.surface,
                                contentColor = c.subText)
                        ) {
                            Text(config.buttons.reject.text, fontSize = 14.sp,
                                fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    if (config.buttons.accept.enabled) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            cornerRadius = 17.dp,
                            colors = ButtonDefaults.buttonColors(color = accent,
                                contentColor = Color.White)
                        ) {
                            Text(config.buttons.accept.text, fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhaleLogo(modifier: Modifier = Modifier, accent: Color) {
    Canvas(modifier = modifier) {
        val unit = size.minDimension / 100f
        val whale = Path().apply {
            moveTo(76f * unit, 24f * unit)
            cubicTo(83f * unit, 18f * unit, 83f * unit, 15f * unit, 82f * unit, 9f * unit)
            cubicTo(89f * unit, 11f * unit, 93f * unit, 16f * unit, 94f * unit, 22f * unit)
            cubicTo(98f * unit, 17f * unit, 99f * unit, 14f * unit, 99f * unit, 13f * unit)
            cubicTo(101f * unit, 30f * unit, 91f * unit, 38f * unit, 83f * unit, 38f * unit)
            cubicTo(86f * unit, 52f * unit, 80f * unit, 68f * unit, 66f * unit, 77f * unit)
            cubicTo(50f * unit, 88f * unit, 32f * unit, 81f * unit, 19f * unit, 68f * unit)
            cubicTo(8f * unit, 57f * unit, 7f * unit, 40f * unit, 15f * unit, 28f * unit)
            cubicTo(26f * unit, 12f * unit, 53f * unit, 13f * unit, 68f * unit, 23f * unit)
            cubicTo(72f * unit, 25f * unit, 74f * unit, 25f * unit, 76f * unit, 24f * unit)
            close()
        }
        drawPath(
            path = whale,
            brush = Brush.linearGradient(
                listOf(accent.copy(alpha = 0.64f), accent),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
        val belly = Path().apply {
            moveTo(20f * unit, 43f * unit)
            cubicTo(24f * unit, 37f * unit, 35f * unit, 38f * unit, 42f * unit, 43f * unit)
            cubicTo(49f * unit, 49f * unit, 52f * unit, 62f * unit, 60f * unit, 68f * unit)
            cubicTo(47f * unit, 77f * unit, 32f * unit, 70f * unit, 24f * unit, 59f * unit)
            cubicTo(21f * unit, 54f * unit, 20f * unit, 48f * unit, 20f * unit, 43f * unit)
            close()
        }
        drawPath(belly, Color.White)
        drawCircle(
            color = Color.White,
            radius = 2.5f * unit,
            center = Offset(70f * unit, 43f * unit)
        )
        drawCircle(
            color = accent,
            radius = 1f * unit,
            center = Offset(70.6f * unit, 42.8f * unit)
        )
    }
}
