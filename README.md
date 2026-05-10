# AppRamo

Repositório inicial do aplicativo do Ramo Estudantil IEEE UFJF, com bases separadas para Android e iOS.

Este repositório foi estruturado a partir dos projetos de referência construídos no final de 2025.

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
tools/
  sync_door_firebase_users.py
```

## Escopo mínimo

O escopo mínimo do produto é o conjunto de funcionalidades já implementadas no app Android de referência:

- Autenticação e cadastro de membros via Firebase.
- Perfil do usuário com foto, dados pessoais, capítulos e cargos.
- Listagem de membros do Ramo agrupados por capítulo.
- Tarefas por capítulo, com criação, conclusão, detalhes e exclusão.
- Calendário por capítulo, com criação, detalhes, exclusão e integração com a agenda do dispositivo.
- Controle de sala via API HTTP da placa para abertura de porta e modo reunião.
- Vínculo administrativo entre perfis da porta e usuários Firebase para seleção de membros no modo reunião.

Veja os detalhes em [docs/requisitos.md](docs/requisitos.md).

## Documentação

A documentação foi organizada no mesmo espírito do projeto IoT do Ramo: visão operacional, módulos, fluxos, modelo de dados, configuração e checklist de validação.

- [Estado atual](docs/estado-atual.md)
- [Arquitetura](docs/arquitetura.md)
- [Requisitos](docs/requisitos.md)
- [Configuração local](docs/configuracao.md)
- [Segurança](docs/seguranca.md)
- [Roadmap](docs/roadmap.md)
- [Módulos do sistema](docs/modulos/README.md)
- [Integração porta-Firebase](docs/modulos/integracao-porta-firebase.md)
- [Checklist de validação](docs/checklist-validacao.md)

## Configuração rápida

As credenciais reais não são versionadas. Antes de executar os apps, configure os arquivos locais:

- Android: copie o `google-services.json` do Firebase para `apps/android/app/` e configure `DOOR_API_BASE_URL` em `~/.gradle/gradle.properties`, variável de ambiente ou `-P` no Gradle. `DOOR_API_KEY` é opcional e deve ser evitada em produção.
- iOS: copie o `GoogleService-Info.plist` do Firebase para `apps/ios/AppRamoIEEE/` e preencha `DoorAPIBaseURL` no `Info.plist` local ou em uma configuração de build. `DoorAPIKey` é opcional e deve ser evitada em produção.

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
