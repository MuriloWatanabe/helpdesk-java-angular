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

# 🛠️ Principais Tecnologias Utilizadas e Justificativas de Escolha

## 🔹 Backend

### Java 17
A linguagem Java foi escolhida por sua robustez, ampla utilização no mercado corporativo e forte adoção em sistemas empresariais. Sua orientação a objetos e maturidade no ecossistema tornam a tecnologia adequada para o desenvolvimento de aplicações escaláveis e seguras.

### Spring Boot
Framework utilizado para simplificar a criação de APIs REST. O Spring Boot reduz configurações manuais, permite injeção de dependências e favorece a aplicação de boas práticas arquiteturais, aumentando a produtividade no desenvolvimento backend.

### Spring Data JPA
Responsável pela camada de persistência de dados, utilizando o padrão ORM (Object Relational Mapping). Permite o mapeamento entre objetos Java e tabelas do banco de dados, reduzindo a necessidade de consultas SQL manuais e aumentando a produtividade.

### Spring Security
Framework utilizado para implementação de autenticação e controle de acesso. Garante maior segurança na aplicação, protegendo endpoints e permitindo a definição de regras de autorização.

### PostgreSQL
Banco de dados relacional escolhido por sua confiabilidade, robustez e ampla utilização no mercado. Oferece suporte a transações, integridade referencial e alto desempenho para aplicações corporativas.

### Maven
Ferramenta de gerenciamento de dependências e automação de build. Facilita a organização das bibliotecas utilizadas no projeto e padroniza o processo de compilação e execução.

---

## 🔹 Frontend

### Angular
Framework estruturado para desenvolvimento de aplicações do tipo SPA (Single Page Application). Foi escolhido por sua arquitetura modular, organização clara de componentes e forte adoção em sistemas corporativos.

### TypeScript
Superset do JavaScript que adiciona tipagem estática à linguagem. Sua utilização aumenta a previsibilidade do código, reduz erros e melhora a manutenção do projeto.

### HTML e CSS
Tecnologias base para estruturação e estilização da interface do usuário, responsáveis pela construção da experiência visual da aplicação.

---

## 🔹 Versionamento

### Git
Sistema de controle de versão distribuído que permite rastrear alterações no código, facilitando a organização e evolução do projeto.

### GitHub
Plataforma de hospedagem de repositórios Git utilizada para armazenamento do código, controle de versões e colaboração entre os desenvolvedores.

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
