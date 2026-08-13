// Forked from oleksandrbalan/pagecurl v1.5.1 (Apache-2.0) and converted to Compose Multiplatform.
package com.zeyadgasser.pagecurl.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

internal fun Rect.multiply(size: IntSize): Rect =
    Rect(
        topLeft = Offset(size.width * left, size.height * top),
        bottomRight = Offset(size.width * right, size.height * bottom),
    )
