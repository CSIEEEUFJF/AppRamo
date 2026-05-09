# Módulo: Firebase e dados compartilhados

## Objetivo

Centralizar autenticação, persistência e arquivos usados pelos apps Android e iOS.

## Serviços usados

- Firebase Authentication.
- Cloud Firestore.
- Firebase Storage.

## Coleções

### `users`

Documento: `users/{uid}`

Responsabilidade:

- guardar dados públicos/operacionais do membro;
- guardar capítulos e cargos;
- guardar URL da foto de perfil.

Campos mínimos:

- `name`
- `email`
- `phoneNumber`
- `birthDate`
- `profilePictureUrl`
- `chapterRoles`

### `tasks`

Documento: `tasks/{taskId}`

Responsabilidade:

- guardar tarefas por capítulo;
- permitir tarefas globais.

Campos mínimos:

- `title`
- `description`
- `chapter`
- `completed`

### `events`

Documento: `events/{eventId}`

Responsabilidade:

- guardar eventos e reuniões por capítulo;
- permitir eventos globais.

Campos mínimos:

- `title`
- `description`
- `location`
- `startTime`
- `endTime`
- `chapter`

## Índices prováveis

Eventos:

- `chapter` com operador `in`.
- `startTime` ascendente.

Tarefas:

- `chapter` com operador `in`.
- `completed` ascendente.
- `title` ascendente.

## Storage

Uso atual:

- Android: `profile_pictures/{uid}`.
- iOS: `profile_images/{uid}.jpg`.

Recomendação:

- padronizar para `profile_pictures/{uid}.jpg` ou outro caminho único antes do MVP.

## Regras de segurança

Diretriz mínima:

- somente usuários autenticados podem ler dados necessários ao app;
- usuário só pode editar seu próprio perfil;
- criação/edição/exclusão de tarefas e eventos deve depender de cargo ou capítulo;
- fotos devem ser gravadas apenas pelo próprio usuário ou por administradores.

## Pontos sensíveis

- Consultas com `whereIn` têm limite de itens; se um usuário tiver muitos capítulos, pode ser necessário dividir consultas.
- Dados globais usam `Todos` em parte do app e `Geral` em parte do iOS; escolher uma convenção única.
- Regras de segurança precisam acompanhar o modelo de cargo.
- Migrações de campos devem ser compatíveis com Android e iOS ao mesmo tempo.

## Validação mínima

- Criar usuário e confirmar `users/{uid}`.
- Fazer upload de foto e confirmar leitura nas duas plataformas.
- Criar tarefa e evento por capítulo.
- Criar tarefa e evento global.
- Confirmar índices exigidos pelo Firestore.
- Testar regras com usuário comum e usuário de diretoria.

