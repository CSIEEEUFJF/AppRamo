package com.ramoieeeufjf.appRamo.security

object AccessPolicy {
    const val GLOBAL_CHAPTER = "Todos"
    const val UNASSIGNED_CHAPTER = "Sem capítulo"

    private val privilegedRoles = setOf(
        "admin",
        "administrador",
        "diretoria",
        "presidente",
        "vice presidente",
        "vice-presidente",
        "tesoureiro",
        "webmaster"
    )

    fun visibleChapters(chapterRoles: Map<String, String>): List<String> {
        return (chapterRoles.keys.filter { it.isNotBlank() } + GLOBAL_CHAPTER)
            .distinct()
            .take(10)
    }

    fun canManageContent(chapterRoles: Map<String, String>): Boolean {
        return chapterRoles.any { (chapter, role) ->
            chapter.equals("Diretoria", ignoreCase = true) || isPrivilegedRole(role)
        }
    }

    fun canControlRoom(chapterRoles: Map<String, String>): Boolean {
        return canManageContent(chapterRoles)
    }

    fun publicChapterLabels(chapterRoles: Map<String, String>): List<String> {
        return chapterRoles.keys
            .filter { it.isNotBlank() && it != GLOBAL_CHAPTER }
            .ifEmpty { listOf(UNASSIGNED_CHAPTER) }
    }

    private fun isPrivilegedRole(role: String): Boolean {
        val normalized = role.trim().lowercase()
        return normalized in privilegedRoles
    }
}
