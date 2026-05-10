import Foundation

enum AccessPolicy {
    static let globalChapter = "Todos"
    static let unassignedChapter = "Sem capítulo"

    private static let privilegedRoles: Set<String> = [
        "admin",
        "administrador",
        "diretoria",
        "presidente",
        "vice presidente",
        "vice-presidente",
        "tesoureiro",
        "webmaster"
    ]

    static func visibleChapters(_ chapterRoles: [String: String]) -> [String] {
        var chapters = chapterRoles.keys.filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
        chapters.append(globalChapter)
        var unique: [String] = []
        for chapter in chapters where !unique.contains(chapter) {
            unique.append(chapter)
        }
        return Array(unique.prefix(10))
    }

    static func canManageContent(_ chapterRoles: [String: String]) -> Bool {
        chapterRoles.contains { chapter, role in
            chapter.caseInsensitiveCompare("Diretoria") == .orderedSame || isPrivilegedRole(role)
        }
    }

    static func canControlRoom(_ chapterRoles: [String: String]) -> Bool {
        canManageContent(chapterRoles)
    }

    static func publicChapterLabels(_ chapterRoles: [String: String]) -> [String] {
        let chapters = chapterRoles.keys
            .filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
            .filter { $0 != globalChapter }
            .sorted()
        return chapters.isEmpty ? [unassignedChapter] : chapters
    }

    private static func isPrivilegedRole(_ role: String) -> Bool {
        privilegedRoles.contains(role.trimmingCharacters(in: .whitespacesAndNewlines).lowercased())
    }
}
