# Segurança

## Princípios

- Credenciais reais não devem ser versionadas no repositório.
- O app não grava cargos aprovados a partir do cadastro ou edição do próprio usuário.
- O campo `chapterRoles` representa permissão efetiva e deve ser alterado apenas por administradores ou processo confiável fora do app cliente.
- O campo `requestedChapterRoles` representa solicitação do usuário e não concede acesso.
- A listagem de membros usa `publicProfiles`, sem telefone, e-mail ou data de nascimento.
- O controle da sala deve preferir um serviço intermediário autenticado por Firebase ID Token. A chave `DOOR_API_KEY` fica apenas como compatibilidade local/legada.
- O vínculo entre usuários Firebase e perfis da porta é administrativo; o app cliente não grava `doorProfiles` nem campos `door*`.

## Firebase

As regras versionadas ficam em:

- `firestore.rules`
- `storage.rules`
- `firebase.json`

Para publicar:

```powershell
firebase deploy --only firestore:rules,storage
```

## Dados

Coleções privadas:

- `users/{uid}`: dados pessoais do próprio usuário.
- `events/{eventId}`: eventos visíveis por capítulo.
- `tasks/{taskId}`: tarefas visíveis por capítulo.

Coleção pública autenticada:

- `publicProfiles/{uid}`: nome, foto e cargos aprovados.
- `doorProfiles/{uid}`: nome, índice da porta e metadados sem UID RFID completo.

## Controle da sala

Os apps enviam `Authorization: Bearer <Firebase ID Token>` quando `DOOR_API_KEY` não está configurada. O endpoint recomendado é uma API intermediária que valide esse token, confira permissões em `chapterRoles` e só então converse com o firmware/serviço da porta.

O script [sync_door_firebase_users.py](../tools/sync_door_firebase_users.py) usa o PIN administrativo apenas em execução local, por `DOOR_ADMIN_PIN` ou argumento de linha de comando. Não versionar PIN, service account, exports reais do `users.json` ou chaves da API.

`doorProfiles` não armazena cartões RFID completos. O app só recebe `doorProfileIndex` e `cardCount`, suficientes para agendar o modo reunião.
