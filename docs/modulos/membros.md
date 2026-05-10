# Módulo: Membros do Ramo

## Objetivo

Exibir os membros cadastrados, seus capítulos e cargos aprovados, sem expor dados pessoais sensíveis.

## Arquivos principais

Android:

- [MembersPage.kt](../../apps/android/app/src/main/java/com/ramoieeeufjf/appRamo/pages/MembersPage.kt)

iOS:

- [MembersView.swift](../../apps/ios/AppRamoIEEE/MembersView.swift)
- [Models.swift](../../apps/ios/AppRamoIEEE/Models.swift)

## Fonte de dados

Coleção: `publicProfiles`

Campos consumidos:

- `name`
- `profilePictureUrl`
- `chapterRoles`

## Comportamento esperado

Android:

- Escuta `publicProfiles` em tempo real.
- Agrupa membros por capítulo.
- Exibe grade com foto e nome.
- Ao tocar no membro, mostra capítulo e cargo aprovado.

iOS:

- Escuta `publicProfiles` em tempo real.
- Agrupa membros por capítulo.
- Ao tocar no membro, mostra apenas detalhes públicos e cargos aprovados.

## Fluxo principal

1. App consulta a coleção `publicProfiles`.
2. App transforma cada documento em perfil de usuário.
3. App usa `chapterRoles` para exibir vínculo com capítulos.
4. Usuário toca em um membro.
5. App abre detalhe com dados de contato e cargos.

## Pontos sensíveis

- Perfis sem `chapterRoles` devem continuar aparecendo de forma segura.
- Fotos podem vir como URL pública ou referência Firebase Storage.
- Android e iOS devem manter o mesmo agrupamento por capítulo.
- Dados sensíveis como telefone, e-mail e nascimento permanecem em `users/{uid}`.

## Validação mínima

- Criar três usuários em capítulos diferentes.
- Confirmar exibição de nome e foto.
- Confirmar agrupamento por capítulo nas duas plataformas.
- Confirmar detalhe do usuário nas duas plataformas.
- Confirmar atualização após alteração no Firestore.
