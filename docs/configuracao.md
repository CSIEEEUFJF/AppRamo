# Configuração local

## Android

Pré-requisitos:

- Android Studio atual.
- JDK 17 para o Android Gradle Plugin.
- SDK Android com compile SDK compatível com o projeto.

Passos:

1. Copie o arquivo Firebase `google-services.json` para `apps/android/app/google-services.json`.
2. Configure a URL da API da porta em `~/.gradle/gradle.properties`, variáveis de ambiente ou parâmetros `-P` do Gradle. A chave direta é opcional e deve ser evitada em produção:

```properties
DOOR_API_BASE_URL=https://ramoieeeufjf.dpdns.org
DOOR_API_KEY=
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
4. Preencha `DoorAPIBaseURL` no `Info.plist` local ou configure via build settings. `DoorAPIKey` deve ficar vazia quando a API intermediária validar Firebase ID Token.

## Firebase

Serviços necessários:

- Firebase Authentication com provedor de e-mail/senha.
- Cloud Firestore.
- Firebase Storage.

Coleções mínimas:

- `users`
- `publicProfiles`
- `doorProfiles`
- `tasks`
- `events`

Índices prováveis no Firestore:

- `events`: filtro `chapter in (...)` e ordenação por `startTime` ascendente.
- `tasks`: filtro `chapter in (...)`, ordenação por `completed` e depois por `title`.

As regras versionadas em `firestore.rules` e `storage.rules` devem ser publicadas antes de produção:

```powershell
firebase deploy --only firestore:rules,storage
```

## Integração porta-Firebase

O vínculo entre usuários Firebase e perfis do sistema da porta é executado fora do app, com service account do Firebase:

```powershell
python -m pip install firebase-admin
$env:DOOR_ADMIN_PIN = "<PIN_ADMIN>"
python tools/sync_door_firebase_users.py --door-url http://192.168.11.2 --service-account C:\caminho\service-account.json
```

Revise o relatório em `build/door/door-firebase-link-report.json` e publique os vínculos somente depois:

```powershell
python tools/sync_door_firebase_users.py --door-url http://192.168.11.2 --service-account C:\caminho\service-account.json --apply-firebase
```

Não versionar o PIN, a service account ou exports reais da placa.

## Observações de migração

- O Android de referência usa `profile_pictures/{uid}` no Storage; o iOS usa `profile_images/{uid}.jpg`. Recomenda-se padronizar esse caminho.
- O Android contém duas implementações de telas de login/cadastro em pacotes diferentes. Recomenda-se manter apenas as telas realmente usadas na navegação.
- O controle da sala deve preferir uma API intermediária autenticada por Firebase ID Token. A chave direta da porta deve ficar fora do Git e ser tratada apenas como compatibilidade local/legada.
