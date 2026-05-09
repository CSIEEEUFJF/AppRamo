# Módulo: Membros do Ramo

## Objetivo

Exibir os membros cadastrados, seus capítulos e cargos, permitindo consulta rápida de dados de contato.

## Arquivos principais

Android:

- [MembersPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/MembersPage.kt)

iOS:

- [MembersView.swift](../../apps/ios/AppRamoIEEE/MembersView.swift)
- [Models.swift](../../apps/ios/AppRamoIEEE/Models.swift)

## Fonte de dados

Coleção: `users`

Campos consumidos:

- `name`
- `phoneNumber`
- `profilePictureUrl`
- `chapterRoles`
- `email`, no iOS para detalhe do membro

## Comportamento esperado

Android:

- Escuta `users` em tempo real.
- Agrupa membros por capítulo.
- Exibe grade com foto e nome.
- Ao tocar no membro, mostra cargo no capítulo selecionado e telefone.

iOS:

- Busca os usuários no Firestore ao abrir a tela.
- Exibe grade com foto, nome e primeiro capítulo/cargo.
- Ao tocar no membro, mostra detalhes, cargos, e-mail e telefone.

## Fluxo principal

1. App consulta a coleção `users`.
2. App transforma cada documento em perfil de usuário.
3. App usa `chapterRoles` para exibir vínculo com capítulos.
4. Usuário toca em um membro.
5. App abre detalhe com dados de contato e cargos.

## Pontos sensíveis

- Perfis sem `chapterRoles` devem continuar aparecendo de forma segura.
- Fotos podem vir como URL pública ou referência Firebase Storage.
- Android e iOS hoje apresentam agrupamentos diferentes; a experiência deve ser padronizada.
- Dados sensíveis como telefone devem respeitar regras de acesso definidas pelo Ramo.

## Validação mínima

- Criar três usuários em capítulos diferentes.
- Confirmar exibição de nome e foto.
- Confirmar agrupamento por capítulo no Android.
- Confirmar detalhe do usuário nas duas plataformas.
- Confirmar atualização após alteração no Firestore.

