-- TABELA DE USUÁRIOS
CREATE TABLE usuario (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    senha      VARCHAR(255) NOT NULL,
    tipo       VARCHAR(20)  NOT NULL CHECK (tipo IN ('ADMIN', 'SUPORTE', 'CLIENTE')),
    criado_em  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- TABELA DE STATUS
CREATE TABLE status_chamado (
    id   SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

-- TABELA DE PRIORIDADE
CREATE TABLE prioridade (
    id   SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

-- TABELA DE CHAMADOS
CREATE TABLE chamado (
    id              SERIAL PRIMARY KEY,
    titulo          VARCHAR(200) NOT NULL,
    descricao       TEXT,
    usuario_id      INT NOT NULL,
    responsavel_id  INT,                          -- RF06: responsável pelo atendimento
    status_id       INT NOT NULL,
    prioridade_id   INT NOT NULL,
    data_abertura   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP,

    CONSTRAINT fk_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id) ON DELETE RESTRICT,

    CONSTRAINT fk_responsavel
        FOREIGN KEY (responsavel_id)
        REFERENCES usuario(id) ON DELETE SET NULL,

    CONSTRAINT fk_status
        FOREIGN KEY (status_id)
        REFERENCES status_chamado(id),

    CONSTRAINT fk_prioridade
        FOREIGN KEY (prioridade_id)
        REFERENCES prioridade(id)
);

CREATE INDEX idx_chamado_prioridade ON chamado(prioridade_id);
CREATE INDEX idx_chamado_status     ON chamado(status_id);
CREATE INDEX idx_chamado_usuario    ON chamado(usuario_id);

-- TABELA DE COMENTÁRIOS
CREATE TABLE comentario (
    id               SERIAL PRIMARY KEY,
    mensagem         TEXT NOT NULL,
    chamado_id       INT  NOT NULL,
    usuario_id       INT  NOT NULL,
    data_comentario  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chamado
        FOREIGN KEY (chamado_id)
        REFERENCES chamado(id) ON DELETE CASCADE,

    CONSTRAINT fk_usuario_comentario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id) ON DELETE RESTRICT
);