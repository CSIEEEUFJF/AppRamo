# Estado atual do AppRamo

Data de referência: `2026-05-09`

Este arquivo resume rapidamente o estado inicial do repositório. A organização segue a mesma lógica prática usada na documentação do projeto [`IoT_Ramo_Renesas`](https://github.com/CSIEEEUFJF/IoT_Ramo_Renesas): primeiro o que funciona, depois módulos, pontos sensíveis e próximos passos.

Referência consultada do IoT: commit `f82c9726e944df9aad84c4ceee40cd992dfd44e6`.

## Base funcional atual

Hoje a base do AppRamo está organizada como um monorepo com:

- aplicativo Android nativo em Kotlin e Jetpack Compose;
- aplicativo iOS nativo em SwiftUI;
- autenticação por Firebase Authentication;
- dados de usuários, tarefas e eventos no Cloud Firestore;
- fotos de perfil no Firebase Storage;
- integração mobile com relay HTTP para porta/luz da sala;
- documentação inicial separada por módulos.

## Funcionalidades importadas das bases de referência

- Login por e-mail e senha.
- Cadastro de usuário.
- Edição/exibição de perfil.
- Associação de usuário a capítulos e cargos.
- Listagem de membros.
- Tarefas por capítulo.
- Calendário por capítulo.
- Controle de sala por status/comando HTTP.

## Características importantes

- Android é a referência mínima de escopo funcional.
- iOS já contém implementação equivalente para os principais fluxos, mas ainda precisa de validação em Xcode.
- Credenciais reais do Firebase e chave do relay não são versionadas.
- O relay da sala é documentado como contrato HTTP independente do app.
- O repositório remoto principal é `https://github.com/CSIEEEUFJF/AppRamo.git`.

## Pontos sensíveis

- Regras de segurança do Firestore e Storage ainda precisam ser definidas para produção.
- O Android possui telas duplicadas de login/cadastro em pacotes diferentes.
- Android e iOS usam caminhos diferentes para upload de foto de perfil.
- O controle da sala depende de chave de API configurada localmente.
- A integração direta com o projeto embarcado `IoT_Ramo_Renesas` ainda precisa de uma camada de compatibilidade ou relay.

## Arquivos mais importantes

- [README.md](../README.md)
- [docs/requisitos.md](requisitos.md)
- [docs/arquitetura.md](arquitetura.md)
- [docs/configuracao.md](configuracao.md)
- [docs/modulos/README.md](modulos/README.md)
- [apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt](../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt)
- [apps/ios/AppRamoIEEE/AppViewModel.swift](../apps/ios/AppRamoIEEE/AppViewModel.swift)

## Validação feita

No ambiente Windows usado para criação do repositório:

- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:testDebugUnitTest`

O iOS precisa ser validado em macOS com Xcode.
