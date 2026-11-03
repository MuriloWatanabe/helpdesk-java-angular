# Help Desk Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aplicar em todo o frontend a identidade Help Desk, preservar fluxos e ocultar credenciais demonstrativas.

**Architecture:** Centralizar tokens em `styles.scss` e reformular templates/SCSS por área. TypeScript muda apenas para apresentação e remoção do autopreenchimento; contratos HTTP permanecem.

**Tech Stack:** Angular 21, TypeScript 5.9, SCSS, Chart.js, Vitest, Spring Boot 4 e JUnit.

**Spec:** `docs/superpowers/specs/2026-11-03-helpdesk-frontend-redesign-design.md`

## Global Constraints

- Sem novas dependências, endpoints, contratos ou alterações na massa de demonstração.
- Paleta base `#15191c`, `#f3f3ef`, `#ffffff`, `#171a1c`, `#6f7474`, `#deded8`, `#168979`.
- Preservar perfis, loading/erro/vazio, teclado, foco, drawer e mobile de 360 px.
- Por instrução do usuário, não realizar commits; manter somente as alterações na branch `dev`.

## Review Focus

- Navegação e ações corretas para cliente, técnico e administrador.
- Redirecionamento `retorno` e aviso de sessão expirada.
- Tabelas, abas e ações sem overflow em 360 px.
- Estados loading, vazio e erro legíveis.
- Ausência de credenciais demonstrativas na UI e OpenAPI.

---

### Task 1: Marca e fundação visual

**Files:** `frontend/src/styles.scss`, `frontend/src/app/app.routes.ts`, `frontend/src/app/shared/logo/*`.

**Produces:** tokens globais e marca “Help Desk”.

- [ ] Criar teste da marca e confirmar RED.
- [ ] Implementar marca, tokens, controles e títulos.
- [ ] Verificar: `cd frontend && npm test -- --watch=false --include src/app/shared/logo/logo.component.spec.ts && npm run build`.

### Task 2: Login e autenticação sem credenciais expostas

**Files:** `frontend/src/app/features/auth/**`.

**Consumes:** Task 1. **Produces:** shell dividido sem atalhos demonstrativos.

- [ ] Criar teste do login real, ausência de credenciais e retorno; confirmar RED.
- [ ] Implementar login, cadastro e recuperação no novo tema.
- [ ] Verificar: `cd frontend && npm test -- --watch=false --include src/app/features/auth/login/login.component.spec.ts && npm run build`.

### Task 3: Navegação lateral responsiva

**Files:** `frontend/src/app/layout/sidebar/*`.

**Consumes:** marca e sinais de perfil. **Produces:** sidebar de 252 px e drawer móvel.

- [ ] Preservar `isCliente`, `isAtendente`, `isAdmin`, `aberta` e `logout()`.
- [ ] Implementar navegação escura, indicador verde, perfil e logout.
- [ ] Verificar: `cd frontend && npm run build`.

### Task 4: Painel operacional

**Files:** `frontend/src/app/features/dashboard/*`.

**Consumes:** `DashboardStats`. **Produces:** indicadores, recentes, status e insights.

- [ ] Mapear somente campos reais do contrato.
- [ ] Reorganizar template/SCSS e ajustar cores Chart.js.
- [ ] Verificar: `cd frontend && npm test -- --watch=false && npm run build`.

### Task 5: Listas e formulários de chamados

**Files:**
- Modify: `frontend/src/app/features/chamados/chamados-list/chamados-list.component.html`
- Modify: `frontend/src/app/features/chamados/chamados-list/chamados-list.component.scss`
- Modify: `frontend/src/app/features/chamados/novo-chamado/novo-chamado.component.html`
- Modify: `frontend/src/app/features/chamados/novo-chamado/novo-chamado.component.scss`
- Modify: `frontend/src/app/features/chamados/chamado-edit/chamado-edit.component.html`

**Interfaces:** Consome shell/controles e produz listagem, filtros, paginação, criação e edição responsivos.

- [ ] **Step 1: Implementar listagem** — cabeçalho, filtros, tabela e fallback móvel sem remover busca, paginação ou links.
- [ ] **Step 2: Implementar formulários** — aplicar superfícies/grid preservando `formControlName`, validações, selects, anexos e mensagens.
- [ ] **Step 3: Verificar** — Run: `cd frontend && npm test -- --watch=false --include src/app/features/chamados/chamado-ui.spec.ts && npm run build`; Expected: PASS sem avisos.

### Task 6: Detalhe do chamado

**Files:**
- Modify: `frontend/src/app/features/chamados/chamado-detail/chamado-detail.component.html`
- Modify: `frontend/src/app/features/chamados/chamado-detail/chamado-detail.component.scss`

**Interfaces:** Consome ações/sinais existentes e produz progresso, conteúdo principal e painel lateral.

- [ ] **Step 1: Reorganizar bindings** — preservar condicionais, formulários, upload, comentários internos, histórico, avaliação e status.
- [ ] **Step 2: Aplicar progresso/responsividade** — duas colunas desktop, uma mobile.
- [ ] **Step 3: Verificar** — Run: `cd frontend && npm test -- --watch=false --include src/app/features/chamados/chamado-ui.spec.ts && npm run build`; Expected: PASS.

### Task 7: Relatórios, usuários, perfil e erros

**Files:** `frontend/src/app/features/relatorios/*`, `usuarios/*`, `perfil/*`, `erros/erros.scss`.

**Interfaces:** Consome sistema visual e preserva exportação, impressão, CRUD, filtros e perfil.

- [ ] **Step 1:** Atualizar superfícies, métricas, tabelas, modais/formulários e gráficos.
- [ ] **Step 2:** Atualizar perfil e erros.
- [ ] **Step 3:** Ajustar Chart.js e `@media print` sem alterar dados.
- [ ] **Step 4: Verificar** — Run: `cd frontend && npm test -- --watch=false && npm run build`; Expected: PASS.

### Task 8: Documentação segura e verificação final

**Files:**
- Modify: `backend/helpdesk/src/test/java/com/murilo/helpdesk/config/OpenApiDocsTest.java`
- Modify: `backend/helpdesk/src/main/java/com/murilo/helpdesk/config/OpenApiConfig.java`

**Interfaces:** Produz OpenAPI sem e-mails ou senha demonstrativa.

- [ ] **Step 1:** Adicionar teste que rejeita `admin@helpdesk.com`, `tecnico@helpdesk.com`, `cliente@helpdesk.com`, `123456` e “Usuários de demonstração”.
- [ ] **Step 2: Confirmar RED** — Run: `cd backend/helpdesk && ./gradlew test --tests com.murilo.helpdesk.config.OpenApiDocsTest`; Expected: FAIL.
- [ ] **Step 3:** Remover somente o parágrafo de `OpenApiConfig`; não alterar `database/data.sql`.
- [ ] **Step 4: Verificar** — Run: `cd backend/helpdesk && ./gradlew test`; Run: `cd frontend && npm test -- --watch=false && npm run build`; Expected: PASS.
- [ ] **Step 5: Auditar** — Run: `rg -n "admin@helpdesk|tecnico@helpdesk|cliente@helpdesk|acesso de demonstração|Usuários de demonstração" frontend/src backend/helpdesk/src/main`; Expected: nenhuma ocorrência.
