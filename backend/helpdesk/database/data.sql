-- ================================================
-- HELPDESK — Dados Iniciais
-- Execute após schema.sql.
--
-- Senha dos usuários abaixo: 123456
-- Hash gerado com BCrypt (strength 10).
-- ================================================

-- Usuário admin padrão
INSERT INTO usuarios (nome, email, senha)
VALUES (
    'Admin',
    'admin@helpdesk.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);

-- Perfil ADMIN (código 0) para o usuário recém-criado
INSERT INTO usuario_perfis (usuario_id, perfil)
VALUES (
    (SELECT id FROM usuarios WHERE email = 'admin@helpdesk.com'),
    0
);
