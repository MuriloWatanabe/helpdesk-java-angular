# Sistema de Gestão de Chamados (Help Desk)

## 1. Domínio do Problema

Empresas frequentemente enfrentam dificuldades no gerenciamento de chamados técnicos internos, como problemas de rede, computadores, sistemas e acessos.

Quando não há um sistema estruturado, podem ocorrer:

- Perda de solicitações
- Falta de priorização adequada
- Atraso no atendimento
- Ausência de histórico de ocorrências

O sistema proposto centraliza e organiza os chamados técnicos internos, proporcionando controle, rastreabilidade e melhor gestão do suporte, incluindo acompanhamento completo do ciclo de vida do chamado.

---

## 2. Objetivo do Sistema

- Cadastro de usuários com diferentes perfis de acesso
- Abertura de chamados técnicos
- Definição de prioridade e categorização
- Acompanhamento de status do chamado
- Registro de responsável pelo atendimento
- Histórico de atendimentos com interações (comentários)
- Controle administrativo

---

## 3. Arquitetura do Sistema

### Visão de Contexto

![Diagrama de Contexto](backend/helpdesk/docs/architecture/SystemContext-Sistema_de_Gestão_de_Chamados___Contexto.png)

### Visão de Containers

![Diagrama de Containers](backend/helpdesk/docs/architecture/Containers-Sistema_de_Gestão_de_Chamados___Containers.png)

### Visão de Componentes (Backend)

![Diagrama de Componentes (Backend)](backend/helpdesk/docs/architecture/BackendComponents-Backend___Componentes.png)

---

## 4. Requisitos Funcionais (RF)

| # | Requisito | Situação |
|---|---|---|
| **RF01** | Cadastro de usuários (autocadastro e criação pelo administrador) | Implementado |
| **RF02** | Autenticação de usuários com JWT | Implementado |
| **RF03** | Abertura de chamado pelo próprio cliente ou pela equipe em nome dele | Implementado |
| **RF04** | Alteração de status do chamado respeitando o ciclo de vida | Implementado |
| **RF05** | Listagem de chamados com filtro por prioridade, status, categoria, técnico e busca textual | Implementado |
| **RF06** | Registro de data e responsável pelo atendimento | Implementado |
| **RF07** | Comentários no chamado, com notas internas visíveis só para a equipe | Implementado |
| **RF08** | Anexos no chamado (envio, download e exclusão) | Implementado |
| **RF09** | Histórico completo de alterações (linha do tempo) | Implementado |
| **RF10** | Prazo de atendimento (SLA) calculado pela prioridade, com alerta de vencimento | Implementado |
| **RF11** | Avaliação do atendimento pelo cliente após a resolução | Implementado |
| **RF12** | Painel e relatórios com indicadores filtrados pelo perfil do usuário | Implementado |
| **RF13** | Recuperação de senha por link de uso único | Implementado |

### Fluxo do chamado

```
                  ┌──────────────────────── reabertura ─────────────────────┐
                  ▼                                                          │
  ABERTO ──► EM ANDAMENTO ──► AGUARDANDO CLIENTE ──► RESOLVIDO ──► ENCERRADO ┘
     │             │                   │                 │
     └─────────────┴───────────────────┴─────────────────┘
                        CANCELADO
```

O servidor recusa transições fora desse fluxo (por exemplo, sair de *Aberto*
direto para *Encerrado*, ou reabrir um chamado cancelado).

### O que cada perfil pode fazer

| Ação | Cliente | Técnico | Admin |
|---|:---:|:---:|:---:|
| Abrir chamado para si | ✔ | ✔ | ✔ |
| Abrir chamado em nome de outro | — | ✔ | ✔ |
| Ver os próprios chamados | ✔ | ✔ | ✔ |
| Ver chamados de terceiros | — | ✔ | ✔ |
| Comentar | ✔ | ✔ | ✔ |
| Escrever nota interna | — | ✔ | ✔ |
| Anexar arquivos | ✔ | ✔ | ✔ |
| Assumir / atribuir técnico | — | ✔ | ✔ |
| Editar dados do chamado | — | ✔ | ✔ |
| Marcar como resolvido | — | ✔ | ✔ |
| Cancelar chamado não atendido | ✔ (o seu) | ✔ | ✔ |
| Confirmar solução / reabrir | ✔ (o seu) | ✔ | ✔ |
| Avaliar o atendimento | ✔ (o seu) | — | — |
| Fila de atendimento e relatórios | — | ✔ | ✔ |
| Gestão de usuários e perfis | — | — | ✔ |
| Excluir chamado | — | — | ✔ |

