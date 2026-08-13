package com.zeyadgasser.pagecurl.page

import androidx.compose.ui.unit.Constraints
import com.zeyadgasser.pagecurl.ExperimentalPageCurlApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalPageCurlApi::class)
class PageCurlStateTest {

    // Regression: snapTo before the PageCurl composable's first layout (max still 0) used to do
    // value.coerceIn(0, -1) and throw "Cannot coerce value to an empty range". The state-restore
    // path constructs PageCurlState(current = saved, max = 0), and DateCurl's snap effect can run
    // before the pager's first measure.
    @Test
    fun when_snapTo_before_setup_then_does_not_throw_and_floors_at_zero() = runTest {
        val state = PageCurlState(initialCurrent = 5)

        state.snapTo(3)

        assertEquals(3, state.current)
    }

    @Test
    fun when_snapTo_negative_before_setup_then_floors_at_zero() = runTest {
        val state = PageCurlState()

        state.snapTo(-2)

        assertEquals(0, state.current)
    }

    @Test
    fun when_snapTo_after_setup_then_coerces_into_page_range() = runTest {
        val state = PageCurlState()
        state.setup(count = 3, constraints = Constraints.fixed(100, 200))

        state.snapTo(7)

        assertEquals(2, state.current)
    }

    @Test
    fun when_setup_with_fewer_pages_than_current_then_clamps_current() {
        val state = PageCurlState(initialCurrent = 9)

        state.setup(count = 4, constraints = Constraints.fixed(100, 200))

        assertEquals(3, state.current)
    }

    @Test
    fun when_setup_with_zero_pages_then_current_is_zero() {
        val state = PageCurlState(initialCurrent = 2)

        state.setup(count = 0, constraints = Constraints.fixed(100, 200))

        assertEquals(0, state.current)
    }

    // next()/prev() before setup have no internal state yet — they must be safe no-ops.
    @Test
    fun when_next_before_setup_then_no_op() = runTest {
        val state = PageCurlState(initialCurrent = 1)

        state.next()

        assertEquals(1, state.current)
    }

    @Test
    fun when_prev_before_setup_then_no_op() = runTest {
        val state = PageCurlState(initialCurrent = 1)

        state.prev()

        assertEquals(1, state.current)
    }
}
