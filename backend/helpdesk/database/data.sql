-- ================================================
-- HELPDESK — Dados Iniciais
-- Execute após schema.sql.
--
-- Senha de todos os usuários: 123456
-- Hash BCrypt (strength 10).
-- ================================================

-- ================================================
-- USUÁRIOS
-- ================================================
INSERT INTO usuarios (nome, email, senha) VALUES
    ('Admin',           'admin@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW'),
    ('Carlos Técnico',  'tecnico@helpdesk.com', '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW'),
    ('Ana Cliente',     'cliente@helpdesk.com', '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW'),
    ('Pedro Santos',    'pedro@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW'),
    ('Julia Almeida',   'julia@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW')
ON CONFLICT (email) DO NOTHING;

-- ================================================
-- PERFIS (0=ADMIN, 1=CLIENTE, 2=TECNICO)
-- ================================================
INSERT INTO usuario_perfis (usuario_id, perfil) VALUES
    ((SELECT id FROM usuarios WHERE email = 'admin@helpdesk.com'),   0),
    ((SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'), 2),
    ((SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'), 1),
    ((SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),   1),
    ((SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),   1)
ON CONFLICT DO NOTHING;

-- ================================================
-- CHAMADOS DE EXEMPLO
-- status:    0=ABERTO | 1=EM_ANDAMENTO | 2=ENCERRADO
-- prioridade: 0=BAIXA | 1=MEDIA | 2=ALTA
-- ================================================
INSERT INTO chamados (titulo, observacoes, status, prioridade, tecnico_id, cliente_id, data_abertura) VALUES
    (
        'Impressora não imprime na rede',
        'A impressora HP do segundo andar parou de imprimir. Já tentei reiniciar o spooler e verificar os cabos. O problema persiste.',
        0, 2,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '2 days'
    ),
    (
        'Sem acesso ao sistema ERP após atualização',
        'Após a atualização do ERP ontem à noite, não consigo mais acessar. Aparece a mensagem "Módulo não licenciado". Necessário urgente.',
        1, 2,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '3 days'
    ),
    (
        'Redefinição de senha do Active Directory',
        'Esqueci minha senha do AD e o portal de auto-serviço não está funcionando. Não consigo fazer login no Windows.',
        0, 1,
        NULL,
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '1 day'
    ),
    (
        'Lentidão na conexão Wi-Fi na sala de reuniões',
        'O Wi-Fi da sala 301 está extremamente lento durante as reuniões. A velocidade cai para menos de 1 Mbps quando há mais de 5 pessoas conectadas.',
        1, 1,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '5 days'
    ),
    (
        'Monitor com tela piscando',
        'O monitor do meu computador fica piscando a cada 2-3 minutos. Já testei com outro cabo HDMI e o problema persiste. Modelo: Dell 24".',
        2, 0,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '7 days'
    ),
    (
        'Instalação do Microsoft Teams',
        'Preciso instalar o Microsoft Teams no meu computador. Não tenho permissão de administrador para instalar programas.',
        0, 0,
        NULL,
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '6 hours'
    ),
    (
        'VPN não conecta ao trabalhar em home office',
        'Estou em home office e o cliente VPN Cisco fica conectando e caindo. Isso impede o acesso aos sistemas internos.',
        1, 2,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '4 days'
    ),
    (
        'Backup automático não executou na sexta-feira',
        'O relatório de backup da sexta-feira (19/04) mostra falha. O arquivo de log indica timeout na conexão com o servidor de backup.',
        2, 1,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '10 days'
    );

-- Atualizar data de fechamento dos chamados encerrados
UPDATE chamados SET data_fechamento = data_abertura + INTERVAL '2 days'
WHERE status = 2;
