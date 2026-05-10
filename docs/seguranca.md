# Segurança

## Princípios

- Credenciais reais não devem ser versionadas no repositório.
- O app não grava cargos aprovados a partir do cadastro ou edição do próprio usuário.
- O campo `chapterRoles` representa permissão efetiva e deve ser alterado apenas por administradores ou processo confiável fora do app cliente.
- O campo `requestedChapterRoles` representa solicitação do usuário e não concede acesso.
- A listagem de membros usa `publicProfiles`, sem telefone, e-mail ou data de nascimento.
- O controle da sala deve preferir um serviço intermediário autenticado por Firebase ID Token. A chave `DOOR_API_KEY` fica apenas como compatibilidade local/legada.

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

## Controle da sala

Os apps enviam `Authorization: Bearer <Firebase ID Token>` quando `DOOR_API_KEY` não está configurada. O endpoint recomendado é uma API intermediária que valide esse token, confira permissões em `chapterRoles` e só então converse com o firmware/serviço da porta.
