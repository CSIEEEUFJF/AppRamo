import Foundation
import FirebaseFirestore

struct UserProfile: Identifiable, Codable {
    @DocumentID var id: String?
    var name: String
    var phoneNumber: String
    var profilePictureUrl: String?
    var email: String?
    var chapterRoles: [String: String] = [:]
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
struct DeviceStatus: Codable {
    let device_id: String?
    let door: Int?
    let light: Int?
    let last_seen: Int64?
}

struct CommandPayload: Codable {
    let device_id: String
    let command: ActionCommand
}

struct ActionCommand: Codable {
    let action: String
}
