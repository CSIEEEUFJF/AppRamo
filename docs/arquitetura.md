# Arquitetura

## Visão geral

O AppRamo começa como um monorepo com dois aplicativos nativos:

- `apps/android`: app Android em Kotlin, Jetpack Compose e Firebase.
- `apps/ios`: app iOS em SwiftUI e Firebase.

As duas plataformas devem compartilhar o mesmo modelo de dados no Firebase para usuários, tarefas e eventos.

A documentação detalhada por responsabilidade está em [modulos/README.md](modulos/README.md).

```mermaid
flowchart LR
  Android["Android app\nKotlin + Compose"] --> Firebase["Firebase\nAuth, Firestore, Storage"]
  IOS["iOS app\nSwiftUI"] --> Firebase
  Android --> Relay["Relay HTTP da sala\n/status e /send"]
  IOS --> Relay
  Relay --> ESP["Dispositivo IoT\nporta/luz"]
```

## Android

- Entrada: `apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/MainActivity.kt`.
- Navegação: `NavGraph.kt`.
- Telas principais:
  - `ui/screens/RegistrationPage.kt`
  - `pages/LoginPage.kt`
  - `pages/MainPage.kt`
  - `pages/ProfilePage.kt`
  - `pages/MembersPage.kt`
  - `pages/TasksPage.kt`
  - `pages/CalendarPage.kt`
  - `pages/DoorControlPage.kt`
- Dependências principais:
  - Android Gradle Plugin
  - Kotlin
  - Jetpack Compose
  - Firebase Auth, Firestore, Realtime Database e Storage
  - Ktor Client
  - Coil

## iOS

- Entrada: `apps/ios/AppRamoIEEE/RamoApp.swift`.
- Estado global: `AppViewModel.swift`.
- Modelos: `Models.swift`.
- Telas principais:
  - `LoginView.swift`
  - `RegistrationView.swift`
  - `MainView.swift`
  - `ProfileView.swift`
  - `EditProfileView.swift`
  - `MembersView.swift`
  - `TasksView.swift`
  - `CalendarView.swift`
  - `DoorControlView.swift`
- Dependências principais:
  - Firebase iOS SDK
  - SDWebImageSwiftUI
  - SDWebImageWebPCoder

## Modelo de dados

### `users/{uid}`

```json
{
  "name": "Nome do membro",
  "email": "membro@exemplo.com",
  "birthDate": "Timestamp",
  "phoneNumber": "(32) 00000-0000",
  "profilePictureUrl": "https://...",
  "chapterRoles": {
    "RAS": "Presidente",
    "IAS": "Membro"
  }
}
```

### `tasks/{taskId}`

```json
{
  "title": "Título da tarefa",
  "description": "Descrição da tarefa",
  "chapter": "RAS",
  "completed": false
}
```

### `events/{eventId}`

```json
{
  "title": "Reunião semanal",
  "description": "Pauta da reunião",
  "location": "Sala do Ramo",
  "startTime": "Timestamp",
  "endTime": "Timestamp",
  "chapter": "RAS"
}
```

## Integrações externas

### Firebase

O Firebase centraliza autenticação, persistência e armazenamento de fotos. Cada plataforma precisa dos arquivos locais de configuração:

- Android: `apps/android/app/google-services.json`.
- iOS: `apps/ios/AppRamoIEEE/GoogleService-Info.plist`.

Esses arquivos não devem ser commitados.

### Relay da sala

O controle da sala usa um relay HTTP com:

- `GET /status?device_id=esp01`
- `POST /send`
- Header `X-API-KEY`

O Android lê `DOOR_RELAY_BASE_URL`, `DOOR_RELAY_API_KEY` e `DOOR_RELAY_DEVICE_ID` de propriedades Gradle. O iOS lê `DoorRelayBaseURL`, `DoorRelayAPIKey` e `DoorRelayDeviceID` do `Info.plist`.

O módulo de controle da sala está detalhado em [modulos/controle-da-sala.md](modulos/controle-da-sala.md), incluindo o contrato esperado pelo app e observações de integração com o projeto IoT do Ramo.
