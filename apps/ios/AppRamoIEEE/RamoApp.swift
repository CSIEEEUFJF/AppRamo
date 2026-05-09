import SwiftUI
import FirebaseCore
import SDWebImage
import SDWebImageWebPCoder

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        // 1. Configurar Firebase
        FirebaseApp.configure()
        
        // 2. Adicionar suporte a imagens WebP (Crucial para imagens vindas do Android/Web)
        let WebPCoder = SDImageWebPCoder.shared
        SDImageCodersManager.shared.addCoder(WebPCoder)
        
        return true
    }
}

@main
struct RamoIEEEApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject var viewModel = AppViewModel()

    var body: some Scene {
        WindowGroup {
            NavigationStack {
                if viewModel.isAuthenticated {
                    MainView()
                } else {
                    LoginView()
                }
            }
            .environmentObject(viewModel)
        }
    }
}
