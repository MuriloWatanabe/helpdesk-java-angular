# Help Desk Frontend Redesign

## Intent

Reformular todo o frontend Angular para a identidade das referências: interface operacional, sidebar escura, fundo off-white, cartões claros, tipografia direta e verde-petróleo como cor de ação. Serviços, permissões, rotas e dados existentes permanecem.

## Goals

- Aplicar o estilo em autenticação, painel, chamados, relatórios, usuários, perfil e erros.
- Preservar fluxos, estados de carregamento/erro/vazio, acessibilidade e responsividade.
- Ocultar no login e OpenAPI as contas demonstrativas, sem excluir ou desativar essas contas.
- Aproximar login, painel e detalhe do chamado das imagens fornecidas.

## Non-goals

- Alterar API, regras de negócio, permissões, banco ou massa de demonstração.
- Adicionar biblioteca de UI, fonte externa ou novo endpoint.
- Substituir dados reais pelos exemplos fictícios das imagens.

## Constraints

- Angular 21 standalone, SCSS, componentes e Chart.js existentes.
- Sidebar como drawer em telas estreitas, sem overflow horizontal.
- Rótulos, foco visível, estados desabilitados e mensagens acessíveis.
- A cor não será o único meio de comunicar status, prioridade ou erro.
- Como o Figma não está acessível no ambiente, as imagens anexadas são a referência visual aprovada.

## Approaches

1. Reformular somente as telas retratadas: rejeitado por deixar o restante inconsistente.
2. Trocar apenas tokens globais: rejeitado por não reproduzir as composições principais.
3. Sistema visual global com reformulação progressiva de todas as telas: selecionado por entregar consistência sem trocar a lógica.

## Design

- Marca “Help Desk” e subtítulo “Suporte técnico interno”.
- Paleta base: sidebar `#15191c`, fundo `#f3f3ef`, cartões `#ffffff`, texto `#171a1c`, secundário `#6f7474`, bordas `#deded8` e ação `#168979`.
- Login dividido em painel institucional e formulário; no mobile, introdução compacta ou apenas formulário.
- Sidebar preserva permissões, rota ativa, perfil, logout e drawer.
- Painel prioriza saudação, quatro indicadores, recentes e status, mantendo insights existentes.
- Detalhe usa cabeçalho de andamento, conteúdo principal e painel lateral de SLA/responsáveis.
- Demais listas, formulários e páginas reutilizam tokens, controles, tabelas e estados.

## Acceptance Criteria

- Nenhum e-mail, papel ou senha demonstrativa aparece no login/OpenAPI.
- Contas permanecem em `database/data.sql` e autenticáveis.
- Rotas, ações e restrições de perfil continuam compilando e funcionando.
- Testes frontend/backend e build de produção passam sem avisos relevantes.
- Layout permanece utilizável a partir de 360 px.
