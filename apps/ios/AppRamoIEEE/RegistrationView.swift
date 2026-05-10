//
//  RegistrationView.swift
//  AppRamoIEEE
//
//  Created by Rafael Lago on 03/12/25.
//


import SwiftUI
import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage
import PhotosUI

struct RegistrationView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Environment(\.dismiss) var dismiss // Para fechar a tela após registrar
    
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var phoneNumber = ""
    @State private var birthDate = Date()
    @State private var selectedItem: PhotosPickerItem?
    @State private var selectedImageData: Data?
    @State private var selectedChapter = "RAS"
    @State private var chapterRole = "Membro"
    @State private var errorMessage = ""
    @State private var isLoading = false
    
    let chapters = ["RAS", "IAS", "PES", "WIE", "SIGHT", "EdSoc", "ComSoc", "APS", "MTT-S", "CS", "Diretoria"]
    
    var body: some View {
        Form {
            Section(header: Text("Dados Pessoais")) {
                HStack {
                    Spacer()
                    VStack(spacing: 8) {
                        if let selectedImageData, let uiImage = UIImage(data: selectedImageData) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 96, height: 96)
                                .clipShape(Circle())
                        } else {
                            Image(systemName: "person.circle.fill")
                                .resizable()
                                .foregroundColor(.gray)
                                .frame(width: 96, height: 96)
                        }

                        PhotosPicker(selection: $selectedItem, matching: .images) {
                            Text("Selecionar foto")
                                .font(.footnote)
                        }
                    }
                    Spacer()
                }
                TextField("Nome Completo", text: $name)
                TextField("Telefone", text: $phoneNumber)
                    .keyboardType(.phonePad)
                DatePicker("Nascimento", selection: $birthDate, displayedComponents: .date)
            }
            
            Section(header: Text("Acesso")) {
                TextField("Email", text: $email)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                SecureField("Senha", text: $password)
            }
            
            Section(header: Text("Ramo IEEE")) {
                Picker("Capítulo Principal", selection: $selectedChapter) {
                    ForEach(chapters, id: \.self) { chapter in
                        Text(chapter)
                    }
                }
                TextField("Cargo (Ex: Presidente, Membro)", text: $chapterRole)
            }
            
            if !errorMessage.isEmpty {
                Text(errorMessage).foregroundColor(.red)
            }
            
            Button(isLoading ? "Criando..." : "Criar Conta") {
                registerUser()
            }
            .disabled(isLoading || name.isEmpty || email.isEmpty || password.isEmpty)
        }
        .navigationTitle("Cadastro")
        .onChange(of: selectedItem) { item in
            Task {
                selectedImageData = try? await item?.loadTransferable(type: Data.self)
            }
        }
    }
    
    func registerUser() {
        isLoading = true
        errorMessage = ""

        Auth.auth().createUser(withEmail: email, password: password) { result, error in
            if let error = error {
                errorMessage = error.localizedDescription
                isLoading = false
                return
            }
            
            guard let uid = result?.user.uid else {
                errorMessage = "Não foi possível criar o usuário."
                isLoading = false
                return
            }

            if let selectedImageData {
                uploadProfileImage(uid: uid, data: selectedImageData) { url in
                    saveUserData(uid: uid, profilePictureUrl: url)
                }
            } else {
                saveUserData(uid: uid, profilePictureUrl: nil)
            }
        }
    }

    private func uploadProfileImage(uid: String, data: Data, completion: @escaping (String?) -> Void) {
        let ref = Storage.storage().reference().child("profile_images/\(uid).jpg")
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"

        ref.putData(data, metadata: metadata) { _, error in
            if let error = error {
                errorMessage = "Erro ao enviar foto: \(error.localizedDescription)"
                completion(nil)
                return
            }

            ref.downloadURL { url, _ in
                completion(url?.absoluteString)
            }
        }
    }

    private func saveUserData(uid: String, profilePictureUrl: String?) {
        var userData: [String: Any] = [
            "name": name,
            "email": email,
            "phoneNumber": phoneNumber,
            "birthDate": birthDate,
            "chapterRoles": [:] as [String: String],
            "requestedChapterRoles": [selectedChapter: chapterRole]
        ]
        if let profilePictureUrl, !profilePictureUrl.isEmpty {
            userData["profilePictureUrl"] = profilePictureUrl
        }

        Firestore.firestore().collection("users").document(uid).setData(userData) { error in
            if let error = error {
                errorMessage = "Erro ao salvar dados: \(error.localizedDescription)"
                isLoading = false
                return
            }

            var publicProfile: [String: Any] = [
                "name": name,
                "chapterRoles": [:] as [String: String]
            ]
            if let profilePictureUrl, !profilePictureUrl.isEmpty {
                publicProfile["profilePictureUrl"] = profilePictureUrl
            }

            Firestore.firestore().collection("publicProfiles").document(uid).setData(publicProfile) { publicError in
                if let publicError = publicError {
                    errorMessage = "Erro ao salvar perfil público: \(publicError.localizedDescription)"
                    isLoading = false
                    return
                }

                viewModel.isAuthenticated = true
                viewModel.fetchUserData()
                isLoading = false
                dismiss()
            }
        }
    }
}
