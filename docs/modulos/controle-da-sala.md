# Módulo: Controle da sala

## Objetivo

Permitir que o app abra a porta remotamente e gerencie o modo reunião implementado no sistema da porta.

Este módulo é o ponto de contato entre o AppRamo e a infraestrutura IoT. A implementação atual foi baseada no repositório local `C:\Users\CS\Documents\IoT_Ramo_Renesas`, que expõe a nova API HTTP da placa.

## Arquivos principais

Android:

- [DoorControlPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/DoorControlPage.kt)
- [build.gradle.kts](../../apps/android/app/build.gradle.kts)

iOS:

- [DoorControlView.swift](../../apps/ios/AppRamoIEEE/DoorControlView.swift)
- [Models.swift](../../apps/ios/AppRamoIEEE/Models.swift)
- [Info.plist](../../apps/ios/AppRamoIEEE/Info.plist)

## Contrato HTTP usado pelo AppRamo

Todas as rotas usam:

- `X-API-KEY: <chave>`
- ou `Authorization: Bearer <chave>`, no firmware

O app usa `X-API-KEY`.

### Abrir porta

```http
POST /api/door/open
X-API-KEY: <chave>
```

Resposta de sucesso:

```json
{"ok":true,"message":"Door open command sent."}
```

### Consultar modo reunião

```http
GET /api/meeting/status
X-API-KEY: <chave>
```

Resposta esperada:

```json
{
  "ok": true,
  "active": false,
  "pending_count": 1,
  "time_synced": true,
  "now_unix": 1893455700,
  "active_selected_profiles": 0,
  "active_allowed_cards": 0,
  "last_id": 3,
  "last_start_unix": 1893456000,
  "last_selected_profiles": 0,
  "last_allowed_cards": 0,
  "last_status": "scheduled",
  "schedules": [
    {
      "id": 3,
      "start_unix": 1893456000,
      "profile_count": 2,
      "recurrence": "none",
      "weekdays_mask": 0
    }
  ]
}
```

### Agendar modo reunião

```http
POST /api/meeting/schedule
Content-Type: application/json
X-API-KEY: <chave>
```

Agendamento relativo:

```json
{
  "delay_seconds": 300,
  "profile_indices": [0, 4, 12],
  "recurrence": "none"
}
```

Agendamento diário:

```json
{
  "delay_seconds": 300,
  "profile_indices": [0, 4, 12],
  "recurrence": "daily"
}
```

Agendamento semanal:

```json
{
  "delay_seconds": 300,
  "profile_indices": [0, 4, 12],
  "recurrence": "weekly",
  "weekdays": [1, 3, 5]
}
```

Observações:

- `profile_indices` usa os índices dos perfis cadastrados na placa, não os IDs do Firebase.
- `delay_seconds` precisa ficar entre 1 segundo e 24 horas.
- A placa precisa estar com horário sincronizado para converter atraso em `start_unix`.
- Se `recurrence` for `weekly` e `weekdays` não for enviado, a placa usa o dia de `start_unix`.

### Cancelar modo reunião

Cancelar por ID:

```http
POST /api/meeting/cancel
Content-Type: application/json
X-API-KEY: <chave>
```

```json
{"id":3}
```

Cancelar todos:

```http
POST /api/meeting/cancel
X-API-KEY: <chave>
```

Sem corpo.

## Configuração

Android:

- `DOOR_API_BASE_URL`
- `DOOR_API_KEY`

Fallbacks aceitos no Android:

- `DOOR_RELAY_BASE_URL`
- `DOOR_RELAY_API_KEY`

iOS:

- `DoorAPIBaseURL`
- `DoorAPIKey`

Fallbacks aceitos no iOS:

- `DoorRelayBaseURL`
- `DoorRelayAPIKey`

As chaves reais não devem ser commitadas.

## Fluxo principal

1. Usuário abre a tela de controle da sala.
2. App consulta `GET /api/meeting/status`.
3. App exibe estado do modo reunião e agendamentos pendentes.
4. Usuário pode abrir a porta via `POST /api/door/open`.
5. Usuário pode agendar modo reunião por atraso em minutos.
6. Usuário pode cancelar um agendamento por ID ou cancelar todos.

## O que não faz parte deste app

- Fechamento manual da porta.
- Cadastro de perfis da placa.
- Mapeamento automático entre usuário Firebase e índice do perfil na placa.

## Pontos sensíveis

- `profile_indices` depende da ordem/cadastro local da placa.
- O app não deve expor a chave da API em commits.
- Agendamento por atraso depende do NTP da placa.
- O limite do firmware é de até 8 agendamentos pendentes.
- Agendamentos recorrentes mantêm o mesmo ID e avançam para a próxima ocorrência.

## Validação mínima

- Abrir porta com chave válida.
- Validar erro com chave inválida.
- Consultar status do modo reunião.
- Agendar reunião única.
- Agendar reunião diária.
- Agendar reunião semanal.
- Cancelar reunião por ID.
- Cancelar todos os agendamentos.
