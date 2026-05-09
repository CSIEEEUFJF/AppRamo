# Módulo: Controle da sala

## Objetivo

Permitir que o app consulte o estado da sala e envie comandos para porta e luz.

Este módulo é o ponto de contato entre o AppRamo e a infraestrutura IoT. A documentação foi escrita considerando o projeto [`IoT_Ramo_Renesas`](https://github.com/CSIEEEUFJF/IoT_Ramo_Renesas) como referência operacional para controle físico de acesso, interface local, rede e estado da porta/luz.

## Arquivos principais

Android:

- [DoorControlPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/DoorControlPage.kt)
- [build.gradle.kts](../../apps/android/app/build.gradle.kts)

iOS:

- [DoorControlView.swift](../../apps/ios/AppRamoIEEE/DoorControlView.swift)
- [Info.plist](../../apps/ios/AppRamoIEEE/Info.plist)

## Contrato HTTP usado pelo AppRamo

Status:

```http
GET /status?device_id=esp01
X-API-KEY: <chave>
```

Resposta esperada:

```json
{
  "device_id": "esp01",
  "door": 1,
  "light": 0,
  "last_seen": 1710000000
}
```

Comando:

```http
POST /send
Content-Type: application/json
X-API-KEY: <chave>
```

Corpo:

```json
{
  "device_id": "esp01",
  "command": {
    "action": "door_on"
  }
}
```

## Comandos mínimos

- `door_on`: abre a porta.
- `door_off`: fecha a porta, se suportado pelo relay/dispositivo.
- `light_on`: liga a luz.
- `light_off`: desliga a luz.

## Estados exibidos

Porta:

- `1`: `Aberta`.
- `0`: `Fechada`.
- ausente/desconhecido: `Desconhecida`.

Luz:

- `1`: `Ligada`.
- `0`: `Desligada`.
- ausente/desconhecido: `Desconhecida`.

## Configuração

Android:

- `DOOR_RELAY_BASE_URL`
- `DOOR_RELAY_API_KEY`
- `DOOR_RELAY_DEVICE_ID`

iOS:

- `DoorRelayBaseURL`
- `DoorRelayAPIKey`
- `DoorRelayDeviceID`

As chaves reais não devem ser commitadas.

## Relação com `IoT_Ramo_Renesas`

O projeto IoT de referência documenta:

- controle físico de porta e luz;
- interface local com display/touch;
- autenticação por RFID;
- painel web administrativo;
- rede em modo HTTP;
- logs de acesso e persistência local.

O AppRamo, por enquanto, não fala diretamente com o firmware Renesas documentado. Ele espera um relay HTTP com `/status` e `/send`.

Para integração direta com o `IoT_Ramo_Renesas`, há duas opções:

- adaptar o firmware/servidor embarcado para expor o contrato `/status` e `/send`;
- manter um serviço intermediário que traduza o contrato mobile para as rotas atuais do firmware, como `/door`, `/portaon` e `/lampadatoggle`.

## Fluxo principal

1. Usuário abre a tela de controle da sala.
2. App consulta `/status`.
3. App exibe estado de porta e luz.
4. Usuário aciona porta ou luz.
5. App envia `/send`.
6. App faz atualização otimista.
7. App consulta `/status` novamente para obter o estado real.

## Pontos sensíveis

- A chave de API atual é uma credencial compartilhada; idealmente deve ser substituída por autorização por usuário.
- O endpoint precisa responder rápido para não travar a experiência mobile.
- Erros de rede devem aparecer na tela.
- A semântica de `door_off` deve ser confirmada, porque o projeto IoT registra principalmente abertura de porta.
- Logs de abertura/acionamento devem ser definidos no lado IoT ou relay.

## Validação mínima

- Consultar status com chave válida.
- Validar erro com chave inválida.
- Acionar `door_on`.
- Alternar luz com `light_on` e `light_off`.
- Confirmar que o estado real retorna corretamente após comando.
- Confirmar que nenhum segredo aparece no Git.

