# Requisitos do AppRamo

Este documento define o escopo mínimo inicial do AppRamo. A referência funcional principal é o aplicativo Android em Jetpack Compose, porque ele contém o conjunto de funções já implementadas e integradas.

Para documentação operacional por área, veja [modulos/README.md](modulos/README.md).

## Usuários e permissões

- O usuário principal é um membro do Ramo Estudantil IEEE UFJF.
- Cada usuário pode pertencer a um ou mais capítulos: `RAS`, `IAS`, `PES`, `WIE`, `EdSoc`, `Diretoria`, `SIGHT`.
- Cada associação a capítulo possui um cargo. Para capítulos técnicos, os cargos mínimos são `Membro` e `Presidente`; para `Diretoria`, os cargos mínimos são `Presidente`, `Vice Presidente`, `Tesoureiro`, `Marketing` e `Webmaster`.
- Eventos e tarefas devem ser filtrados pelos capítulos do usuário e por entradas globais marcadas como `Todos`.

## Funcionalidades mínimas

### Autenticação

- Permitir login por e-mail e senha usando Firebase Authentication.
- Exibir mensagem de erro quando a autenticação falhar.
- Permitir logout a partir da tela de perfil.

### Cadastro e edição de perfil

- Permitir cadastro de nome, data de nascimento, e-mail, senha, telefone, capítulos, cargos e foto de perfil.
- Criar a conta no Firebase Authentication quando o usuário ainda não existir.
- Salvar os dados do usuário na coleção `users` do Firestore.
- Fazer upload da foto de perfil para Firebase Storage.
- Reabrir a tela de cadastro em modo de edição quando o usuário já estiver autenticado.
- Desabilitar edição de e-mail e senha no modo de edição.

### Tela inicial

- Exibir saudação com nome do usuário autenticado.
- Exibir foto de perfil ou imagem padrão.
- Navegar para perfil, tarefas, calendário, controle da sala e membros do Ramo.
- Exibir identidade visual do IEEE e capítulos usados no app de referência.

### Perfil

- Exibir nome, e-mail, foto de perfil e cargos por capítulo.
- Permitir acessar edição de dados editáveis.
- Permitir logout e retorno para a tela de login.

### Membros do Ramo

- Carregar membros em tempo real a partir da coleção `users`.
- Agrupar membros por capítulo.
- Exibir foto e nome em grade.
- Ao selecionar um membro, exibir nome, cargo no capítulo selecionado e telefone.

### Tarefas do capítulo

- Carregar tarefas em tempo real a partir da coleção `tasks`.
- Filtrar tarefas por capítulos do usuário e `Todos`.
- Ordenar por conclusão e título.
- Criar tarefa com título, descrição e capítulo.
- Marcar tarefa como concluída ou pendente.
- Abrir detalhes da tarefa.
- Excluir tarefa.
- Exibir estado vazio quando não houver tarefas.

### Calendário do capítulo

- Carregar eventos em tempo real a partir da coleção `events`.
- Filtrar eventos por capítulos do usuário e `Todos`.
- Ordenar por data/hora de início.
- Criar evento com título, descrição, local, data/hora de início, data/hora de fim e capítulo.
- Abrir a agenda do dispositivo para salvar o evento localmente.
- Abrir detalhes do evento.
- Excluir evento.
- Exibir estado vazio quando não houver eventos.

### Controle da sala

- Abrir a porta usando `POST /api/door/open` com o header `X-API-KEY`.
- Consultar o modo reunião usando `GET /api/meeting/status`.
- Agendar modo reunião usando `POST /api/meeting/schedule`.
- Cancelar agendamentos de reunião usando `POST /api/meeting/cancel`.
- Exibir estado ativo/inativo do modo reunião.
- Exibir agendamentos pendentes e último status informado pela placa.
- Exibir estado de carregamento e mensagens de erro.

## Requisitos técnicos mínimos

- Android nativo em Kotlin.
- Interface Android em Jetpack Compose e Material 3.
- Navegação Android com Navigation Compose.
- Firebase Authentication, Firestore e Storage.
- Ktor Client no Android para o relay da sala.
- iOS nativo em SwiftUI com Firebase iOS SDK.
- Credenciais e chaves fora do controle de versão.

## Critérios de aceite iniciais

- Um membro consegue criar conta, autenticar, ver seu perfil e sair.
- Um membro autenticado vê apenas tarefas e eventos dos seus capítulos e globais.
- Um membro consegue criar, concluir e excluir tarefas.
- Um membro consegue criar e excluir eventos.
- A lista de membros reflete atualizações do Firestore sem reiniciar o app.
- O controle da sala consegue abrir a porta e gerenciar agendamentos de reunião quando a chave da API está configurada.
