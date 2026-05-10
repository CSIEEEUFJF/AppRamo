package com.ramoieeeufjf.appRamo.pages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DoorInputParserTest {
    @Test
    fun parsesProfileIndicesWithCommonSeparators() {
        assertEquals(listOf(0, 4, 12), DoorInputParser.parseProfileIndices("0, 4;12"))
    }

    @Test
    fun rejectsEmptyProfileIndices() {
        assertThrows(IllegalArgumentException::class.java) {
            DoorInputParser.parseProfileIndices("   ")
        }
    }

    @Test
    fun parsesOptionalWeekdays() {
        assertEquals(listOf(1, 3, 5), DoorInputParser.parseWeekdays("1 3 5"))
        assertNull(DoorInputParser.parseWeekdays(""))
    }

    @Test
    fun rejectsWeekdaysOutsideFirmwareRange() {
        assertThrows(IllegalArgumentException::class.java) {
            DoorInputParser.parseWeekdays("7")
        }
    }

    @Test
    fun parsesDelayAndCancelIdBounds() {
        assertEquals(5L, DoorInputParser.parseDelayMinutes("5"))
        assertEquals(3L, DoorInputParser.parseCancelId("3"))
        assertNull(DoorInputParser.parseCancelId(""))
    }
}
