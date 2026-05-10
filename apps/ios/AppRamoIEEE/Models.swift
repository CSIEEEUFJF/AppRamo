import Foundation
import FirebaseFirestore

struct UserProfile: Identifiable, Codable {
    @DocumentID var id: String?
    var name: String
    var phoneNumber: String?
    var profilePictureUrl: String?
    var email: String?
    var birthDate: Date?
    var chapterRoles: [String: String] = [:]
    var requestedChapterRoles: [String: String]?
}

struct ChapterEvent: Identifiable, Codable {
    @DocumentID var id: String?
    var title: String
    var description: String
    var location: String
    var startTime: Date? // IMPORTANTE: Opcional
    var endTime: Date?   // IMPORTANTE: Opcional
    var chapter: String
}

struct ChapterTask: Identifiable, Codable {
    @DocumentID var id: String?
    var title: String
    var description: String
    var chapter: String
    var completed: Bool? // Opcional
}

// IoT
struct DoorAPIResponse: Codable {
    let ok: Bool
    let message: String?
    let error: String?
}

struct MeetingSchedulePayload: Codable {
    let delay_seconds: Int64
    let profile_indices: [Int]
    let recurrence: String
    let weekdays: [Int]?
}

struct MeetingCancelPayload: Codable {
    let id: Int64
}

struct MeetingScheduleResponse: Codable {
    let ok: Bool
    let error: String?
    let id: Int64?
    let pending_count: Int?
    let active: Bool?
    let time_synced: Bool?
    let now_unix: Int64?
    let start_unix: Int64?
    let delay_seconds: Int64?
    let profile_count: Int?
    let recurrence: String?
    let weekdays_mask: Int?
}

struct MeetingCancelResponse: Codable {
    let ok: Bool
    let error: String?
    let canceled_count: Int?
    let pending_count: Int?
    let active: Bool?
}

struct MeetingStatusResponse: Codable {
    let ok: Bool
    let error: String?
    let active: Bool?
    let pending_count: Int?
    let time_synced: Bool?
    let now_unix: Int64?
    let active_selected_profiles: Int?
    let active_allowed_cards: Int?
    let last_id: Int64?
    let last_start_unix: Int64?
    let last_selected_profiles: Int?
    let last_allowed_cards: Int?
    let last_status: String?
    let schedules: [MeetingScheduleItem]?
}

struct MeetingScheduleItem: Identifiable, Codable {
    let id: Int64
    let start_unix: Int64
    let profile_count: Int
    let recurrence: String
    let weekdays_mask: Int
}

struct DoorAccessProfile: Identifiable, Codable {
    @DocumentID var id: String?
    var firebaseUid: String?
    var name: String?
    var chapter: String?
    var role: String?
    var doorProfileIndex: Int?
    var cardCount: Int?

    var stableId: String {
        id ?? firebaseUid ?? "\(doorProfileIndex ?? -1):\(name ?? "")"
    }

    var displayName: String {
        let trimmed = (name ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? "Perfil \(doorProfileIndex ?? -1)" : trimmed
    }

    var chapterRoleLabel: String {
        let values = [chapter, role]
            .compactMap { $0?.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
        return values.isEmpty ? "Sem capítulo" : values.joined(separator: " · ")
    }
}
