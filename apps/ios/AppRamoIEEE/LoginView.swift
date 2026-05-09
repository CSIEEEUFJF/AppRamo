import SwiftUI
import FirebaseAuth

struct LoginView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var email = ""
    @State private var password = ""
    @State private var showRegistration = false
    @State private var errorMessage = ""
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            Image("ieeelogo") // Adicione estas imagens ao Assets.xcassets
                .resizable()
                .scaledToFit()
                .frame(height: 100)
            
            Spacer()
            
            TextField("E-mail", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .textInputAutocapitalization(.never)
                .keyboardType(.emailAddress)
            
            SecureField("Senha", text: $password)
                .textFieldStyle(RoundedBorderTextFieldStyle())
            
            if !errorMessage.isEmpty {
                Text(errorMessage).foregroundColor(.red).font(.caption)
            }
            
            Button("LOGIN") {
                login()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)
            
            Button("REGISTRAR") {
                showRegistration = true
            }
            
            Spacer()
            
            HStack(spacing: 16) {
                Image("rasieee") // Adicione ao Assets
                    .resizable().scaledToFit().frame(width: 64, height: 64)
                Image("iaslogo") // Adicione ao Assets
                    .resizable().scaledToFit().frame(width: 64, height: 64)
            }
            .padding(.bottom, 32)
        }
        .padding()
        .navigationDestination(isPresented: $showRegistration) {
            RegistrationView()
        }
    }
    
    func login() {
        Auth.auth().signIn(withEmail: email.trimmingCharacters(in: .whitespaces), password: password) { result, error in
            if let error = error {
                errorMessage = error.localizedDescription
            } else {
                viewModel.isAuthenticated = true
                viewModel.fetchUserData()
            }
        }
    }
}
