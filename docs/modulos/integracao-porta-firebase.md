# Módulo: Integração porta-Firebase

## Objetivo

Relacionar os perfis persistidos no sistema da porta com os usuários do Firebase, permitindo que os apps selecionem membros pelo nome no modo reunião sem expor os UIDs completos dos cartões RFID.

## Arquivos principais

- [sync_door_firebase_users.py](../../tools/sync_door_firebase_users.py)
- [firestore.rules](../../firestore.rules)
- [DoorControlPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/DoorControlPage.kt)
- [DoorControlView.swift](../../apps/ios/AppRamoIEEE/DoorControlView.swift)
- [Models.swift](../../apps/ios/AppRamoIEEE/Models.swift)

## Fontes de dados

Sistema da porta:

- URL local padrão: `http://192.168.11.2`.
- Login administrativo: `POST /login` com o campo `pin`.
- Exportação: `GET /storage_users_download`.
- Formato base: lista `users.json` com `name`, `role`, `chapter`, `photo_id`, `uid`, `cards`, `cards_csv`, `is_admin`.

Firebase:

- `users/{uid}` continua sendo o documento privado do usuário.
- `doorProfiles/{uid}` guarda apenas metadados sanitizados para os apps.

## Modelo Firestore

### `doorProfiles/{uid}`

```json
{
  "firebaseUid": "uid-do-firebase",
  "name": "Nome do membro",
  "chapter": "CS",
  "role": "Membro",
  "doorProfileIndex": 7,
  "doorProfileName": "Nome no sistema da porta",
  "doorChapter": "CS",
  "doorRole": "Membro",
  "cardCount": 1,
  "hasDoorCards": true,
  "doorSource": "192.168.11.2",
  "matchMethod": "name_chapter",
  "matchScore": 0.94,
  "updatedAt": "server timestamp"
}
```

O app usa `doorProfileIndex` para montar `profile_indices` no agendamento do modo reunião.

### `users/{uid}`

Campos operacionais adicionados pelo script administrativo:

- `doorProfileIndex`
- `doorProfileName`
- `doorChapter`
- `doorRole`
- `doorCardCount`
- `doorLinkedAt`
- `doorSource`

Esses campos são bloqueados para escrita pelo cliente nas regras do Firestore. O script usa Admin SDK, que bypassa regras e deve rodar apenas em ambiente confiável.

## Execução

Instale a dependência administrativa uma vez:

```powershell
python -m pip install firebase-admin
```

Defina o PIN sem gravar no repositório:

```powershell
$env:DOOR_ADMIN_PIN = "<PIN_ADMIN>"
```

Rode em modo relatório:

```powershell
python tools/sync_door_firebase_users.py `
  --door-url http://192.168.11.2 `
  --service-account C:\caminho\service-account.json
```

Depois de revisar `build/door/door-firebase-link-report.json`, publique os vínculos:

```powershell
python tools/sync_door_firebase_users.py `
  --door-url http://192.168.11.2 `
  --service-account C:\caminho\service-account.json `
  --apply-firebase
```

Quando algum vínculo ficar ambíguo, crie um CSV manual com as colunas `firebase_uid,door_profile_index` e rode novamente:

```csv
firebase_uid,door_profile_index
uid-do-firebase,7
```

```powershell
python tools/sync_door_firebase_users.py `
  --door-url http://192.168.11.2 `
  --service-account C:\caminho\service-account.json `
  --bindings C:\caminho\bindings.csv `
  --apply-firebase
```

## Estratégia de vínculo

O script tenta casar perfis nesta ordem:

1. Interseção de cartões, se o Firebase já tiver algum campo de cartão.
2. `photo_id` apontando para o usuário ou para sua foto.
3. Similaridade de nome com bônus quando o capítulo também coincide.

Vínculos manuais via CSV têm prioridade. Perfis ambíguos ou abaixo do limiar ficam no relatório como não vinculados.

## Segurança

- Não versionar service account, PIN de admin, API key da porta ou exports reais.
- Não gravar UID completo de cartão no Firestore.
- `doorProfiles` pode ser lido por usuários autenticados, mas não pode ser escrito por clientes.
- Campos `door*` em `users/{uid}` são bloqueados contra alteração pelo próprio app.
- Em produção, preferir API intermediária validando Firebase ID Token antes de acionar a porta.

## Validação mínima

- Baixar `users.json` da porta em modo relatório.
- Conferir que o relatório não contém UIDs completos dos cartões.
- Publicar `firestore.rules`.
- Rodar com `--apply-firebase`.
- Abrir Android ou iOS e confirmar que os perfis vinculados aparecem na tela de controle da sala.
- Agendar modo reunião selecionando usuários vinculados e validar `GET /api/meeting/status`.