---

## 5. Requisitos Não Funcionais (RNF)

- **RNF01** – O sistema deve utilizar arquitetura REST.
- **RNF02** – O sistema deve utilizar autenticação JWT.
- **RNF03** – O sistema deve utilizar banco de dados relacional PostgreSQL com integridade referencial.
- **RNF04** – O frontend deve ser SPA (Single Page Application).
- **RNF05** – O sistema deve possuir responsividade básica.
- **RNF06** – O código deve seguir padrão MVC.

---

## 6. Principais Tecnologias

### Backend

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.3 | Framework base / REST |
| Spring Data JPA | — | Persistência / ORM |
| Spring Security | — | Autenticação e autorização |
| JJWT | 0.12.6 | Geração e validação de tokens JWT |
| Hibernate | — | Mapeamento objeto-relacional |
| Lombok | — | Redução de boilerplate |
| Springdoc OpenAPI | 2.8.8 | Documentação automática (Swagger UI) |
| PostgreSQL | — | Banco de dados relacional |
| Gradle | — | Build e gerenciamento de dependências |

### Frontend

| Tecnologia | Versão | Função |
|---|---|---|
| Angular | 21 | Framework SPA (componentes standalone e signals) |
| TypeScript | 5.9 | Tipagem estática |
| RxJS | 7.8 | Fluxos assíncronos (HTTP, debounce da busca) |
| Chart.js | 4.5 | Gráficos do painel e dos relatórios |
| SCSS | — | Estilos, sem framework de UI externo |
| Vitest | 4 | Testes unitários |
| nginx | alpine | Serve o build e repassa `/api` para o backend |

---

## 7. Estrutura do Projeto

```
helpdesk-java-angular/
├── docker-compose.yml
├── README.md
│
├── backend/helpdesk/
│   ├── database/
│   │   ├── schema.sql          # cria o banco do zero
│   │   ├── data.sql            # massa de demonstração
│   │   └── migration-v2.sql    # atualiza um banco já existente
│   │
│   └── src/main/java/com/murilo/helpdesk/
│       ├── config/             # SecurityConfig, OpenApiConfig
│       ├── controller/         # Auth, Chamado, Comentario, Anexo, Avaliacao,
│       │                       # Usuario, Dashboard, Metadados
│       ├── dto/
│       │   ├── request/        # entradas validadas + ChamadoFiltro
│       │   └── response/       # saídas da API
│       ├── exception/          # ApiError + GlobalExceptionHandler
│       ├── model/
│       │   ├── enums/          # Status, Prioridade, Categoria, Perfil...
│       │   └── ...             # Usuario, Chamado, Comentario, Anexo,
│       │                       # Avaliacao, HistoricoChamado, PasswordResetToken
│       ├── repository/
│       │   └── spec/           # ChamadoSpecs (filtros dinâmicos)
│       ├── security/           # JWT, UserDetails, handlers 401/403
│       ├── service/            # regras de negócio
│       └── util/Mapper.java    # entidade → DTO
│
└── frontend/
    ├── nginx.conf              # SPA + proxy /api → backend
    └── src/app/
        ├── core/
        │   ├── guards/         # authGuard, adminGuard, atendenteGuard
        │   ├── interceptors/   # jwt, auth-error
        │   ├── models/         # tipos espelhando os DTOs
        │   └── services/       # auth, chamado, usuario, dashboard,
        │                       # toast, confirm
        ├── shared/             # toast, diálogo de confirmação
        ├── layout/sidebar/     # navegação (com menu mobile)
        └── features/
            ├── auth/           # login, cadastro, esqueci/redefinir senha
            ├── chamados/       # lista, detalhe, novo, edição
            ├── dashboard/
            ├── relatorios/
            ├── usuarios/
            ├── perfil/
            └── erros/          # 404 e acesso negado
```

---

## 8. Endpoints da API

> Base: `http://localhost:9090/api` · Documentação interativa em
> `http://localhost:9090/api/swagger-ui.html` (use o botão **Authorize** com o token do login).

