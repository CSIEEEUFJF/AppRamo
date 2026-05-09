# Módulo: Plataformas mobile

## Objetivo

Manter duas implementações nativas do AppRamo com o mesmo comportamento funcional:

- Android em Kotlin e Jetpack Compose.
- iOS em SwiftUI.

## Android

Arquivos principais:

- [MainActivity.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/MainActivity.kt)
- [NavGraph.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt)
- [build.gradle.kts](../../apps/android/app/build.gradle.kts)

Responsabilidades:

- Inicializar a aplicação Compose.
- Controlar navegação entre login, cadastro, home, perfil, tarefas, calendário, membros e controle da sala.
- Consultar Firebase diretamente nos fluxos atuais.
- Consultar o relay da sala via Ktor.

Dependências principais:

- Android Gradle Plugin.
- Kotlin.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- Firebase Android SDK.
- Ktor Client.
- Coil.

## iOS

Arquivos principais:

- [RamoApp.swift](../../apps/ios/AppRamoIEEE/RamoApp.swift)
- [AppViewModel.swift](../../apps/ios/AppRamoIEEE/AppViewModel.swift)
- [Models.swift](../../apps/ios/AppRamoIEEE/Models.swift)

Responsabilidades:

- Inicializar Firebase.
- Manter o estado global do usuário autenticado.
- Escutar eventos e tarefas em tempo real.
- Controlar navegação entre telas SwiftUI.
- Consultar o relay da sala via `URLSession`.

Dependências principais:

- Firebase iOS SDK.
- SDWebImageSwiftUI.
- SDWebImageWebPCoder.
- EventKit para integração com calendário.

## Pontos sensíveis

- O Android usa `profile_pictures/{uid}` para fotos; o iOS usa `profile_images/{uid}.jpg`.
- O Android tem duas implementações de login/cadastro; a navegação usa `pages/LoginPage.kt` e `ui/screens/RegistrationPage.kt`.
- O iOS precisa ser validado em Xcode após cada mudança de pacote ou configuração.
- As duas plataformas devem manter os mesmos nomes de campos no Firestore.

## Validação mínima

- Android: `.\gradlew.bat :app:assembleDebug`.
- Android: `.\gradlew.bat :app:testDebugUnitTest`.
- iOS: build do target `AppRamoIEEE` no Xcode.
- Teste manual dos fluxos principais nas duas plataformas.

