//
//  RegistrationView.swift
//  AppRamoIEEE
//
//  Created by Rafael Lago on 03/12/25.
//


import SwiftUI
import FirebaseAuth
import FirebaseFirestore

struct RegistrationView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Environment(\.dismiss) var dismiss // Para fechar a tela após registrar
    
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var phoneNumber = ""
    @State private var selectedChapter = "RAS"
    @State private var chapterRole = "Membro"
    @State private var errorMessage = ""
    
    let chapters = ["RAS", "IAS", "PES", "WIE", "SIGHT", "EdSoc", "ComSoc", "APS", "MTT-S", "CS", "Diretoria"]
    
    var body: some View {
        Form {
            Section(header: Text("Dados Pessoais")) {
                TextField("Nome Completo", text: $name)
                TextField("Telefone", text: $phoneNumber)
                    .keyboardType(.phonePad)
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
            
            Button("Criar Conta") {
                registerUser()
            }
        }
        .navigationTitle("Cadastro")
    }
    
    func registerUser() {
        Auth.auth().createUser(withEmail: email, password: password) { result, error in
            if let error = error {
                errorMessage = error.localizedDescription
                return
            }
            
            guard let uid = result?.user.uid else { return }
            
            // Salvar dados extras no Firestore
            let userData: [String: Any] = [
                "name": name,
                "email": email,
                "phoneNumber": phoneNumber,
                "chapterRoles": [selectedChapter: chapterRole]
            ]
            
            Firestore.firestore().collection("users").document(uid).setData(userData) { error in
                if let error = error {
                    errorMessage = "Erro ao salvar dados: \(error.localizedDescription)"
                } else {
                    viewModel.isAuthenticated = true
                    viewModel.fetchUserData()
                    dismiss()
                }
            }
        }
    }
}
