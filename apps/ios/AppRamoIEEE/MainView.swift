import SwiftUI
import SDWebImageSwiftUI // Importante para carregar imagem de perfil URL

struct MainView: View {
    @EnvironmentObject var viewModel: AppViewModel
    
    var body: some View {
        VStack {
            // Header Perfil
            NavigationLink(destination: ProfileView()) {
                HStack {
                    if let url = viewModel.currentUser?.profilePictureUrl, let imgUrl = URL(string: url) {
                        WebImage(url: imgUrl)
                            .resizable()
                            //.placeholder(Image(systemName: "person.circle.fill"))
                            .scaledToFill()
                            .frame(width: 40, height: 40)
                            .clipShape(Circle())
                    } else {
                        Image(systemName: "person.circle.fill")
                            .resizable()
                            .frame(width: 40, height: 40)
                            .foregroundColor(.gray)
                    }
                    Text("Bem vindo, \(viewModel.currentUser?.name ?? "Usuário")")
                        .foregroundColor(.primary)
                }
            }
            .padding(.top)
            
            Spacer()
            
            Image("ieeelogo")
                .resizable()
                .scaledToFit()
                .frame(height: 80)
            
            Spacer()
            
            VStack(spacing: 16) {
                NavigationLink("Tarefas do Capítulo", destination: TasksView())
                    .buttonStyle(MainButtonStyle())
                
                NavigationLink("Calendário do Capítulo", destination: CalendarView())
                    .buttonStyle(MainButtonStyle())
                
                NavigationLink("Controle da Sala", destination: DoorControlView())
                    .buttonStyle(MainButtonStyle())
                
                NavigationLink("Membros do Ramo", destination: MembersView())
                    .buttonStyle(MainButtonStyle())
            }
            
            Spacer()
            
            HStack(spacing: 16) {
                Image("rasieee").resizable().scaledToFit().frame(width: 64, height: 64)
                Image("cslogo").resizable().scaledToFit().frame(width: 64, height: 64)
            }
            .padding(.bottom)
        }
        .padding()
        .navigationBarHidden(true)
    }
}

struct MainButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.blue.opacity(configuration.isPressed ? 0.7 : 1.0))
            .foregroundColor(.white)
            .cornerRadius(8)
    }
}