### Autenticação e conta (`/v1/auth`)

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/v1/auth/login` | Público | Autentica e devolve o token JWT |
| POST | `/v1/auth/register` | Público | Autocadastro (sempre com perfil CLIENTE) |
| POST | `/v1/auth/recuperar-senha` | Público | Gera link de redefinição (uso único, 30 min) |
| POST | `/v1/auth/redefinir-senha` | Público | Define nova senha a partir do token |
| GET | `/v1/auth/me` | Autenticado | Dados do próprio usuário |
| PUT | `/v1/auth/me` | Autenticado | Edita os próprios dados |
| POST | `/v1/auth/alterar-senha` | Autenticado | Troca a senha (exige a senha atual) |

### Chamados (`/v1/chamados`)

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/v1/chamados` | Autenticado | Lista paginada e filtrada (cliente vê só os seus) |
| GET | `/v1/chamados/{id}` | Dono ou equipe | Detalhe do chamado |
| POST | `/v1/chamados` | Autenticado | Abre um chamado |
| PUT | `/v1/chamados/{id}` | Técnico/Admin | Edita título, descrição, prioridade, categoria |
| PATCH | `/v1/chamados/{id}/status/{status}` | Conforme o papel | Move o chamado no fluxo |
| PATCH | `/v1/chamados/{id}/assumir` | Técnico/Admin | Assume o atendimento |
| PATCH | `/v1/chamados/{id}/tecnico` | Técnico/Admin | Atribui ou remove o responsável |
| GET | `/v1/chamados/{id}/historico` | Dono ou equipe | Linha do tempo |
| DELETE | `/v1/chamados/{id}` | Admin | Exclui o chamado e seus dependentes |

Filtros aceitos em `GET /v1/chamados` (todos opcionais, resolvidos no banco):
`q`, `status`, `prioridade`, `categoria`, `tecnicoId`, `clienteId`, `semTecnico`,
`slaVencido`, `apenasPendentes`, `dataInicio`, `dataFim`, `page`, `size`, `sort`.

### Conversa, anexos e avaliação

| Método | Endpoint | Descrição |
|---|---|---|
| GET / POST | `/v1/chamados/{id}/comentarios` | Lista e cria comentários (notas internas só para a equipe) |
| PUT / DELETE | `/v1/chamados/{id}/comentarios/{cid}` | Edita o próprio comentário; exclui (autor ou admin) |
| GET / POST | `/v1/chamados/{id}/anexos` | Lista e envia arquivos (até 10 MB) |
| GET | `/v1/chamados/{id}/anexos/{aid}/download` | Baixa o arquivo |
| DELETE | `/v1/chamados/{id}/anexos/{aid}` | Remove o anexo (autor ou admin) |
| GET / POST | `/v1/chamados/{id}/avaliacao` | Consulta e registra a avaliação (cliente do chamado) |

