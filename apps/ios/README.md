# AppRamo iOS

Aplicativo iOS nativo do Ramo Estudantil IEEE UFJF.

Base importada do projeto `AppRamoIEEEUFJF_iOS`, com ajustes iniciais para uso dentro do monorepo e remoção de credenciais versionadas.

## Stack

- SwiftUI
- Firebase iOS SDK
- SDWebImageSwiftUI
- SDWebImageWebPCoder

## Configuração

1. Adicione `AppRamoIEEE/GoogleService-Info.plist`.
2. Configure `DoorAPIBaseURL` no `Info.plist` local ou por build setting. Deixe `DoorAPIKey` vazia quando a API intermediária validar Firebase ID Token.
3. Abra `AppRamoIEEE.xcodeproj` no Xcode e aguarde a resolução dos Swift Packages.

## Execução

Abra o projeto no Xcode, selecione um simulador ou dispositivo e execute o target `AppRamoIEEE`.
