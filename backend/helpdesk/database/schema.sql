
-- TABELA DE USUÁRIOS
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- ADMIN, SUPORTE, CLIENTE
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABELA DE STATUS
CREATE TABLE status_chamado (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

-- TABELA DE PRIORIDADE
CREATE TABLE prioridade (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

-- TABELA DE CHAMADOS
CREATE TABLE chamado (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    usuario_id INT NOT NULL,
    status_id INT NOT NULL,
    prioridade_id INT NOT NULL,
    data_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP,

    CONSTRAINT fk_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT fk_status
        FOREIGN KEY (status_id)
        REFERENCES status_chamado(id),

    CONSTRAINT fk_prioridade
        FOREIGN KEY (prioridade_id)
        REFERENCES prioridade(id)
);

-- TABELA DE COMENTÁRIOS
CREATE TABLE comentario (
    id SERIAL PRIMARY KEY,
    mensagem TEXT NOT NULL,
    chamado_id INT NOT NULL,
    usuario_id INT NOT NULL,
    data_comentario TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chamado
        FOREIGN KEY (chamado_id)
        REFERENCES chamado(id),

    CONSTRAINT fk_usuario_comentario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);