import SwiftUI
import FirebaseFirestore
import FirebaseStorage
import SDWebImageSwiftUI

struct MembersView: View {
    @State private var users: [UserProfile] = []
    @State private var selectedUser: UserProfile?
    
    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
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
                LazyVGrid(columns: columns, spacing: 20) {
                    ForEach(users) { user in
                        VStack {
                            // FOTO DE PERFIL
                            ProfileImageView(urlString: user.profilePictureUrl)
                                .frame(width: 80, height: 80)
                                .onTapGesture {
                                    selectedUser = user
                                }
                            
                            // NOME
                            Text(user.name)
                                .font(.caption)
                                .bold()
                                .lineLimit(1)
                                .multilineTextAlignment(.center)
                            
                            // LEGENDA: CAPÍTULO - CARGO
                            // Correção: Acessamos diretamente pois não é opcional no seu Model
                            if let firstChapter = user.chapterRoles.keys.sorted().first,
                               let role = user.chapterRoles[firstChapter] {
                                
                                Text("\(firstChapter) - \(role)")
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
                .padding()
            }
        }
        .navigationTitle("Membros")
        .onAppear {
            fetchUsers()
        }
        .sheet(item: $selectedUser) { user in
            MemberDetailPopup(user: user)
        }
    }
    
    func fetchUsers() {
        Firestore.firestore().collection("users").getDocuments { snapshot, _ in
            guard let docs = snapshot?.documents else { return }
            self.users = docs.compactMap { try? $0.data(as: UserProfile.self) }
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
                        
                        if let email = user.email {
                            Text(email).font(.subheadline).foregroundColor(.gray)
                        }
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
                    
                    if !user.phoneNumber.isEmpty {
                        HStack {
                            Image(systemName: "phone.fill").foregroundColor(.green)
                            Text(user.phoneNumber)
                        }
                        .padding()
                        .background(Color.green.opacity(0.1))
                        .cornerRadius(10)
                    }
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