### Usuários, painel e domínio

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/v1/usuarios` | Técnico/Admin | Lista com filtros `perfil`, `ativo`, `q` |
| GET | `/v1/usuarios/{id}` | Técnico/Admin | Busca por ID |
| POST | `/v1/usuarios` | Admin | Cria usuário |
| PUT | `/v1/usuarios/{id}` | Admin | Atualiza usuário |
| PATCH | `/v1/usuarios/{id}/perfis` | Admin | Altera perfis de acesso |
| PATCH | `/v1/usuarios/{id}/situacao?ativo=` | Admin | Ativa/desativa (mantém o histórico) |
| DELETE | `/v1/usuarios/{id}` | Admin | Exclui — bloqueado se houver chamados vinculados |
| GET | `/v1/dashboard/stats` | Autenticado | Indicadores já filtrados pelo perfil |
| GET | `/v1/metadados` | Autenticado | Status, prioridades, categorias e perfis |

### Formato de erro

Toda falha devolve o mesmo corpo, com a mensagem pronta para exibição:

```json
{
  "timestamp": "2026-08-05T14:32:10.123",
  "status": 404,
  "error": "Not Found",
  "message": "Chamado não encontrado(a). ID: 99",
  "path": "/api/v1/chamados/99"
}
```

| Código | Quando ocorre |
|---|---|
| 400 | Validação de campo ou regra de negócio |
| 401 | Sem token, token expirado ou credenciais inválidas |
| 403 | Autenticado, mas sem permissão sobre o recurso |
| 404 | Recurso inexistente |
| 409 | Conflito (e-mail duplicado, chamado já avaliado, vínculo existente) |
| 413 | Arquivo acima de 10 MB |

---

## 9. Como subir o projeto completo com Docker Compose

> Sobe **banco de dados + backend + frontend** com um único comando. Não é necessário ter Java, Node ou PostgreSQL instalados localmente.

### Pré-requisitos

| Sistema | O que instalar |
|---|---|
| **Windows 11** | [Docker Desktop para Windows](https://www.docker.com/products/docker-desktop/) (inclui Docker Compose) |
| **Arch Linux** | `docker` + `docker-compose` (veja abaixo) |

#### Arch Linux — instalação do Docker

```bash
sudo pacman -S docker docker-compose
sudo systemctl enable --now docker
sudo usermod -aG docker $USER   # evita usar sudo a cada comando
newgrp docker                   # aplica o grupo sem reiniciar
```

### Subindo tudo

Execute na **raiz do projeto** (onde está o `docker-compose.yml`):

**Windows 11 (PowerShell ou CMD):**
```powershell
docker compose up --build
```

**Arch Linux (terminal):**
```bash
docker compose up --build
```

O comando `--build` reconstrói as imagens a cada execução. Nas próximas vezes que não houver mudança de código, pode omiti-lo:

```bash
docker compose up
```

### O que sobe

| Serviço | URL de acesso | Descrição |
|---|---|---|
| Frontend (Angular) | http://localhost:4200 | Interface web |
| Backend (Spring Boot) | http://localhost:9090/api | API REST |
| Swagger UI | http://localhost:9090/api/swagger-ui/index.html | Documentação interativa |
| PostgreSQL | `localhost:5433` | Banco de dados (acesso direto via psql/DBeaver) |

> O banco é inicializado automaticamente com `schema.sql` e `data.sql` na primeira vez que o volume é criado.

### Contas de demonstração

O `data.sql` cria três perfis para explorar o sistema. A senha de todas é **`123456`**
e a tela de login tem atalhos para preenchê-las.

| Perfil | E-mail | O que enxerga |
|---|---|---|
| Administrador | `admin@helpdesk.com` | Tudo: gestão de usuários, relatórios e exclusões |
| Técnico | `tecnico@helpdesk.com` | Fila de atendimento, todos os chamados e relatórios |
| Cliente | `cliente@helpdesk.com` | Apenas os próprios chamados |

A massa inclui 12 chamados cobrindo todos os status, chamados na fila sem técnico,
um com prazo estourado, conversas com notas internas, histórico e avaliações —
o suficiente para o painel e os relatórios já nascerem com conteúdo.

### Atualizando um banco que já existe

Os scripts de `docker-entrypoint-initdb.d` **só rodam com o volume vazio**. Se você
já tinha o banco da versão anterior, aplique a migração (que preserva os dados):

```bash
docker compose exec -T db psql -U postgres -d helpdesk \
  < backend/helpdesk/database/migration-v2.sql
```

Se preferir recomeçar do zero, use `docker compose down -v` e suba novamente.

### Parar os serviços

```bash
# Para os containers sem remover dados
docker compose down

# Para e remove também os volumes (apaga o banco)
docker compose down -v
```

### Reconstruir apenas um serviço

```bash
docker compose up --build backend
docker compose up --build frontend
```

### Ver logs em tempo real

```bash
# Todos os serviços
docker compose logs -f

# Apenas o backend
docker compose logs -f backend
```

### Solução de problemas comuns

| Problema | Solução |
|---|---|
| Porta já em uso (9090, 4200, 5433) | Pare o processo que usa a porta ou mude o mapeamento no `docker-compose.yml` |
| Backend não conecta ao banco | Aguarde o healthcheck do `db` passar; use `docker compose logs db` para verificar |
| Arch Linux: `permission denied` no Docker | Execute `sudo usermod -aG docker $USER` e reinicie a sessão |
| Windows: WSL 2 não habilitado | No Docker Desktop, vá em *Settings → General* e marque "Use the WSL 2 based engine" |

---

## 10. Como configurar o banco de dados (PostgreSQL via Docker)

> **Pré-requisito:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução.  
> Funciona da mesma forma no **Windows 11** e no **Pop OS**.

### Subir o container do PostgreSQL

**Pop OS (terminal):**
```bash
docker run -d \
  --name helpdesk-db \
  -e POSTGRES_DB=helpdesk \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

**Windows 11 (PowerShell ou CMD):**
```cmd
docker run -d ^
  --name helpdesk-db ^
  -e POSTGRES_DB=helpdesk ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=postgres ^
  -p 5432:5432 ^
  postgres:16
```

