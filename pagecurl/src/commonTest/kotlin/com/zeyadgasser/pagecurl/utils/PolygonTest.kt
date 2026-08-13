package com.zeyadgasser.pagecurl.utils

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class PolygonTest {

    private val square = Polygon(
        listOf(
            Offset(0f, 0f),
            Offset(10f, 0f),
            Offset(10f, 10f),
            Offset(0f, 10f),
        )
    )

    @Test
    fun when_translate_then_all_vertices_shift_by_offset() {
        val translated = square.translate(Offset(5f, -3f))

        assertEquals(
            listOf(Offset(5f, -3f), Offset(15f, -3f), Offset(15f, 7f), Offset(5f, 7f)),
            translated.vertices,
        )
    }

    // The shadow path is the polygon offset OUTWARD by the shadow radius — every offset vertex
    // must sit strictly outside the original bounds, symmetrically in all directions.
    @Test
    fun when_offset_outward_then_polygon_grows_symmetrically() {
        val grown = square.offset(2f)

        val xs = grown.vertices.map { it.x }
        val ys = grown.vertices.map { it.y }
        assertTrue(xs.min() < 0f && xs.max() > 10f, "expected x-range to grow, got ${xs.min()}..${xs.max()}")
        assertTrue(ys.min() < 0f && ys.max() > 10f, "expected y-range to grow, got ${ys.min()}..${ys.max()}")
        assertTrue(abs(xs.min() + (xs.max() - 10f)) < 0.01f, "expected symmetric growth")
    }

    @Test
    fun when_offset_zero_then_polygon_unchanged() {
        val same = square.offset(0f)

        same.vertices.zip(square.vertices).forEach { (actual, expected) ->
            assertTrue((actual - expected).getDistance() < 0.01f, "expected $expected, got $actual")
        }
    }

    @Test
    fun when_lineLineIntersection_crossing_then_returns_point() {
        val intersection = lineLineIntersection(
            Offset(0f, 0f), Offset(10f, 10f),
            Offset(0f, 10f), Offset(10f, 0f),
        )

        assertEquals(Offset(5f, 5f), intersection)
    }

    @Test
    fun when_lineLineIntersection_parallel_then_returns_null() {
        val intersection = lineLineIntersection(
            Offset(0f, 0f), Offset(10f, 0f),
            Offset(0f, 5f), Offset(10f, 5f),
        )

        assertEquals(null, intersection)
    }
}
