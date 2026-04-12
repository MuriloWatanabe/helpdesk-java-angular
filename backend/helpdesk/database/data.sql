-- STATUS
INSERT INTO status_chamado (nome) VALUES
('ABERTO'),
('EM ANDAMENTO'),
('FECHADO');

-- PRIORIDADE
INSERT INTO prioridade (nome) VALUES
('BAIXA'),
('MEDIA'),
('ALTA'),
('URGENTE');

-- USUÁRIO PADRÃO ADMIN
-- Senha: 123456 (hash BCrypt gerado pelo Spring Security)
INSERT INTO usuario (nome, email, senha, tipo)
VALUES (
    'Admin',
    'admin@helpdesk.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
);