-- STATUS
INSERT INTO status_chamado (nome) VALUES
('ABERTO'),
('EM ANDAMENTO'),
('FECHADO');

-- PRIORIDADE
INSERT INTO prioridade (nome) VALUES
('BAIXA'),
('MEDIA'),
('ALTA');

-- USUÁRIO PADRÃO
INSERT INTO usuario (nome, email, senha, tipo)
VALUES ('Admin', 'admin@helpdesk.com', '123456', 'ADMIN');