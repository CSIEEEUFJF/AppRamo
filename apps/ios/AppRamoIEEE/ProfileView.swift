//
//  ProfileView.swift
//  AppRamoIEEE
//
//  Created by Rafael Lago on 03/12/25.
//


import SwiftUI
import SDWebImageSwiftUI
import FirebaseAuth

struct ProfileView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var showEditProfile = false
    
    var body: some View {
        List {
            // CABEÇALHO DO PERFIL
            Section {
                HStack {
                    Spacer()
                    VStack(spacing: 12) {
                        // Foto de Perfil
                        if let urlString = viewModel.currentUser?.profilePictureUrl, let url = URL(string: urlString) {
                            WebImage(url: url) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .resizable().foregroundColor(.gray)
                            }
                            .indicator(.activity)
                            .frame(width: 100, height: 100)
                            .clipShape(Circle())
                            .shadow(radius: 3)
                        } else {
                            Image(systemName: "person.circle.fill")
                                .resizable()
                                .foregroundColor(.gray)
                                .frame(width: 100, height: 100)
                        }
                        
                        // Nome e Email
                        Text(viewModel.currentUser?.name ?? "Carregando...")
                            .font(.title2)
                            .bold()
                        
                        Text(viewModel.currentUser?.email ?? "")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        // Telefone
                        if let phone = viewModel.currentUser?.phoneNumber, !phone.isEmpty {
                            Text(phone)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer()
                }
                .padding(.vertical, 10)
            }
            
            // CARGOS
            Section(header: Text("Cargos e Capítulos")) {
                if let roles = viewModel.currentUser?.chapterRoles, !roles.isEmpty {
                    ForEach(Array(roles.keys.sorted()), id: \.self) { key in
                        HStack {
                            Text(key)
                                .bold()
                            Spacer()
                            Text(roles[key] ?? "")
                                .foregroundColor(.gray)
                        }
                    }
                } else {
                    Text("Nenhum cargo definido")
                        .italic()
                        .foregroundColor(.gray)
                }
            }
            
            // LOGOUT
            Section {
                Button("Sair") {
                    viewModel.signOut()
                }
                .foregroundColor(.red)
                .frame(maxWidth: .infinity, alignment: .center)
            }
        }
        .navigationTitle("Perfil")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("Editar") {
                    showEditProfile = true
                }
            }
        }
        .sheet(isPresented: $showEditProfile) {
            if let user = viewModel.currentUser {
                EditProfileView(currentUser: user)
            }
        }
    }
}
