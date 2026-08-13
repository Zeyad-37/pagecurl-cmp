// Vendored from oleksandrbalan/pagecurl v1.5.1 (Apache-2.0), modified for CMP — see docs/rfc/T-037-RFC-CMP-PageCurl-Fork.md
package eu.wewox.pagecurl.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

internal fun Rect.multiply(size: IntSize): Rect =
    Rect(
        topLeft = Offset(size.width * left, size.height * top),
        bottomRight = Offset(size.width * right, size.height * bottom),
    )
