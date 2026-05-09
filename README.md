# AppRamo

Monorepo inicial do aplicativo do Ramo Estudantil IEEE UFJF, com bases separadas para Android e iOS.

Este repositório foi estruturado a partir dos projetos de referência:

- Android: [CSIEEEUFJF/AppRamoIEEEUFJF_Android](https://github.com/CSIEEEUFJF/AppRamoIEEEUFJF_Android), commit `d811cfb65efa6b36f514c1d1327c2b6c726e8ba2`
- iOS: [CSIEEEUFJF/AppRamoIEEEUFJF_iOS](https://github.com/CSIEEEUFJF/AppRamoIEEEUFJF_iOS), commit `dbb666674a292c2e1a7da0a8f19a52b284c2bd0f`

## Estrutura

```text
apps/
  android/  Projeto Android nativo em Kotlin + Jetpack Compose
  ios/      Projeto iOS nativo em SwiftUI
docs/
  requisitos.md
  arquitetura.md
  configuracao.md
  roadmap.md
```

## Escopo mínimo

O escopo mínimo do produto é o conjunto de funcionalidades já implementadas no app Android de referência:

- Autenticação e cadastro de membros via Firebase.
- Perfil do usuário com foto, dados pessoais, capítulos e cargos.
- Listagem de membros do Ramo agrupados por capítulo.
- Tarefas por capítulo, com criação, conclusão, detalhes e exclusão.
- Calendário por capítulo, com criação, detalhes, exclusão e integração com a agenda do dispositivo.
- Controle de sala via relay HTTP para status da porta/luz e envio de comandos.

Veja os detalhes em [docs/requisitos.md](docs/requisitos.md).

## Configuração rápida

As credenciais reais não são versionadas. Antes de executar os apps, configure os arquivos locais:

- Android: copie o `google-services.json` do Firebase para `apps/android/app/` e configure `DOOR_RELAY_API_KEY` em `~/.gradle/gradle.properties`, variável de ambiente ou `-P` no Gradle.
- iOS: copie o `GoogleService-Info.plist` do Firebase para `apps/ios/AppRamoIEEE/` e preencha `DoorRelayAPIKey` no `Info.plist` local ou em uma configuração de build.

Mais detalhes em [docs/configuracao.md](docs/configuracao.md).

## Comandos úteis

Android:

```powershell
cd apps/android
.\gradlew.bat :app:assembleDebug
```

iOS:

```powershell
cd apps/ios
```

Abra `AppRamoIEEE.xcodeproj` no Xcode para resolver os pacotes Swift e executar o app.
