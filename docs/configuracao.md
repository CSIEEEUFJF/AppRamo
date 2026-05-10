# Configuração local

## Android

Pré-requisitos:

- Android Studio atual.
- JDK 17 para o Android Gradle Plugin.
- SDK Android com compile SDK compatível com o projeto.

Passos:

1. Copie o arquivo Firebase `google-services.json` para `apps/android/app/google-services.json`.
2. Configure as chaves da API da placa em `~/.gradle/gradle.properties`, variáveis de ambiente ou parâmetros `-P` do Gradle:

```properties
DOOR_API_BASE_URL=https://ramoieeeufjf.dpdns.org
DOOR_API_KEY=preencher-localmente
```

3. Execute:

```powershell
cd apps/android
.\gradlew.bat :app:assembleDebug
```

O plugin `com.google.gms.google-services` só é aplicado quando `apps/android/app/google-services.json` existe. Isso permite clonar e abrir o projeto sem versionar credenciais.

## iOS

Pré-requisitos:

- macOS com Xcode.
- Conta Apple configurada no Xcode para execução em dispositivo, se necessário.

Passos:

1. Copie o arquivo Firebase `GoogleService-Info.plist` para `apps/ios/AppRamoIEEE/GoogleService-Info.plist`.
2. Abra `apps/ios/AppRamoIEEE.xcodeproj` no Xcode.
3. Resolva os pacotes Swift quando o Xcode solicitar.
4. Preencha `DoorAPIBaseURL` e `DoorAPIKey` no `Info.plist` local ou configure essas chaves via build settings.

## Firebase

Serviços necessários:

- Firebase Authentication com provedor de e-mail/senha.
- Cloud Firestore.
- Firebase Storage.

Coleções mínimas:

- `users`
- `tasks`
- `events`

Índices prováveis no Firestore:

- `events`: filtro `chapter in (...)` e ordenação por `startTime` ascendente.
- `tasks`: filtro `chapter in (...)`, ordenação por `completed` e depois por `title`.

Regras de segurança devem ser revisadas antes de produção. O início do projeto pode usar regras restritas a usuários autenticados, mas permissões por capítulo e cargo devem ser implementadas antes de liberar operações administrativas.

## Observações de migração

- O Android de referência usa `profile_pictures/{uid}` no Storage; o iOS usa `profile_images/{uid}.jpg`. Recomenda-se padronizar esse caminho.
- O Android contém duas implementações de telas de login/cadastro em pacotes diferentes. Recomenda-se manter apenas as telas realmente usadas na navegação.
- O controle da sala depende de uma chave de API. Ela deve ficar fora do Git e, idealmente, ser substituída por autenticação ligada ao usuário.
