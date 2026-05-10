# Módulo: Calendário do capítulo

## Objetivo

Gerenciar eventos e reuniões do Ramo por capítulo, com opção de salvar eventos na agenda do dispositivo.

## Arquivos principais

Android:

- [CalendarPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/CalendarPage.kt)
- [NavGraph.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/NavGraph.kt)

iOS:

- [CalendarView.swift](../../apps/ios/AppRamoIEEE/CalendarView.swift)
- [AppViewModel.swift](../../apps/ios/AppRamoIEEE/AppViewModel.swift)

## Modelo de dados

Coleção: `events/{eventId}`

```json
{
  "title": "Reunião semanal",
  "description": "Pauta da reunião",
  "location": "Sala do Ramo",
  "startTime": "Timestamp",
  "endTime": "Timestamp",
  "chapter": "RAS"
}
```

## Regras de visibilidade

- Usuário vê eventos dos seus capítulos.
- Usuário vê eventos globais marcados como `Todos`.
- As duas plataformas usam `Todos` como marcador global.

## Fluxo principal

1. App carrega capítulos do usuário.
2. App consulta `events` filtrando pelos capítulos visíveis.
3. App ordena eventos por `startTime`.
4. Usuário cria evento com título, descrição, local, início, fim e capítulo.
5. App salva evento no Firestore.
6. App oferece integração com calendário nativo.
7. Usuário pode apagar evento.

## Integração com calendário nativo

Android:

- Usa `Intent(Intent.ACTION_INSERT)` com `CalendarContract.Events`.

iOS:

- Usa `EventKit`.
- Em iOS 17+, solicita acesso de escrita com `requestWriteOnlyAccessToEvents`.
- Em versões anteriores, usa `requestAccess(to: .event)`.

## Pontos sensíveis

- Firestore pode exigir índice composto para `chapter` e `startTime`.
- Permissões de calendário precisam estar declaradas no iOS.
- Eventos globais devem usar a convenção única `Todos`.
- O Android permite selecionar início e fim; o iOS usa duração padrão de 1 hora no fluxo atual.

## Validação mínima

- Criar evento de capítulo.
- Criar evento global.
- Confirmar ordenação por data.
- Salvar evento no calendário do dispositivo.
- Excluir evento e confirmar remoção em tempo real.
