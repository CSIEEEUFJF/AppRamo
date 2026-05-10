# AppRamo Android

Aplicativo Android nativo do Ramo Estudantil IEEE UFJF.

Base importada do projeto `AppRamoIEEEUFJF_Android`, com ajustes iniciais para uso dentro do monorepo e remoção de credenciais versionadas.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Firebase Auth, Firestore e Storage
- Ktor Client
- Coil

## Configuração

1. Adicione `app/google-services.json`.
2. Configure as propriedades locais da API da placa:

```properties
DOOR_API_BASE_URL=https://ramoieeeufjf.dpdns.org
DOOR_API_KEY=preencher-localmente
```

Essas propriedades podem ficar em `~/.gradle/gradle.properties`, variáveis de ambiente ou parâmetros `-P` do Gradle.

## Build

```powershell
.\gradlew.bat :app:assembleDebug
```
