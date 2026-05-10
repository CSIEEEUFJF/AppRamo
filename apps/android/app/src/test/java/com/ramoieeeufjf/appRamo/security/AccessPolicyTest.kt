package com.ramoieeeufjf.appRamo.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPolicyTest {
    @Test
    fun alwaysIncludesGlobalChapterInVisibilityFilter() {
        assertEquals(listOf("RAS", "Todos"), AccessPolicy.visibleChapters(mapOf("RAS" to "Membro")))
        assertEquals(listOf("Todos"), AccessPolicy.visibleChapters(emptyMap()))
    }

    @Test
    fun grantsPrivilegedRoomAccessOnlyForApprovedRoles() {
        assertFalse(AccessPolicy.canControlRoom(mapOf("RAS" to "Membro")))
        assertTrue(AccessPolicy.canControlRoom(mapOf("RAS" to "Presidente")))
        assertTrue(AccessPolicy.canControlRoom(mapOf("Diretoria" to "Membro")))
    }

    @Test
    fun exposesSafeFallbackForMembersWithoutApprovedChapter() {
        assertEquals(listOf("Sem capítulo"), AccessPolicy.publicChapterLabels(emptyMap()))
    }
}
