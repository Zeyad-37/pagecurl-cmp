package eu.wewox.pagecurl.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState

private val Paper = Color(0xFFF9F2E7)
private val Ink = Color(0xFF3B3228)

private data class DemoPage(
    val title: String,
    val body: String,
    val accent: Color,
)

private val pages = listOf(
    DemoPage(
        title = "PageCurl",
        body = "A Compose Multiplatform page-turn effect.\n\n" +
            "Drag from the right edge to turn this page forward, " +
            "or from the left edge to go back. You can also tap the right or left half of the page.",
        accent = Color(0xFFB3541E),
    ),
    DemoPage(
        title = "One codebase",
        body = "This exact page — and the curl you just performed — renders from the same Kotlin code " +
            "on Android and iOS.\n\nThe only platform-specific part is the page-edge shadow: " +
            "Android uses its native blur, iOS draws a Compose gradient.",
        accent = Color(0xFF1E6091),
    ),
    DemoPage(
        title = "Gestures",
        body = "Interactive drags follow your finger, flings settle naturally, " +
            "and cancelled drags snap back.\n\nTap navigation is configurable, " +
            "and everything can be driven programmatically through PageCurlState.",
        accent = Color(0xFF52796F),
    ),
    DemoPage(
        title = "Configuration",
        body = "Shadow color, alpha, radius and offset, back-page color, " +
            "drag and tap interaction zones — all configurable via PageCurlConfig.",
        accent = Color(0xFF6D466B),
    ),
    DemoPage(
        title = "The End",
        body = "Turn back to any page, or start over.\n\n" +
            "Fork: github.com/Zeyad-37/pagecurl — branch cmp.",
        accent = Color(0xFF9A031E),
    ),
)

/**
 * Multiplatform demo of the [PageCurl] composable — a small "book" with a few pages.
 * Used unchanged by both the Android and iOS sample apps.
 */
@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun DemoApp() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Ink)) {
            val state = rememberPageCurlState()

            PageCurl(
                count = pages.size,
                state = state,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                BookPage(page = pages[index], number = index + 1)
            }

            FpsOverlay(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun BookPage(page: DemoPage, number: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 40.dp),
    ) {
        Text(
            text = "Chapter $number",
            style = MaterialTheme.typography.labelLarge,
            color = page.accent,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = page.accent, thickness = 2.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink.copy(alpha = 0.85f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { dot ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (dot == number - 1) 10.dp else 6.dp)
                        .background(
                            color = if (dot == number - 1) page.accent else Ink.copy(alpha = 0.25f),
                            shape = CircleShape,
                        ),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "— $number —",
            style = MaterialTheme.typography.labelMedium,
            color = Ink.copy(alpha = 0.5f),
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
