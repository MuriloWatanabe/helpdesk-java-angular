

DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS historico_chamados  CASCADE;
DROP TABLE IF EXISTS avaliacao_aspectos  CASCADE;
DROP TABLE IF EXISTS avaliacoes          CASCADE;
DROP TABLE IF EXISTS anexos              CASCADE;
DROP TABLE IF EXISTS comentarios         CASCADE;
DROP TABLE IF EXISTS chamados            CASCADE;
DROP TABLE IF EXISTS usuario_perfis      CASCADE;
DROP TABLE IF EXISTS usuarios            CASCADE;
DROP TABLE IF EXISTS departamentos       CASCADE;


CREATE TABLE departamentos (
    id                BIGSERIAL    PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL UNIQUE,
    descricao         TEXT,
    gerente_id        BIGINT,
    email_contato     VARCHAR(100),
    telefone_contato  VARCHAR(20),
    localizacao       VARCHAR(100),
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP
);


CREATE TABLE usuarios (
    id                BIGSERIAL    PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    email             VARCHAR(100) NOT NULL UNIQUE,
    senha             VARCHAR(255) NOT NULL,
    telefone          VARCHAR(20),
    cargo             VARCHAR(100),
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    departamento_id   BIGINT,
    data_criacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP,
    ultimo_acesso     TIMESTAMP
);


CREATE TABLE usuario_perfis (
    usuario_id  BIGINT  NOT NULL,
    perfil      INTEGER NOT NULL,
    PRIMARY KEY (usuario_id, perfil),
    CONSTRAINT fk_perfis_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,

    CONSTRAINT ck_perfil_valido CHECK (perfil BETWEEN 0 AND 2)
);


ALTER TABLE departamentos
    ADD CONSTRAINT fk_departamento_gerente
    FOREIGN KEY (gerente_id) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuario_departamento
    FOREIGN KEY (departamento_id) REFERENCES departamentos(id) ON DELETE SET NULL;


CREATE TABLE chamados (
    id                     BIGSERIAL    PRIMARY KEY,
    numero                 VARCHAR(20)  UNIQUE,
    titulo                 VARCHAR(200) NOT NULL,
    observacoes            TEXT         NOT NULL,
    status                 INTEGER      NOT NULL,
    prioridade             INTEGER      NOT NULL,
    categoria              INTEGER,
    tecnico_id             BIGINT,
    cliente_id             BIGINT       NOT NULL,
    data_abertura          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento        TIMESTAMP,
    data_atualizacao       TIMESTAMP,
    prazo_sla              TIMESTAMP,
    data_primeira_resposta TIMESTAMP,

    CONSTRAINT fk_chamado_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT ck_chamado_status     CHECK (status BETWEEN 0 AND 5),
    CONSTRAINT ck_chamado_prioridade CHECK (prioridade BETWEEN 0 AND 3)
);

CREATE INDEX idx_chamado_cliente   ON chamados(cliente_id);
CREATE INDEX idx_chamado_tecnico   ON chamados(tecnico_id);
CREATE INDEX idx_chamado_status    ON chamados(status);
CREATE INDEX idx_chamado_categoria ON chamados(categoria);
CREATE INDEX idx_chamado_abertura  ON chamados(data_abertura);


CREATE TABLE comentarios (
    id                BIGSERIAL  PRIMARY KEY,
    chamado_id        BIGINT     NOT NULL,
    autor_id          BIGINT     NOT NULL,
    texto             TEXT       NOT NULL,
    interno           BOOLEAN    NOT NULL DEFAULT FALSE,
    editado           BOOLEAN    NOT NULL DEFAULT FALSE,
    data_criacao      TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP,

    CONSTRAINT fk_comentario_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

CREATE INDEX idx_comentario_chamado ON comentarios(chamado_id);
CREATE INDEX idx_comentario_autor   ON comentarios(autor_id);


CREATE TABLE anexos (
    id               BIGSERIAL    PRIMARY KEY,
    chamado_id       BIGINT       NOT NULL,
    usuario_id       BIGINT       NOT NULL,
    nome_arquivo     VARCHAR(255) NOT NULL,
    caminho_arquivo  TEXT         NOT NULL,
    tamanho          BIGINT       NOT NULL,
    tipo_mime        VARCHAR(100) NOT NULL,
    descricao        TEXT,
    publico          BOOLEAN      NOT NULL DEFAULT TRUE,
    data_upload      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_anexo_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_anexo_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

CREATE INDEX idx_anexo_chamado ON anexos(chamado_id);
CREATE INDEX idx_anexo_usuario ON anexos(usuario_id);


CREATE TABLE avaliacoes (
    id               BIGSERIAL  PRIMARY KEY,
    chamado_id       BIGINT     NOT NULL UNIQUE,
    nota             INTEGER    NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario       TEXT,
    data_avaliacao   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id       BIGINT,

    CONSTRAINT fk_avaliacao_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_avaliacao_chamado ON avaliacoes(chamado_id);
CREATE INDEX idx_avaliacao_usuario ON avaliacoes(usuario_id);


CREATE TABLE avaliacao_aspectos (
    avaliacao_id  BIGINT       NOT NULL,
    aspecto       VARCHAR(100) NOT NULL,
    CONSTRAINT fk_aspecto_avaliacao
        FOREIGN KEY (avaliacao_id) REFERENCES avaliacoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_aspecto_avaliacao ON avaliacao_aspectos(avaliacao_id);


CREATE TABLE historico_chamados (
    id              BIGSERIAL    PRIMARY KEY,
    chamado_id      BIGINT       NOT NULL,
    usuario_id      BIGINT,
    tipo_alteracao  VARCHAR(50)  NOT NULL,
    descricao       VARCHAR(200),
    valor_anterior  TEXT,
    valor_novo      TEXT,
    data_alteracao  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historico_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados(id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_historico_chamado ON historico_chamados(chamado_id);
CREATE INDEX idx_historico_usuario ON historico_chamados(usuario_id);
CREATE INDEX idx_historico_data    ON historico_chamados(data_alteracao);
CREATE INDEX idx_historico_tipo    ON historico_chamados(tipo_alteracao);


CREATE TABLE password_reset_tokens (
    id              BIGSERIAL    PRIMARY KEY,
    token           VARCHAR(100) NOT NULL UNIQUE,
    usuario_id      BIGINT       NOT NULL,
    data_expiracao  TIMESTAMP    NOT NULL,
    data_uso        TIMESTAMP,
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reset_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_reset_token   ON password_reset_tokens(token);
CREATE INDEX idx_reset_usuario ON password_reset_tokens(usuario_id);
