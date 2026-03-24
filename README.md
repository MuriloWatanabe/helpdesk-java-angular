# 🖥️ Sistema de Gestão de Chamados (Help Desk)

## 📌 1. Domínio do Problema

Empresas frequentemente enfrentam dificuldades no gerenciamento de chamados técnicos internos, como problemas de rede, computadores, sistemas e acessos.

Quando não há um sistema estruturado, podem ocorrer:

- Perda de solicitações
- Falta de priorização adequada
- Atraso no atendimento
- Ausência de histórico de ocorrências

O sistema proposto tem como objetivo centralizar e organizar os chamados técnicos internos, proporcionando controle, rastreabilidade e melhor gestão do suporte.

---

## 🎯 2. Objetivo do Sistema

Desenvolver um sistema web para:

- Cadastro de usuários
- Abertura de chamados técnicos
- Definição de prioridade
- Acompanhamento de status
- Histórico de atendimentos
- Controle administrativo

---

## 📐 3. Arquitetura do Sistema

### 🔹 Visão de Contexto

![Diagrama de Contexto](backend/helpdesk/docs/architecture/SystemContext-Sistema_de_Gestão_de_Chamados___Contexto.png)

### 🔹 Visão de Containers

![Diagrama de Containers](backend/helpdesk/docs/architecture/Containers-Sistema_de_Gestão_de_Chamados___Containers.png)

### 🔹 Visão de Componentes (Backend)

![Diagrama de Componentes (Backend)](backend/helpdesk/docs/architecture/BackendComponents-Backend___Componentes.png)

---

## ✅ 4. Requisitos Funcionais (RF)

- **RF01** – O sistema deve permitir cadastro de usuários.
- **RF02** – O sistema deve permitir autenticação de usuários.
- **RF03** – O usuário deve poder abrir um chamado.
- **RF04** – O administrador deve poder alterar o status do chamado.
- **RF05** – O sistema deve listar chamados por prioridade.
- **RF06** – O sistema deve registrar data e responsável pelo atendimento.

---

## ⚙️ 5. Requisitos Não Funcionais (RNF)

- **RNF01** – O sistema deve utilizar arquitetura REST.
- **RNF02** – O sistema deve utilizar autenticação JWT.
- **RNF03** – O sistema deve utilizar banco de dados relacional (PostgreSQL) *(em desenvolvimento)*.
- **RNF04** – O frontend deve ser SPA (Single Page Application).
- **RNF05** – O sistema deve possuir responsividade básica.
- **RNF06** – O código deve seguir padrão MVC.

---

## 🛠️ 6. Principais Tecnologias Utilizadas e Justificativas de Escolha

### 🔹 Backend

#### Java 17
A linguagem Java foi escolhida por sua robustez, ampla utilização no mercado corporativo e forte adoção em sistemas empresariais. Sua orientação a objetos e maturidade no ecossistema tornam a tecnologia adequada para o desenvolvimento de aplicações escaláveis e seguras.

#### Spring Boot 4.0
Framework utilizado para simplificar a criação de APIs REST. O Spring Boot reduz configurações manuais, permite injeção de dependências e favorece a aplicação de boas práticas arquiteturais, aumentando a produtividade no desenvolvimento backend.

#### Spring Data JPA
Responsável pela camada de persistência de dados, utilizando o padrão ORM (Object Relational Mapping). Permite o mapeamento entre objetos Java e tabelas do banco de dados, reduzindo a necessidade de consultas SQL manuais e aumentando a produtividade.

#### Spring Security + JWT
Framework utilizado para implementação de autenticação segura e controle de acesso. Garante maior segurança na aplicação, protegendo endpoints e permitindo a definição de regras de autorização.

#### Lombok
Biblioteca que reduz significativamente o código boilerplate (getters, setters, construtores) através de annotations simples. Mantém o código limpo, legível e mais fácil de manter.

#### Springdoc OpenAPI
Ferramenta que gera automaticamente documentação da API através do Swagger UI. Facilita testes, integração frontend/backend e compreensão dos endpoints disponíveis.

---

### 🔹 Banco de Dados

#### PostgreSQL
Banco de dados relacional escolhido *(em desenvolvimento)* por sua confiabilidade, robustez e ampla utilização no mercado. Oferece suporte a transações ACID, integridade referencial e alto desempenho para aplicações corporativas.

---

### 🔹 Frontend

#### Angular 18+
Framework estruturado para desenvolvimento de aplicações do tipo SPA (Single Page Application). Foi escolhido por sua arquitetura modular, organização clara de componentes e forte adoção em sistemas corporativos.

#### TypeScript
Superset do JavaScript que adiciona tipagem estática à linguagem. Sua utilização aumenta a previsibilidade do código, reduz erros e melhora a manutenção do projeto.

#### Bootstrap / Material Design
Frameworks de UI que fornecem componentes profissionais, responsividade automática e facilitam a criação de interfaces modernas e intuitivas.

---

### 🔹 Versionamento

#### Git
Sistema de controle de versão distribuído que permite rastrear alterações no código, facilitando a organização e evolução do projeto.

#### GitHub
Plataforma de hospedagem de repositórios Git utilizada para armazenamento do código, controle de versões e colaboração entre os desenvolvedores.

---

## 📂 7. Estrutura do Projeto

```
helpdesk-java-angular/
│
├── README.md (este arquivo)
├── CONTRIBUICOES.md (resumo de melhorias implementadas)
├── QUICKSTART.md (guia rápido para rodar o projeto)
│
├── 📁 backend/
│   └── 📁 helpdesk/
│       │
│       ├── 📁 src/
│       │   ├── 📁 main/java/com/murilo/helpdesk/
│       │   │   ├── HelpdeskApplication.java
│       │   │   ├── 📁 controller/
│       │   │   │   ├── UsuarioController.java
│       │   │   │   └── ChamadoController.java
│       │   │   ├── 📁 service/
│       │   │   │   ├── UsuarioService.java
│       │   │   │   └── ChamadoService.java
│       │   │   ├── 📁 repository/
│       │   │   │   ├── UsuarioRepository.java
│       │   │   │   └── ChamadoRepository.java
│       │   │   ├── 📁 model/
│       │   │   │   ├── Usuario.java
│       │   │   │   ├── Chamado.java
│       │   │   │   └── 📁 enums/
│       │   │   │       ├── Perfil.java
│       │   │   │       ├── Status.java
│       │   │   │       └── Prioridade.java
│       │   │   └── 📁 config/
│       │   │       └── (configurações do projeto)
│       │   │
│       │   └── 📁 resources/
│       │       └── application.properties
│       │
│       ├── 📁 docs/
│       │   └── 📁 architecture/
│       │       ├── system-context.puml
│       │       ├── containers.puml
│       │       └── components-backend.puml
│       │
│       ├── build.gradle
│       ├── settings.gradle
│       └── gradlew
│
├── 📁 frontend/
│   └── (A ser desenvolvido)
│
```

## 📌 8. Status do Projeto

🚧 Em desenvolvimento.
