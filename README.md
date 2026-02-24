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

## ✅ 3. Requisitos Funcionais (RF)

- **RF01** – O sistema deve permitir cadastro de usuários.
- **RF02** – O sistema deve permitir autenticação de usuários.
- **RF03** – O usuário deve poder abrir um chamado.
- **RF04** – O administrador deve poder alterar o status do chamado.
- **RF05** – O sistema deve listar chamados por prioridade.
- **RF06** – O sistema deve registrar data e responsável pelo atendimento.

---

## ⚙️ 4. Requisitos Não Funcionais (RNF)

- **RNF01** – O sistema deve utilizar arquitetura REST.
- **RNF02** – O sistema deve utilizar autenticação JWT.
- **RNF03** – O sistema deve utilizar banco de dados relacional (PostgreSQL).
- **RNF04** – O frontend deve ser SPA (Single Page Application).
- **RNF05** – O sistema deve possuir responsividade básica.
- **RNF06** – O código deve seguir padrão MVC.

---

## 🛠️ 5. Tecnologias Utilizadas

### 🔹 Backend

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL**
- **Maven**

### 🔹 Frontend

- **Angular**
- **TypeScript**
- **HTML + CSS**

### 🔹 Versionamento

- Git
- GitHub

---

## 📂 6. Estrutura do Projeto
helpdesk-java-angular/
│
├── backend/ → API REST em Spring Boot
├── frontend/ → Aplicação Angular
└── README.md


---

## 👥 7. Organização de Tarefas (Dupla)

### 🔹 Desenvolvedor Backend

- Modelagem do banco de dados
- Criação das entidades
- Implementação de Controllers REST
- Implementação de autenticação JWT
- Testes de API

### 🔹 Desenvolvedor Frontend

- Criação da estrutura Angular
- Desenvolvimento das telas (Login, Cadastro, Chamados)
- Integração com API REST
- Controle de rotas
- Validações no frontend

---

## 🚀 8. Planejamento de Desenvolvimento

### Semana 1
- Estrutura inicial do backend
- Modelagem do banco
- CRUD de Usuário

### Semana 2
- CRUD de Chamado
- Estrutura inicial do Angular

### Semana 3
- Implementação de autenticação
- Integração frontend + backend

### Semana 4
- Ajustes finais
- Testes
- Documentação

---

## 📌 9. Status do Projeto

🚧 Em desenvolvimento.
