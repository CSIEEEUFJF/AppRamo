//
//  EditProfileView.swift
//  AppRamoIEEE
//
//  Created by Rafael Lago on 03/12/25.
//


import SwiftUI
import PhotosUI // Necessário para escolher fotos da galeria

// Struct auxiliar para editar o dicionário em uma lista
struct RoleEntry: Identifiable {
    let id = UUID()
    var chapter: String
    var role: String
}

struct EditProfileView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @Environment(\.dismiss) var dismiss
    
    // Dados Pessoais
    @State private var name: String
    @State private var phoneNumber: String
    @State private var profilePictureUrl: String
    
    // Dados de Cargos (Convertido de Dictionary para Array para editar)
    @State private var roleEntries: [RoleEntry] = []
    
    // Controle de Imagem
    @State private var selectedItem: PhotosPickerItem? = nil
    @State private var selectedImageData: Data? = nil
    
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    // Opções de Capítulos
    let availableChapters = ["RAS", "IAS", "PES", "WIE", "SIGHT", "EdSoc", "ComSoc", "APS", "MTT-S", "CS", "Diretoria"]
    
    init(currentUser: UserProfile) {
        _name = State(initialValue: currentUser.name)
        _phoneNumber = State(initialValue: currentUser.phoneNumber)
        _profilePictureUrl = State(initialValue: currentUser.profilePictureUrl ?? "")
        
        // Converte o dicionário [String: String] para nosso array editável
        let entries = (currentUser.chapterRoles ?? [:]).map { RoleEntry(chapter: $0.key, role: $0.value) }
        _roleEntries = State(initialValue: entries)
    }
    
    var body: some View {
        NavigationView {
            Form {
                // SEÇÃO 1: FOTO DE PERFIL
                Section(header: Text("Foto de Perfil")) {
                    HStack {
                        Spacer()
                        VStack {
                            // Mostra a imagem selecionada (nova) ou a atual (URL)
                            if let data = selectedImageData, let uiImage = UIImage(data: data) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 100, height: 100)
                                    .clipShape(Circle())
                            } else if let url = URL(string: profilePictureUrl), !profilePictureUrl.isEmpty {
                                AsyncImage(url: url) { image in
                                    image.resizable().scaledToFill()
                                } placeholder: {
                                    ProgressView()
                                }
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                            } else {
                                Image(systemName: "person.circle.fill")
                                    .resizable()
                                    .foregroundColor(.gray)
                                    .frame(width: 100, height: 100)
                            }
                            
                            // Botão nativo para escolher foto
                            PhotosPicker(
                                selection: $selectedItem,
                                matching: .images,
                                photoLibrary: .shared()
                            ) {
                                Text("Alterar Foto")
                                    .font(.footnote)
                                    .bold()
                                    .padding(.top, 4)
                            }
                            .onChange(of: selectedItem) { newItem in
                                Task {
                                    // Converte a seleção para Data
                                    if let data = try? await newItem?.loadTransferable(type: Data.self) {
                                        selectedImageData = data
                                    }
                                }
                            }
                        }
                        Spacer()
                    }
                    .padding(.vertical)
                }
                
                // SEÇÃO 2: DADOS PESSOAIS
                Section(header: Text("Informações Pessoais")) {
                    TextField("Nome Completo", text: $name)
                    TextField("Telefone", text: $phoneNumber)
                        .keyboardType(.phonePad)
                }
                
                // SEÇÃO 3: CARGOS E CAPÍTULOS
                Section(header: Text("Cargos e Capítulos")) {
                    ForEach($roleEntries) { $entry in
                        HStack {
                            // Picker para o Capítulo
                            Picker("Capítulo", selection: $entry.chapter) {
                                ForEach(availableChapters, id: \.self) { chapter in
                                    Text(chapter).tag(chapter)
                                }
                            }
                            .labelsHidden()
                            .frame(width: 100)
                            
                            Divider()
                            
                            // TextField para o Cargo
                            TextField("Cargo (ex: Presidente)", text: $entry.role)
                        }
                    }
                    .onDelete(perform: deleteRole)
                    
                    Button(action: addRole) {
                        Label("Adicionar Cargo", systemImage: "plus.circle.fill")
                            .foregroundColor(.blue)
                    }
                }
                
                // SEÇÃO 4: SALVAR
                Section {
                    if isLoading {
                        HStack { Spacer(); ProgressView("Salvando..."); Spacer() }
                    } else {
                        Button("Salvar Alterações") {
                            saveProfile()
                        }
                        .disabled(name.isEmpty)
                        .frame(maxWidth: .infinity, alignment: .center)
                    }
                    
                    if let error = errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("Editar Perfil")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
            }
        }
    }
    
    // Lógica para adicionar uma linha vazia na lista de cargos
    func addRole() {
        roleEntries.append(RoleEntry(chapter: availableChapters.first ?? "RAS", role: ""))
    }
    
    // Lógica para remover cargo
    func deleteRole(at offsets: IndexSet) {
        roleEntries.remove(atOffsets: offsets)
    }
    
    // Lógica de Salvamento
    func saveProfile() {
        isLoading = true
        errorMessage = nil
        
        // 1. Se tiver nova foto, faz upload primeiro
        if let imageData = selectedImageData {
            viewModel.uploadProfileImage(data: imageData) { newUrl in
                if let url = newUrl {
                    self.profilePictureUrl = url // Atualiza URL com a nova do Firebase
                    self.finalizeSave()
                } else {
                    self.isLoading = false
                    self.errorMessage = "Falha ao enviar imagem."
                }
            }
        } else {
            // Se não mudou a foto, salva direto
            finalizeSave()
        }
    }
    
    func finalizeSave() {
        // Reconstrói o dicionário [String: String] a partir da lista
        var newRoles: [String: String] = [:]
        for entry in roleEntries {
            if !entry.chapter.isEmpty && !entry.role.isEmpty {
                newRoles[entry.chapter] = entry.role
            }
        }
        
        viewModel.updateUserProfile(
            name: name,
            phoneNumber: phoneNumber,
            profilePictureUrl: profilePictureUrl,
            chapterRoles: newRoles
        ) { success in
            isLoading = false
            if success {
                dismiss()
            } else {
                errorMessage = "Erro ao salvar perfil."
            }
        }
    }
}
