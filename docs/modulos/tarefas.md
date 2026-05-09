# Módulo: Tarefas do capítulo

## Objetivo

Gerenciar tarefas vinculadas aos capítulos do usuário e tarefas globais do Ramo.

## Arquivos principais

Android:

- [TasksPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/TasksPage.kt)
- [NavGraph.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt)

iOS:

- [TasksView.swift](../../apps/ios/AppRamoIEEE/TasksView.swift)
- [AppViewModel.swift](../../apps/ios/AppRamoIEEE/AppViewModel.swift)

## Modelo de dados

Coleção: `tasks/{taskId}`

```json
{
  "title": "Título da tarefa",
  "description": "Descrição da tarefa",
  "chapter": "RAS",
  "completed": false
}
```

## Regras de visibilidade

- Usuário vê tarefas dos capítulos presentes em `chapterRoles`.
- Usuário vê tarefas globais marcadas como `Todos`.
- O iOS também inclui `Geral` no listener atual.

## Fluxo principal

1. App carrega `chapterRoles` do usuário autenticado.
2. App consulta `tasks` filtrando por capítulos visíveis.
3. App ordena por `completed` e `title`, quando suportado pela consulta.
4. Usuário cria uma nova tarefa.
5. Usuário marca tarefa como concluída ou pendente.
6. Usuário pode apagar tarefa.

## Operações

- Criar tarefa.
- Listar tarefas.
- Alternar `completed`.
- Abrir detalhes.
- Excluir tarefa.

## Pontos sensíveis

- Firestore pode exigir índice composto para `chapter`, `completed` e `title`.
- Regras de segurança devem controlar quem pode criar, concluir e excluir tarefas.
- `description` é obrigatório no Android, mas não necessariamente no iOS.
- A lista de capítulos disponíveis deve incluir `Todos` quando a tarefa for global.

## Validação mínima

- Criar tarefa para capítulo do usuário.
- Criar tarefa global.
- Confirmar que tarefa de outro capítulo não aparece para usuário sem acesso.
- Marcar tarefa como concluída.
- Excluir tarefa e confirmar remoção em tempo real.

