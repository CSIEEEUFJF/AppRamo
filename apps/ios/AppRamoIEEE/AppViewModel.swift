import SwiftUI
import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage

@MainActor
class AppViewModel: ObservableObject {
    @Published var currentUser: UserProfile?
    @Published var events: [ChapterEvent] = []
    @Published var tasks: [ChapterTask] = []
    @Published var isAuthenticated = false
    
    private var db = Firestore.firestore()
    private var eventsListener: ListenerRegistration?
    private var tasksListener: ListenerRegistration?
    
    init() {
        if Auth.auth().currentUser != nil {
            self.isAuthenticated = true
            fetchUserData()
        }
    }
    
    func fetchUserData() {
        guard let uid = Auth.auth().currentUser?.uid else { return }
        
        db.collection("users").document(uid).getDocument { snapshot, error in
            if let error = error { print("Erro user: \(error)"); return }
            
            if let snapshot = snapshot, snapshot.exists {
                do {
                    self.currentUser = try snapshot.data(as: UserProfile.self)
                    self.fetchEventsAndTasks()
                } catch {
                    print("Erro decoding user: \(error)")
                    // Tenta continuar mesmo se falhar (ex: usuário antigo sem campos novos)
                    self.fetchEventsAndTasks()
                }
            }
        }
    }
    
    // --- ATUALIZAÇÃO DE PERFIL ---
    func updateUserProfile(name: String, phoneNumber: String, profilePictureUrl: String?, requestedChapterRoles: [String: String], completion: @escaping (Bool) -> Void) {
        guard let uid = currentUser?.id else {
            completion(false)
            return
        }
        
        let updates: [String: Any] = [
            "name": name,
            "phoneNumber": phoneNumber,
            "profilePictureUrl": profilePictureUrl ?? "",
            "requestedChapterRoles": requestedChapterRoles
        ]
        
        db.collection("users").document(uid).setData(updates, merge: true) { error in
            if let error = error {
                print("Erro ao atualizar: \(error.localizedDescription)")
                completion(false)
            } else {
                let approvedRoles = self.currentUser?.chapterRoles ?? [:]
                var publicProfile: [String: Any] = [
                    "name": name,
                    "chapterRoles": approvedRoles
                ]
                if let profilePictureUrl, !profilePictureUrl.isEmpty {
                    publicProfile["profilePictureUrl"] = profilePictureUrl
                }
                self.db.collection("publicProfiles").document(uid).setData(publicProfile) { publicError in
                    if let publicError = publicError {
                        print("Erro ao atualizar perfil público: \(publicError.localizedDescription)")
                    }
                }

                self.currentUser?.name = name
                self.currentUser?.phoneNumber = phoneNumber
                self.currentUser?.profilePictureUrl = profilePictureUrl
                self.currentUser?.requestedChapterRoles = requestedChapterRoles
                self.fetchEventsAndTasks()
                completion(true)
            }
        }
    }
    
    // --- UPLOAD DE IMAGEM ---
    func uploadProfileImage(data: Data, completion: @escaping (String?) -> Void) {
        guard let uid = Auth.auth().currentUser?.uid else {
            completion(nil)
            return
        }
        
        let storageRef = Storage.storage().reference().child("profile_images/\(uid).jpg")
        
        // Metadados para indicar que é uma imagem
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        
        storageRef.putData(data, metadata: metadata) { _, error in
            if let error = error {
                print("Erro no upload: \(error.localizedDescription)")
                completion(nil)
                return
            }
            
            // Pega a URL pública
            storageRef.downloadURL { url, error in
                if let error = error {
                    print("Erro ao pegar URL: \(error.localizedDescription)")
                    completion(nil)
                    return
                }
                completion(url?.absoluteString)
            }
        }
    }
    
    func fetchEventsAndTasks() {
        let userRoles = currentUser?.chapterRoles ?? [:]
        
        eventsListener?.remove()
        tasksListener?.remove()
        
        let chapters = AccessPolicy.visibleChapters(userRoles)
        
        // Buscar Eventos
        eventsListener = db.collection("events")
            .whereField("chapter", in: chapters)
            .order(by: "startTime", descending: false)
            .addSnapshotListener { snapshot, _ in
                guard let documents = snapshot?.documents else { return }
                DispatchQueue.main.async {
                    self.events = documents.compactMap { try? $0.data(as: ChapterEvent.self) }
                }
            }
            
        // Buscar Tarefas
        tasksListener = db.collection("tasks")
            .whereField("chapter", in: chapters)
            .order(by: "completed")
            .order(by: "title")
            .addSnapshotListener { snapshot, _ in
                guard let documents = snapshot?.documents else { return }
                DispatchQueue.main.async {
                    self.tasks = documents.compactMap { try? $0.data(as: ChapterTask.self) }
                }
            }
    }
    
    func signOut() {
        try? Auth.auth().signOut()
        eventsListener?.remove()
        tasksListener?.remove()
        self.isAuthenticated = false
        self.currentUser = nil
        self.events = []
        self.tasks = []
    }
}
