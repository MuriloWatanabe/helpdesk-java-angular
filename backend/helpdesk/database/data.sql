

INSERT INTO departamentos (nome, descricao, email_contato, localizacao) VALUES
    ('Tecnologia da Informação', 'Infraestrutura, sistemas e suporte técnico', 'ti@helpdesk.com',      'Sede — 3º andar'),
    ('Financeiro',               'Contas a pagar e receber',                   'financeiro@helpdesk.com', 'Sede — 2º andar'),
    ('Recursos Humanos',         'Gestão de pessoas',                          'rh@helpdesk.com',      'Sede — 2º andar'),
    ('Comercial',                'Vendas e relacionamento',                    'comercial@helpdesk.com', 'Sede — 1º andar')
ON CONFLICT (nome) DO NOTHING;


INSERT INTO usuarios (nome, email, senha, telefone, cargo, ativo, departamento_id) VALUES
    ('Admin do Sistema', 'admin@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 3000-0000', 'Coordenador de TI',    TRUE,  (SELECT id FROM departamentos WHERE nome = 'Tecnologia da Informação')),
    ('Carlos Técnico',   'tecnico@helpdesk.com', '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 3000-0001', 'Analista de Suporte',  TRUE,  (SELECT id FROM departamentos WHERE nome = 'Tecnologia da Informação')),
    ('Lucas Ferreira',   'lucas@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 3000-0002', 'Analista de Redes',    TRUE,  (SELECT id FROM departamentos WHERE nome = 'Tecnologia da Informação')),
    ('Marina Costa',     'marina@helpdesk.com',  '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 3000-0003', 'Analista de Sistemas', TRUE,  (SELECT id FROM departamentos WHERE nome = 'Tecnologia da Informação')),
    ('Roberto Lima',     'roberto@helpdesk.com', '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 3000-0004', 'Técnico de Campo',     FALSE, (SELECT id FROM departamentos WHERE nome = 'Tecnologia da Informação')),
    ('Ana Cliente',      'cliente@helpdesk.com', '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 99000-0001', 'Assistente Financeiro', TRUE, (SELECT id FROM departamentos WHERE nome = 'Financeiro')),
    ('Pedro Santos',     'pedro@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 99000-0002', 'Analista de RH',        TRUE, (SELECT id FROM departamentos WHERE nome = 'Recursos Humanos')),
    ('Julia Almeida',    'julia@helpdesk.com',   '$2b$10$oJoU2XK9Uh.ghqVex/p78umK48wDDMoDMMjo0.2QhJUhXSkkrF9XW', '(11) 99000-0003', 'Executiva de Vendas',   TRUE, (SELECT id FROM departamentos WHERE nome = 'Comercial'))
ON CONFLICT (email) DO NOTHING;


UPDATE departamentos SET gerente_id = (SELECT id FROM usuarios WHERE email = 'admin@helpdesk.com')
WHERE nome = 'Tecnologia da Informação';


