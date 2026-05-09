# Roadmap inicial

## Marco 0: base do repositório

- Estrutura monorepo com `apps/android`, `apps/ios` e `docs`.
- Documentação inicial de requisitos, arquitetura e configuração.
- Remoção de credenciais reais do controle de versão.

## Marco 1: estabilização Android

- Garantir build limpo do Android em ambiente novo.
- Consolidar telas duplicadas de login/cadastro.
- Padronizar textos em português.
- Revisar navegação e estados vazios.
- Cobrir fluxos críticos com testes de ViewModel ou testes instrumentados.

## Marco 2: alinhamento iOS

- Validar paridade funcional com o Android.
- Padronizar modelos, nomes de campos e caminhos de Storage.
- Garantir tratamento de erros equivalente nos fluxos de Firebase e relay.
- Remover qualquer configuração sensível hard-coded.

## Marco 3: segurança e permissões

- Escrever regras de Firestore e Storage por usuário, capítulo e cargo.
- Definir papéis administrativos.
- Proteger criação e exclusão de tarefas/eventos quando necessário.
- Revisar a autenticação do relay da sala.

## Marco 4: preparação de release

- Definir versionamento e changelog.
- Configurar CI para build Android e validação iOS.
- Criar checklist de publicação.
- Preparar política de privacidade e termos de uso, se aplicável.

