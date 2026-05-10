# Checklist de validação

Este checklist cobre a primeira rodada de validação manual e técnica do AppRamo.

## Repositório

- Clonar o repositório em uma máquina limpa.
- Conferir se `apps/android` abre no Android Studio.
- Conferir se `apps/ios/AppRamoIEEE.xcodeproj` abre no Xcode.
- Confirmar que arquivos reais de credenciais não aparecem no Git.

## Android

- Adicionar `apps/android/app/google-services.json`.
- Configurar `DOOR_API_BASE_URL` e `DOOR_API_KEY`.
- Rodar `.\gradlew.bat :app:assembleDebug`.
- Rodar `.\gradlew.bat :app:testDebugUnitTest`.
- Instalar o app em emulador ou dispositivo.
- Validar login, cadastro, perfil, tarefas, calendário, membros e controle da sala.

## iOS

- Adicionar `apps/ios/AppRamoIEEE/GoogleService-Info.plist`.
- Configurar `DoorAPIBaseURL` e `DoorAPIKey`.
- Resolver Swift Packages no Xcode.
- Compilar o target `AppRamoIEEE`.
- Validar login, cadastro, perfil, tarefas, calendário, membros e controle da sala.

## Firebase

- Confirmar Authentication com provedor e-mail/senha ativo.
- Criar usuário de teste.
- Confirmar escrita e leitura em `users`.
- Confirmar escrita e leitura em `tasks`.
- Confirmar escrita e leitura em `events`.
- Confirmar upload e leitura de imagem no Storage.
- Validar índices necessários do Firestore.

## Controle da sala

- Confirmar `POST /api/door/open`.
- Confirmar `GET /api/meeting/status`.
- Confirmar `POST /api/meeting/schedule` com `delay_seconds` e `profile_indices`.
- Confirmar `POST /api/meeting/cancel` com ID.
- Confirmar `POST /api/meeting/cancel` sem ID para cancelar todos.
- Validar mensagem de erro sem conexão ou sem chave.
- Validar que a chave de API não aparece em logs ou commits.

## Critérios de pronto para MVP

- Android compila e executa com credenciais locais.
- iOS compila e executa com credenciais locais.
- Fluxos principais funcionam nas duas plataformas.
- Regras de segurança mínimas do Firebase estão aplicadas.
- A documentação de módulos reflete o estado real do código.