INSERT INTO usuario_perfis (usuario_id, perfil) VALUES
    ((SELECT id FROM usuarios WHERE email = 'admin@helpdesk.com'),   0),
    ((SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'), 2),
    ((SELECT id FROM usuarios WHERE email = 'lucas@helpdesk.com'),   2),
    ((SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),  2),
    ((SELECT id FROM usuarios WHERE email = 'roberto@helpdesk.com'), 2),
    ((SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'), 1),
    ((SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),   1),
    ((SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),   1)
ON CONFLICT DO NOTHING;


INSERT INTO chamados (numero, titulo, observacoes, status, prioridade, categoria,
                      tecnico_id, cliente_id, data_abertura) VALUES
    ('CH-2026-000001',
        'Impressora não imprime na rede',
        'A impressora HP do segundo andar parou de imprimir. Já tentei reiniciar o spooler e verificar os cabos. O problema persiste desde ontem.',
        1, 2, 6,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '2 days'),

    ('CH-2026-000002',
        'Sem acesso ao sistema ERP após atualização',
        'Após a atualização do ERP ontem à noite, não consigo mais acessar. Aparece a mensagem "Módulo não licenciado". Bloqueia todo o fechamento do mês.',
        1, 3, 2,
        (SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '3 days'),


    ('CH-2026-000003',
        'Redefinição de senha do Active Directory',
        'Esqueci minha senha do AD e o portal de autoatendimento não está funcionando. Não consigo fazer login no Windows.',
        0, 1, 3,
        NULL,
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '1 day'),

    ('CH-2026-000004',
        'Lentidão na conexão Wi-Fi na sala de reuniões',
        'O Wi-Fi da sala 301 está extremamente lento durante as reuniões. A velocidade cai para menos de 1 Mbps quando há mais de 5 pessoas conectadas.',
        3, 1, 0,
        (SELECT id FROM usuarios WHERE email = 'lucas@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '5 days'),

    ('CH-2026-000005',
        'Monitor com tela piscando',
        'O monitor do meu computador fica piscando a cada 2-3 minutos. Já testei com outro cabo HDMI e o problema persiste. Modelo: Dell 24".',
        2, 0, 1,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '7 days'),


    ('CH-2026-000006',
        'Instalação do Microsoft Teams',
        'Preciso instalar o Microsoft Teams no meu computador. Não tenho permissão de administrador para instalar programas.',
        0, 0, 8,
        NULL,
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '6 hours'),


    ('CH-2026-000007',
        'VPN não conecta ao trabalhar em home office',
        'Estou em home office e o cliente VPN Cisco fica conectando e caindo a cada poucos minutos. Isso impede o acesso a todos os sistemas internos.',
        1, 3, 5,
        (SELECT id FROM usuarios WHERE email = 'lucas@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '4 days'),

    ('CH-2026-000008',
        'Backup automático não executou na sexta-feira',
        'O relatório de backup da sexta-feira mostra falha. O arquivo de log indica timeout na conexão com o servidor de backup.',
        2, 1, 7,
        (SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '10 days'),


    ('CH-2026-000009',
        'E-mails de clientes indo para a caixa de spam',
        'Vários e-mails de clientes estão sendo classificados como spam automaticamente. Já perdi dois prazos por causa disso.',
        4, 2, 4,
        (SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '2 days'),

    ('CH-2026-000010',
        'Notebook não liga após queda de energia',
        'Depois da queda de energia de ontem o notebook não liga mais. O LED de carga acende, mas a tela permanece apagada.',
        1, 2, 1,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
        NOW() - INTERVAL '12 hours'),

    ('CH-2026-000011',
        'Solicitação de segundo monitor',
        'Gostaria de solicitar um segundo monitor para melhorar a produtividade nas análises de planilhas.',
        5, 0, 1,
        NULL,
        (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
        NOW() - INTERVAL '8 days'),

    ('CH-2026-000012',
        'Acesso à pasta compartilhada do Comercial',
        'Preciso de acesso de leitura e escrita à pasta compartilhada do Comercial para atualizar a planilha de metas.',
        2, 1, 3,
        (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
        (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
        NOW() - INTERVAL '15 days')
ON CONFLICT (numero) DO NOTHING;


UPDATE chamados
SET prazo_sla = data_abertura + (
        CASE prioridade
            WHEN 0 THEN INTERVAL '72 hours'
            WHEN 1 THEN INTERVAL '24 hours'
            WHEN 2 THEN INTERVAL '8 hours'
            ELSE        INTERVAL '2 hours'
        END)
WHERE prazo_sla IS NULL;


UPDATE chamados SET data_fechamento = data_abertura + INTERVAL '2 days' WHERE status IN (2, 5) AND data_fechamento IS NULL;
UPDATE chamados SET data_primeira_resposta = data_abertura + INTERVAL '3 hours' WHERE tecnico_id IS NOT NULL;
UPDATE chamados SET data_atualizacao = COALESCE(data_fechamento, NOW());


INSERT INTO comentarios (chamado_id, autor_id, texto, interno, data_criacao) VALUES
    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000001'),
     (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
     'Olá Ana, bom dia! Já estou verificando a fila de impressão no servidor. Pode confirmar se outros colegas do andar também estão sem imprimir?',
     FALSE, NOW() - INTERVAL '2 days' + INTERVAL '3 hours'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000001'),
     (SELECT id FROM usuarios WHERE email = 'cliente@helpdesk.com'),
     'Bom dia! Sim, o pessoal do financeiro também está com o mesmo problema.',
     FALSE, NOW() - INTERVAL '2 days' + INTERVAL '4 hours'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000001'),
     (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
     'Nota interna: spooler travado no servidor PRINT-01. Reiniciar o serviço e agendar troca do driver na próxima janela de manutenção.',
     TRUE, NOW() - INTERVAL '2 days' + INTERVAL '5 hours'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000002'),
     (SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),
     'Pedro, abrimos um chamado com o fornecedor do ERP. A licença do módulo Financeiro não foi renovada na atualização. Previsão de retorno em 24h.',
     FALSE, NOW() - INTERVAL '2 days'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000004'),
     (SELECT id FROM usuarios WHERE email = 'lucas@helpdesk.com'),
     'Ana, instalamos um novo ponto de acesso na sala 301. Pode testar na próxima reunião e nos dizer se melhorou?',
     FALSE, NOW() - INTERVAL '1 day'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000007'),
     (SELECT id FROM usuarios WHERE email = 'lucas@helpdesk.com'),
     'Identificamos instabilidade no concentrador VPN. A equipe de rede está aplicando a correção — mantenho você informada.',
     FALSE, NOW() - INTERVAL '3 days'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000009'),
     (SELECT id FROM usuarios WHERE email = 'marina@helpdesk.com'),
     'Julia, ajustamos as regras de SPF e DKIM do domínio. Os e-mails já estão chegando na caixa de entrada. Pode confirmar do seu lado?',
     FALSE, NOW() - INTERVAL '6 hours'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000005'),
     (SELECT id FROM usuarios WHERE email = 'tecnico@helpdesk.com'),
     'Monitor substituído por um novo modelo. Qualquer problema é só reabrir o chamado.',
     FALSE, NOW() - INTERVAL '5 days')
ON CONFLICT DO NOTHING;


INSERT INTO historico_chamados (chamado_id, usuario_id, tipo_alteracao, descricao, valor_anterior, valor_novo, data_alteracao)
SELECT c.id, c.cliente_id, 'CRIACAO', 'Chamado criado', NULL, NULL, c.data_abertura
FROM chamados c
ON CONFLICT DO NOTHING;

INSERT INTO historico_chamados (chamado_id, usuario_id, tipo_alteracao, descricao, valor_anterior, valor_novo, data_alteracao)
SELECT c.id, c.tecnico_id, 'TECNICO',
       'Técnico alterado de ''—'' para ''' || u.nome || '''', '—', u.nome,
       c.data_abertura + INTERVAL '2 hours'
FROM chamados c
JOIN usuarios u ON u.id = c.tecnico_id
ON CONFLICT DO NOTHING;

INSERT INTO historico_chamados (chamado_id, usuario_id, tipo_alteracao, descricao, valor_anterior, valor_novo, data_alteracao)
SELECT c.id, c.tecnico_id, 'STATUS',
       'Status alterado de ''Aberto'' para ''Em andamento''', 'Aberto', 'Em andamento',
       c.data_abertura + INTERVAL '3 hours'
FROM chamados c
WHERE c.tecnico_id IS NOT NULL AND c.status <> 0
ON CONFLICT DO NOTHING;

INSERT INTO historico_chamados (chamado_id, usuario_id, tipo_alteracao, descricao, valor_anterior, valor_novo, data_alteracao)
SELECT c.id, c.tecnico_id, 'FECHAMENTO', 'Chamado finalizado', NULL, 'ENCERRADO', c.data_fechamento
FROM chamados c
WHERE c.status = 2 AND c.data_fechamento IS NOT NULL
ON CONFLICT DO NOTHING;


INSERT INTO avaliacoes (chamado_id, nota, comentario, usuario_id, data_avaliacao) VALUES
    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000005'), 5,
     'Atendimento muito rápido, o monitor foi trocado no mesmo dia. Excelente!',
     (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
     NOW() - INTERVAL '4 days'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000008'), 4,
     'Problema resolvido, só demorou um pouco para o primeiro retorno.',
     (SELECT id FROM usuarios WHERE email = 'pedro@helpdesk.com'),
     NOW() - INTERVAL '7 days'),

    ((SELECT id FROM chamados WHERE numero = 'CH-2026-000012'), 3,
     'O acesso foi liberado, mas precisei cobrar duas vezes.',
     (SELECT id FROM usuarios WHERE email = 'julia@helpdesk.com'),
     NOW() - INTERVAL '12 days')
ON CONFLICT (chamado_id) DO NOTHING;

INSERT INTO avaliacao_aspectos (avaliacao_id, aspecto)
SELECT a.id, 'Rapidez' FROM avaliacoes a
JOIN chamados c ON c.id = a.chamado_id WHERE c.numero = 'CH-2026-000005';

INSERT INTO avaliacao_aspectos (avaliacao_id, aspecto)
SELECT a.id, 'Cordialidade' FROM avaliacoes a
JOIN chamados c ON c.id = a.chamado_id WHERE c.numero = 'CH-2026-000005';

INSERT INTO avaliacao_aspectos (avaliacao_id, aspecto)
SELECT a.id, 'Solução completa' FROM avaliacoes a
JOIN chamados c ON c.id = a.chamado_id WHERE c.numero = 'CH-2026-000008';
