package me.rain.miuixdialog

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
private val Blue = Color(0xFF476BDA)
private val BlueBright = Color(0xFF6386FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            MiuixTheme(colors = if (dark) darkColorScheme() else lightColorScheme()) {
                DialogDemo(dark = dark)
            }
        }
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
    Palette(
        background = Color(0xFF0E1013),
        surface = Color(0xFF202226),
        mutedSurface = Color(0xFF2A2D32),
        mainText = Color(0xFFF7F8FA),
        subText = Color(0xFFADB0B8),
        line = Color(0xFF393C42)
    )
} else {
    Palette(
        background = Color(0xFFF5F6F8),
        surface = Color.White,
        mutedSurface = Color(0xFFF4F5F7),
        mainText = Color(0xFF191B20),
        subText = Color(0xFF777D87),
        line = Color(0xFFE9EBEF)
    )
}

@Composable
private fun DialogDemo(dark: Boolean) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var neverShow by remember { mutableStateOf(prefs.getBoolean(PREF_HIDE, false)) }
    var showDialog by rememberSaveable { mutableStateOf(!prefs.getBoolean(PREF_HIDE, false)) }
    var lastAction by rememberSaveable { mutableStateOf("尚未选择") }
    val c = palette(dark)

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
            color = Blue,
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
            text = "一个轻盈、克制的系统级提示体验。",
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
                            if (dark) listOf(Color(0xFF252D47), Color(0xFF171E31))
                            else listOf(Color(0xFFEDF2FF), Color(0xFFE4EAF9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .background(Blue.copy(alpha = if (dark) 0.16f else 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    WhaleLogo(Modifier.size(79.dp))
                }
                Text(
                    "DIALOG / 001",
                    color = if (dark) Color(0xFFC4CEFA) else Color(0xFF7183B6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                "软件使用声明",
                color = c.mainText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "圆角容器 · 原生按压反馈 · 弹性动效",
                color = c.subText,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                cornerRadius = 18.dp,
                colors = ButtonDefaults.buttonColorsPrimary()
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
                Text("偏好记录", color = c.mainText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Text(
                    if (neverShow) "不再自动显示" else "允许自动显示",
                    color = if (neverShow) Blue else c.subText,
                    fontSize = 12.sp
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("上次操作", color = c.subText, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text(lastAction, color = c.mainText, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    prefs.edit().remove(PREF_HIDE).apply()
                    neverShow = false
                    lastAction = "已重置"
                    Toast.makeText(context, "已清除不再显示记录", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                cornerRadius = 15.dp
            ) {
                Text("重置弹窗记录", fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
    }

    StatementDialog(
        show = showDialog,
        dark = dark,
        onDismiss = { showDialog = false },
        onLearn = {
            Toast.makeText(context, "交流学习链接尚未设置", Toast.LENGTH_SHORT).show()
            lastAction = "交流学习"
        },
        onReject = {
            lastAction = "不同意"
            showDialog = false
        },
        onAccept = {
            prefs.edit().putBoolean(PREF_HIDE, true).apply()
            neverShow = true
            lastAction = "同意并不再显示"
            showDialog = false
        }
    )
}

@Composable
private fun StatementDialog(
    show: Boolean,
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
        val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.88f,
                    animationSpec = spring(
                        dampingRatio = 0.77f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(tween(145)) + scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(145)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                        .shadow(24.dp, RoundedCornerShape(34.dp))
                        .clip(RoundedCornerShape(34.dp))
                        .background(c.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(94.dp)
                            .clip(RoundedCornerShape(29.dp))
                            .background(
                                Brush.linearGradient(
                                    if (dark) listOf(Color(0xFF2B3550), Color(0xFF1C263E))
                                    else listOf(Color(0xFFF1F5FF), Color(0xFFE7EEFF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        WhaleLogo(Modifier.size(68.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "作者：神秘人",
                        color = c.subText,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "软件使用声明",
                        color = c.mainText,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "请在继续使用前阅读以下说明",
                        color = c.subText,
                        fontSize = 12.5.sp
                    )
                    Spacer(Modifier.height(22.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(c.mutedSurface)
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(7.dp).background(BlueBright, CircleShape)
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                "重要说明",
                                fontSize = 12.sp,
                                color = c.mainText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "郑重声明：本软件来源于网络，非本人开发。依据《计算机软件保护条例》相关规定，此软件仅用于学习和研究软件的设计思想与原理，绝不可用于任何商业或非法目的。",
                            color = c.mainText.copy(alpha = if (dark) 0.84f else 0.79f),
                            fontSize = 13.5.sp,
                            lineHeight = 24.sp
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onLearn,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        cornerRadius = 16.dp
                    ) {
                        Text("交流学习", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onReject,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        cornerRadius = 16.dp,
                        colors = ButtonDefaults.buttonColors(
                            color = c.surface,
                            contentColor = c.subText
                        )
                    ) {
                        Text("不同意", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        cornerRadius = 17.dp,
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text("同意并不再显示", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhaleLogo(modifier: Modifier = Modifier) {
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
                listOf(Color(0xFF7296FF), Color(0xFF345DD5)),
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
            color = Color(0xFF345DD5),
            radius = 1f * unit,
            center = Offset(70.6f * unit, 42.8f * unit)
        )
    }
}