> O `^` é a quebra de linha do Windows. Você também pode escrever tudo em uma linha só.

### Carregar o schema e os dados iniciais

**Pop OS:**
```bash
# Executar a partir da raiz do projeto
docker exec -i helpdesk-db psql -U postgres -d helpdesk < backend/helpdesk/database/schema.sql
docker exec -i helpdesk-db psql -U postgres -d helpdesk < backend/helpdesk/database/data.sql
```

**Windows 11 (PowerShell):**
```powershell
# Executar a partir da raiz do projeto
Get-Content backend\helpdesk\database\schema.sql | docker exec -i helpdesk-db psql -U postgres -d helpdesk
Get-Content backend\helpdesk\database\data.sql   | docker exec -i helpdesk-db psql -U postgres -d helpdesk
```

### Gerenciar o container

| Ação | Comando |
|---|---|
| Verificar se está rodando | `docker ps` |
| Parar | `docker stop helpdesk-db` |
| Reiniciar | `docker start helpdesk-db` |
| Ver logs | `docker logs helpdesk-db` |
| Remover | `docker rm -f helpdesk-db` |

### Pop OS — instalação nativa (sem Docker)

Caso prefira instalar o PostgreSQL diretamente no sistema:

```bash
sudo apt update && sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable --now postgresql
sudo -u postgres psql -c "CREATE DATABASE helpdesk;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
sudo -u postgres psql -d helpdesk < backend/helpdesk/database/schema.sql
sudo -u postgres psql -d helpdesk < backend/helpdesk/database/data.sql
```

---

## 11. Como executar o backend

**Pré-requisitos:** Java 21+, banco de dados rodando (seção 9), porta **9090** livre.

**Pop OS / Linux:**
```bash
cd backend/helpdesk
./gradlew bootRun
```

**Windows 11 (PowerShell ou CMD):**
```cmd
cd backend\helpdesk
gradlew.bat bootRun
```

Após iniciar:

| Recurso | URL |
|---|---|
| API REST | `http://localhost:9090/api` |
| Swagger UI | `http://localhost:9090/api/swagger-ui.html` |

---

## 12. Histórico de Melhorias

### build.gradle — Dependências adicionadas

O arquivo original continha apenas o `spring-boot-starter` básico, o que impedia o projeto de compilar e executar. Foram adicionados:

| Dependência | Motivo |
|---|---|
| `spring-boot-starter-web` | Habilita endpoints REST (`@RestController`, etc.) |
| `spring-boot-starter-data-jpa` | Habilita repositórios JPA e Hibernate |
| `spring-boot-starter-validation` | Habilita `@NotNull`, `@Size`, `@Email`, `@Valid` |
| `spring-boot-starter-security` | Base para autenticação e JWT |
| `jjwt-api/impl/jackson 0.12.6` | Geração e validação de tokens JWT |
| `postgresql` (runtimeOnly) | Driver JDBC para PostgreSQL |
| `lombok` (compileOnly + annotationProcessor) | Anotações como `@Data` e `@Builder` requerem o annotation processor |
| `springdoc-openapi-starter-webmvc-ui 2.8.8` | Swagger UI — as anotações `@Operation` já existiam nos controllers |
| `spring-security-test` | Suporte a testes com Spring Security |

### Estrutura de pacotes — Migração para o classpath

Todos os arquivos Java estavam em `backend/helpdesk/models/`, `repository/`, `service/` e `controller/` — fora da árvore de fontes do Gradle. O IDE não conseguia resolver imports e o projeto não compilava.

Todos os arquivos foram movidos para o caminho correto:
`backend/helpdesk/src/main/java/com/murilo/helpdesk/`

### Correções de bugs

| Arquivo | Bug | Correção |
|---|---|---|
| `Chamado.java` | Getters/setters com tipo `LocalDate` incorreto (campo é `LocalDateTime`) | Removidos — Lombok já os gera corretamente |
| `Usuario.java` | Faltava campo `departamento` exigido pelo `mappedBy` de `DepartamentoEntity` | Adicionado `@ManyToOne DepartamentoEntity departamento` |
| `HistoricoChamadoRepository.java` | Parâmetro `tipoAlteracao` era `String` em vez do enum `TipoAlteracao` | Corrigido para o tipo correto |
| `UsuarioService.java` | `@Autowired(required=false)` no `PasswordEncoder` — com Spring Security no classpath o bean sempre existe | Substituído por `@RequiredArgsConstructor` com injeção obrigatória |
| `UsuarioController.java` / `ChamadoController.java` | Path `/api/v1/...` + `context-path=/api` gerava `/api/api/v1/...` | Paths corrigidos para `/v1/...` |
| Todos os models | `@Data` sem customização em entidades JPA causa problemas de `equals`/`hashCode` com lazy loading | Adicionados `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` e `@ToString(exclude={...})` em todas as entidades |

