-- ================================================
-- HELPDESK — Schema Principal
-- Compatível com as entidades JPA do backend Java.
--
-- Para recriar do zero em dev:
--   psql -U postgres -d helpdesk -f schema.sql
-- ================================================

-- ================================================
-- DROP (ordem inversa para respeitar FKs)
-- ================================================
DROP TABLE IF EXISTS historico_chamados  CASCADE;
DROP TABLE IF EXISTS avaliacao_aspectos  CASCADE;
DROP TABLE IF EXISTS avaliacoes          CASCADE;
DROP TABLE IF EXISTS anexos              CASCADE;
DROP TABLE IF EXISTS comentarios         CASCADE;
DROP TABLE IF EXISTS chamados            CASCADE;
DROP TABLE IF EXISTS usuario_perfis      CASCADE;
DROP TABLE IF EXISTS usuarios            CASCADE;
DROP TABLE IF EXISTS departamentos       CASCADE;


-- ================================================
-- DEPARTAMENTOS
-- Criado antes de usuarios por causa da FK cruzada.
-- A FK de gerente_id é adicionada via ALTER TABLE depois.
-- ================================================
CREATE TABLE departamentos (
    id                BIGSERIAL    PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL UNIQUE,
    descricao         TEXT,
    gerente_id        BIGINT,                        -- FK adicionada após criar usuarios
    email_contato     VARCHAR(100),
    telefone_contato  VARCHAR(20),
    localizacao       VARCHAR(100),
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP
);


-- ================================================
-- USUÁRIOS
-- Perfis gerenciados via tabela usuario_perfis.
-- FK de departamento_id adicionada via ALTER TABLE depois.
-- ================================================
CREATE TABLE usuarios (
    id                BIGSERIAL    PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    email             VARCHAR(100) NOT NULL UNIQUE,
    senha             VARCHAR(255) NOT NULL,
    departamento_id   BIGINT,                        -- FK adicionada após criar departamentos
    data_criacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao  TIMESTAMP
);

-- Tabela de perfis do usuário (ADMIN=0, CLIENTE=1, TECNICO=2)
CREATE TABLE usuario_perfis (
    usuario_id  BIGINT  NOT NULL,
    perfil      INTEGER NOT NULL,
    PRIMARY KEY (usuario_id, perfil),
    CONSTRAINT fk_perfis_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);


-- ================================================
-- FKs CRUZADAS entre usuarios e departamentos
-- ================================================
ALTER TABLE departamentos
    ADD CONSTRAINT fk_departamento_gerente
    FOREIGN KEY (gerente_id) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuario_departamento
    FOREIGN KEY (departamento_id) REFERENCES departamentos(id) ON DELETE SET NULL;


-- ================================================
-- CHAMADOS
-- status:    0=ABERTO | 1=EM_ANDAMENTO | 2=ENCERRADO
-- prioridade: 0=BAIXA | 1=MEDIA | 2=ALTA
-- ================================================
CREATE TABLE chamados (
    id                BIGSERIAL    PRIMARY KEY,
    titulo            VARCHAR(200) NOT NULL,
    observacoes       TEXT         NOT NULL,
    status            INTEGER      NOT NULL,
    prioridade        INTEGER      NOT NULL,
    tecnico_id        BIGINT,
    cliente_id        BIGINT       NOT NULL,
    data_abertura     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento   TIMESTAMP,
    data_atualizacao  TIMESTAMP,

    CONSTRAINT fk_chamado_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_chamado_cliente    ON chamados(cliente_id);
CREATE INDEX idx_chamado_tecnico    ON chamados(tecnico_id);
CREATE INDEX idx_chamado_status     ON chamados(status);


-- ================================================
-- COMENTÁRIOS
-- ================================================
CREATE TABLE comentarios (
    id                BIGSERIAL  PRIMARY KEY,
    chamado_id        BIGINT     NOT NULL,
    autor_id          BIGINT     NOT NULL,
    texto             TEXT       NOT NULL,
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


-- ================================================
-- ANEXOS
-- ================================================
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


-- ================================================
-- AVALIAÇÕES
-- nota: 1=Muito Ruim | 2=Ruim | 3=Regular | 4=Bom | 5=Excelente
-- Relação OneToOne com chamado (unique em chamado_id).
-- ================================================
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

-- Aspectos positivos destacados na avaliação (ElementCollection)
CREATE TABLE avaliacao_aspectos (
    avaliacao_id  BIGINT       NOT NULL,
    aspecto       VARCHAR(100) NOT NULL,
    CONSTRAINT fk_aspecto_avaliacao
        FOREIGN KEY (avaliacao_id) REFERENCES avaliacoes(id) ON DELETE CASCADE
);

CREATE INDEX idx_aspecto_avaliacao ON avaliacao_aspectos(avaliacao_id);


-- ================================================
-- HISTÓRICO DE CHAMADOS
-- tipo_alteracao: STATUS | PRIORIDADE | TECNICO | TITULO |
--                 COMENTARIO | FECHAMENTO | REABERTURA | CRIACAO
-- ================================================
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
