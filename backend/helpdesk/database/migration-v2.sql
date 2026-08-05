-- ================================================
-- HELPDESK — Migração para a v2
--
-- Aplique em um banco que já está rodando a versão anterior, sem perder dados:
--   psql -U postgres -d helpdesk -f migration-v2.sql
--
-- É idempotente: rodar duas vezes não causa erro.
-- Quem está subindo o banco do zero deve usar schema.sql + data.sql.
-- ================================================

BEGIN;

-- ------------------------------------------------
-- USUÁRIOS: contato, cargo, ativação e último acesso
-- ------------------------------------------------
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS telefone      VARCHAR(20);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cargo         VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ativo         BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ultimo_acesso TIMESTAMP;

-- ------------------------------------------------
-- CHAMADOS: protocolo, categoria e SLA
-- ------------------------------------------------
ALTER TABLE chamados ADD COLUMN IF NOT EXISTS numero                 VARCHAR(20);
ALTER TABLE chamados ADD COLUMN IF NOT EXISTS categoria              INTEGER;
ALTER TABLE chamados ADD COLUMN IF NOT EXISTS prazo_sla              TIMESTAMP;
ALTER TABLE chamados ADD COLUMN IF NOT EXISTS data_primeira_resposta TIMESTAMP;

-- Gera o protocolo dos chamados já existentes, na ordem de abertura.
WITH numerados AS (
    SELECT id,
           'CH-' || EXTRACT(YEAR FROM data_abertura)::INT || '-' ||
           LPAD(ROW_NUMBER() OVER (
                PARTITION BY EXTRACT(YEAR FROM data_abertura)
                ORDER BY data_abertura, id)::TEXT, 6, '0') AS novo_numero
    FROM chamados
    WHERE numero IS NULL
)
UPDATE chamados c
SET numero = n.novo_numero
FROM numerados n
WHERE c.id = n.id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chamados_numero_key'
    ) THEN
        ALTER TABLE chamados ADD CONSTRAINT chamados_numero_key UNIQUE (numero);
    END IF;
END $$;

-- Categoria padrão para o histórico anterior à triagem por categoria.
UPDATE chamados SET categoria = 9 WHERE categoria IS NULL;

-- Prazo de SLA retroativo: BAIXA 72h, MEDIA 24h, ALTA 8h, URGENTE 2h.
UPDATE chamados
SET prazo_sla = data_abertura + (
        CASE prioridade
            WHEN 0 THEN INTERVAL '72 hours'
            WHEN 1 THEN INTERVAL '24 hours'
            WHEN 2 THEN INTERVAL '8 hours'
            ELSE        INTERVAL '2 hours'
        END)
WHERE prazo_sla IS NULL;

-- Novas restrições de faixa (status agora vai até 5, prioridade até 3)
ALTER TABLE chamados DROP CONSTRAINT IF EXISTS ck_chamado_status;
ALTER TABLE chamados DROP CONSTRAINT IF EXISTS ck_chamado_prioridade;
ALTER TABLE chamados ADD  CONSTRAINT ck_chamado_status     CHECK (status BETWEEN 0 AND 5);
ALTER TABLE chamados ADD  CONSTRAINT ck_chamado_prioridade CHECK (prioridade BETWEEN 0 AND 3);

CREATE INDEX IF NOT EXISTS idx_chamado_categoria ON chamados(categoria);
CREATE INDEX IF NOT EXISTS idx_chamado_abertura  ON chamados(data_abertura);

-- ------------------------------------------------
-- COMENTÁRIOS: notas internas
-- ------------------------------------------------
ALTER TABLE comentarios ADD COLUMN IF NOT EXISTS interno BOOLEAN NOT NULL DEFAULT FALSE;

-- ------------------------------------------------
-- PERFIS: bloqueia códigos inválidos
-- ------------------------------------------------
DELETE FROM usuario_perfis WHERE perfil NOT BETWEEN 0 AND 2;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_perfil_valido'
    ) THEN
        ALTER TABLE usuario_perfis ADD CONSTRAINT ck_perfil_valido
            CHECK (perfil BETWEEN 0 AND 2);
    END IF;
END $$;

-- ------------------------------------------------
-- TOKENS DE REDEFINIÇÃO DE SENHA
-- ------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id              BIGSERIAL    PRIMARY KEY,
    token           VARCHAR(100) NOT NULL UNIQUE,
    usuario_id      BIGINT       NOT NULL,
    data_expiracao  TIMESTAMP    NOT NULL,
    data_uso        TIMESTAMP,
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reset_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reset_token   ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_reset_usuario ON password_reset_tokens(usuario_id);

COMMIT;