### Revisão v2 — segurança, regras e telas

#### Falhas de segurança corrigidas

| Problema | Impacto | Correção |
|---|---|---|
| `GET /v1/chamados/{id}` não verificava o dono | Qualquer cliente lia o chamado de qualquer outro trocando o ID na URL | `garantirAcessoDeLeitura` aplicado na busca e reaproveitado por comentários, anexos e avaliação |
| `/v1/dashboard/stats` era global | Cliente via os números da empresa inteira e os 5 chamados mais recentes de terceiros | Indicadores calculados por papel (`GLOBAL`, `TECNICO`, `CLIENTE`) |
| Requisição sem token respondia 403 | O front não distinguia "sessão caiu" de "sem permissão" e deslogava em ambos | `AuthenticationEntryPoint` devolve 401; o 403 passou a só exibir aviso |
| `PATCH /usuarios/{id}/perfis` aceitava qualquer inteiro | `Perfil.values()[codigo]` estourava e o usuário ficava sem conseguir logar | Validação por `Perfil.codigoValido` + `CHECK` no banco |
| Troca de senha não pedia a senha atual | Uma sessão esquecida aberta permitia assumir a conta | `/v1/auth/alterar-senha` exige a senha atual |
| Anexos não existiam; ao criar, o nome do arquivo do cliente seria usado no disco | Risco de path traversal | Arquivo salvo com nome gerado (UUID) e caminho normalizado |

#### Bugs funcionais corrigidos

| Onde | Problema | Correção |
|---|---|---|
| Tela de perfil | Chamava `PUT /v1/usuarios/{id}`, restrito a ADMIN — cliente e técnico tomavam 403 e eram deslogados ao salvar | Passou a usar `/v1/auth/me` (autosserviço) |
| Cadastro de usuário (admin) | O campo de perfis era montado como `[[[codigos]]]` e enviava `perfis: [[1]]` | Substituído por três checkboxes convertidos em códigos |
| Lista de chamados | Filtro e busca rodavam só sobre a página carregada: "Encerrados" só achava o que estivesse entre os 10 primeiros | Filtros resolvidos no banco via `Specification` |
| Sessão | `isLoggedIn()` só checava se havia string no `localStorage` | Passou a validar a expiração do JWT |
| Rota inexistente | O curinga `**` mandava para o login, parecendo queda de sessão | Telas dedicadas de 404 e 403 |
| Erros da API | "Não encontrado" e "e-mail duplicado" chegavam como HTTP 500 | `GlobalExceptionHandler` com 400/401/403/404/409/413 |
| Exclusões | Excluir usuário com chamados ou chamado com dependentes quebrava por FK | Bloqueio com mensagem orientando desativar; exclusão de chamado remove os dependentes |
| Login | Link "Esqueci minha senha" apontava para `#` e havia um botão de SSO que não fazia nada | Fluxo real de recuperação; botão falso removido |

#### O que passou a existir

- **Ciclo de vida completo** do chamado com transições validadas no servidor
- **SLA por prioridade** (72h/24h/8h/2h), com prazo, alerta de vencimento e fila
- **Protocolo** legível (`CH-2026-000001`) e **categoria** do problema
- **Comentários** com notas internas, **anexos**, **linha do tempo** e **avaliação**
- **Telas novas:** fila de atendimento, atribuídos a mim, edição de chamado,
  relatórios com exportação CSV, recuperação/redefinição de senha, 404 e acesso negado
- **Interface:** menu lateral responsivo, avisos não bloqueantes no lugar de `alert()`,
  diálogo de confirmação próprio, contraste de texto ajustado e locale pt-BR

---

## 13. CI/CD — Integração e Entrega Contínua

O projeto utiliza **GitHub Actions** com dois pipelines independentes, acionados automaticamente em `push` e `pull_request` para a branch `main`.

