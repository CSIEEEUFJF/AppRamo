import SwiftUI
import FirebaseFirestore
import FirebaseStorage
import SDWebImageSwiftUI

private struct MemberGroup: Identifiable {
    let chapter: String
    let members: [UserProfile]

    var id: String { chapter }
}

struct MembersView: View {
    @State private var users: [UserProfile] = []
    @State private var selectedUser: UserProfile?
    @State private var listener: ListenerRegistration?
    
    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]

    private var groupedUsers: [MemberGroup] {
        var groups: [String: [UserProfile]] = [:]
        for user in users {
            for chapter in AccessPolicy.publicChapterLabels(user.chapterRoles) {
                groups[chapter, default: []].append(user)
            }
        }
        return groups.keys.sorted().map { chapter in
            MemberGroup(chapter: chapter, members: groups[chapter, default: []].sorted { $0.name < $1.name })
        }
    }
    
    var body: some View {
        ScrollView {
            if users.isEmpty {
                VStack {
                    ProgressView()
                    Text("Carregando...")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 50)
            } else {
                LazyVStack(alignment: .leading, spacing: 24) {
                    ForEach(groupedUsers) { group in
                        Text(group.chapter)
                            .font(.headline)
                            .padding(.horizontal)

                        LazyVGrid(columns: columns, spacing: 20) {
                            ForEach(group.members) { user in
                                VStack {
                                    ProfileImageView(urlString: user.profilePictureUrl)
                                        .frame(width: 80, height: 80)
                                        .onTapGesture {
                                            selectedUser = user
                                        }

                                    Text(user.name)
                                        .font(.caption)
                                        .bold()
                                        .lineLimit(1)
                                        .multilineTextAlignment(.center)

                                    if let role = user.chapterRoles[group.chapter] {
                                        Text(role)
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                            .lineLimit(1)
                                            .minimumScaleFactor(0.8)
                                    } else {
                                        Text("Membro")
                                            .font(.caption2)
                                            .foregroundColor(.gray.opacity(0.5))
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("Membros")
        .onAppear {
            fetchUsers()
        }
        .onDisappear {
            listener?.remove()
            listener = nil
        }
        .sheet(item: $selectedUser) { user in
            MemberDetailPopup(user: user)
        }
    }
    
    func fetchUsers() {
        listener?.remove()
        listener = Firestore.firestore().collection("publicProfiles").addSnapshotListener { snapshot, _ in
            guard let docs = snapshot?.documents else { return }
            DispatchQueue.main.async {
                self.users = docs.compactMap { try? $0.data(as: UserProfile.self) }
            }
        }
    }
}

// MARK: - COMPONENTE DE IMAGEM
struct ProfileImageView: View {
    let urlString: String?
    @State private var validURL: URL?
    
    var body: some View {
        Group {
            if let url = validURL {
                WebImage(url: url) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    ProgressView()
                }
                .indicator(.activity)
                .transition(.fade)
                .clipShape(Circle())
                .shadow(radius: 2)
            } else {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .foregroundColor(.gray.opacity(0.3))
                    .clipShape(Circle())
                    .onAppear {
                        validateAndLoad()
                    }
            }
        }
    }
    
    private func validateAndLoad() {
        guard let urlString = urlString, let initialURL = URL(string: urlString) else { return }
        
        if urlString.contains("firebasestorage") || urlString.contains("gs://") {
            let storage = Storage.storage()
            let ref: StorageReference
            
            if urlString.hasPrefix("gs://") {
                ref = storage.reference(forURL: urlString)
            } else {
                ref = storage.reference(forURL: urlString)
            }
            
            ref.downloadURL { url, error in
                if let validUrl = url {
                    self.validURL = validUrl
                } else {
                    self.validURL = initialURL
                }
            }
        } else {
            self.validURL = initialURL
        }
    }
}

// MARK: - POPUP DETALHADO
struct MemberDetailPopup: View {
    let user: UserProfile
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    ProfileImageView(urlString: user.profilePictureUrl)
                        .frame(width: 120, height: 120)
                        .shadow(radius: 5)
                        .padding(.top, 20)
                    
                    VStack(spacing: 5) {
                        Text(user.name)
                            .font(.title2)
                            .bold()
                            .multilineTextAlignment(.center)
                        
                        Text("Perfil público")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                    }
                    
                    Divider().padding(.horizontal)
                    
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Cargos").font(.headline)
                        
                        // Correção: Verificação direta se não está vazio
                        if !user.chapterRoles.isEmpty {
                            ForEach(user.chapterRoles.keys.sorted(), id: \.self) { chapter in
                                HStack {
                                    Text(chapter)
                                        .bold()
                                        .foregroundColor(.blue)
                                        .frame(width: 60, alignment: .leading)
                                    Text(user.chapterRoles[chapter] ?? "")
                                }
                                .padding(8)
                                .background(Color(.systemGray6))
                                .cornerRadius(8)
                            }
                        } else {
                            Text("Sem cargos.").italic().foregroundColor(.secondary)
                        }
                    }
                    .padding(.horizontal)
                    
                }
            }
            .navigationTitle("Detalhes")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Fechar") { dismiss() }
                }
            }
        }
    }
}
