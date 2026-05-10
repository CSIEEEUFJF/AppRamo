# Módulo: Autenticação e perfil

## Objetivo

Permitir que membros do Ramo criem conta, façam login, mantenham seus dados pessoais e acessem as funcionalidades vinculadas aos seus capítulos.

## Arquivos principais

Android:

- [NavGraph.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt)
- [LoginPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/LoginPage.kt)
- [RegistrationPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/ui/screens/RegistrationPage.kt)
- [ProfilePage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/ProfilePage.kt)

iOS:

- [LoginView.swift](../../apps/ios/AppRamoIEEE/LoginView.swift)
- [RegistrationView.swift](../../apps/ios/AppRamoIEEE/RegistrationView.swift)
- [ProfileView.swift](../../apps/ios/AppRamoIEEE/ProfileView.swift)
- [EditProfileView.swift](../../apps/ios/AppRamoIEEE/EditProfileView.swift)
- [AppViewModel.swift](../../apps/ios/AppRamoIEEE/AppViewModel.swift)

## Dados

Coleção: `users/{uid}`

Campos mínimos:

```json
{
  "name": "Nome do membro",
  "email": "membro@exemplo.com",
  "birthDate": "Timestamp",
  "phoneNumber": "(32) 00000-0000",
  "profilePictureUrl": "https://...",
  "chapterRoles": {
    "RAS": "Membro",
    "Diretoria": "Presidente"
  },
  "requestedChapterRoles": {
    "RAS": "Membro"
  }
}
```

## Fluxo de login

1. Usuário informa e-mail e senha.
2. App chama Firebase Authentication.
3. Em caso de sucesso, app carrega `users/{uid}`.
4. App guarda nome, e-mail, foto e `chapterRoles` aprovados.
5. App libera navegação para a tela inicial.

## Fluxo de cadastro

1. Usuário informa dados pessoais.
2. App cria conta no Firebase Authentication.
3. App salva dados complementares no Firestore com `requestedChapterRoles`.
4. App faz upload de foto de perfil quando selecionada.
5. App volta para o fluxo autenticado.

## Cargos e capítulos

Capítulos usados como base:

- `RAS`
- `IAS`
- `PES`
- `WIE`
- `EdSoc`
- `Diretoria`
- `SIGHT`

Cargos mínimos:

- Capítulos técnicos: `Membro`, `Presidente`.
- Diretoria: `Presidente`, `Vice Presidente`, `Tesoureiro`, `Marketing`, `Webmaster`.

## Pontos sensíveis

- O e-mail vem do Firebase Authentication e deve continuar consistente com o Firestore.
- As regras do Firestore devem impedir edição indevida de perfis de terceiros.
- O usuário pode alterar `requestedChapterRoles`, mas não pode promover a si mesmo alterando `chapterRoles`.
- O fluxo de edição não deve recriar usuário no Authentication.
- O caminho de Storage para fotos precisa ser padronizado entre Android e iOS.

## Validação mínima

- Criar conta nova.
- Fazer login com conta existente.
- Editar nome, telefone, solicitação de cargos e foto.
- Confirmar que `requestedChapterRoles` foi salvo como mapa e que `chapterRoles` não foi alterado pelo cliente.
- Sair e entrar novamente mantendo os dados.