### Pipeline do Backend (`ci-backend.yml`)

| Etapa | O que faz |
|---|---|
| Checkout | Clona o repositório |
| Setup JDK 21 | Configura o Java 21 (Temurin) |
| Cache Gradle | Reutiliza cache de dependências entre execuções |
| `./gradlew test` | Executa todos os testes unitários com H2 em memória |
| Publicar relatório | Sobe o relatório HTML de testes como artefato |
| `./gradlew build -x test` | Gera o JAR de produção |
| Publicar JAR | Disponibiliza o `.jar` como artefato para download |

> O pipeline usa banco **H2 em memória** para os testes — nenhuma dependência de PostgreSQL em CI.

### Pipeline do Frontend (`ci-frontend.yml`)

| Etapa | O que faz |
|---|---|
| Checkout | Clona o repositório |
| Setup Node 22 | Configura o Node.js com cache de `npm` |
| `npm ci` | Instala dependências de forma reproduzível |
| `npm run build` | Build de produção com Angular CLI |
| Publicar dist | Sobe os arquivos compilados como artefato |

### Localização dos arquivos de workflow

```
.github/
└── workflows/
    ├── ci-backend.yml   # Pipeline Java/Gradle
    └── ci-frontend.yml  # Pipeline Angular/Node
```

---

## 14. Testes

### Backend — 39 testes

Todos rodam sem subir o Spring context (Mockito + AssertJ), exceto o teste de
carga da aplicação, que usa H2 em memória.

```
src/test/java/com/murilo/helpdesk/
├── service/
│   ├── ChamadoServiceTest.java   # acesso, criação, fluxo de status, SLA, exclusão
│   └── UsuarioServiceTest.java   # perfis, e-mail duplicado, proteções, senha
├── model/enums/
│   └── StatusTest.java           # conversão por código e transições válidas
├── security/
│   └── JwtServiceTest.java       # geração e validação do token
└── HelpdeskApplicationTests.java # sobe o contexto completo (H2)
```

Cobrem, entre outros: cliente não acessa chamado de terceiro, cliente não marca
o próprio chamado como resolvido, transição inválida é recusada, reabertura
limpa a data de fechamento, o último administrador ativo não pode ser removido,
e a troca de senha exige a senha atual.

```bash
cd backend/helpdesk
./gradlew test
# Relatório HTML em build/reports/tests/test/index.html
```

### Frontend — 26 testes

```
src/app/
├── app.spec.ts                       # shell da aplicação
├── core/services/auth.service.spec.ts # expiração do JWT, papéis, logout
└── features/chamados/chamado-ui.spec.ts # SLA, badges, transições por papel
```

```bash
cd frontend
npm run test:ci
```

---

## 15. Status do Projeto

| Componente | Status |
|---|---|
| Modelo de domínio (entidades, enums, SLA, protocolo) | Completo |
| Segurança (JWT, papéis, 401/403, autorização por recurso) | Completo |
| Tratamento de erros da API | Completo |
| Chamados (ciclo de vida, filtros, atribuição, SLA) | Completo |
| Comentários, anexos, histórico e avaliação | Completo |
| Usuários e perfis (com ativação/desativação) | Completo |
| Painel e relatórios por perfil | Completo |
| Recuperação de senha | Completo (link no log; sem envio de e-mail) |
| Frontend Angular — 13 telas | Completo |
| Testes | 39 no backend · 26 no frontend |
| CI/CD (GitHub Actions) | 2 pipelines, ambos com testes |
| Docker Compose (banco + API + web) | Completo |

### Limitações conhecidas

| Item | Situação |
|---|---|
| Envio de e-mail | Não há servidor SMTP configurado. O link de redefinição vai para o log da aplicação e, em desenvolvimento, também na resposta (`app.reset-senha.expor-link`). Desligue em produção. |
| Departamentos | A entidade e a massa de dados existem e os usuários são vinculados a um departamento, mas não há tela de manutenção nem roteamento de chamados por departamento. |
| Armazenamento de anexos | Os arquivos ficam em disco local (volume `uploads_data`). Para vários nós, trocar por um storage compartilhado (S3 ou equivalente). |
| Notificações | Não há aviso por e-mail ou push quando o chamado muda de status; o acompanhamento é pelo painel. |
| `JWT_SECRET` padrão | O valor de desenvolvimento está versionado. Defina a variável de ambiente em qualquer ambiente real. |
