package com.murilo.helpdesk.model.enums;

/**
 * Enumeração que define o nível de severidade técnica de um chamado.
 * Diferencia da Prioridade, focando no impacto técnico/quantitativo.
 */
public enum Severidade {
    BAIXA(0, "Baixa", "Problema menor, sem impacto na produtividade"),
    MEDIA(1, "Média", "Problema afeta produtividade de um usuário"),
    ALTA(2, "Alta", "Problema afeta múltiplos usuários ou departamento"),
    CRITICA(3, "Crítica", "Sistema parado, emergência - múltiplos usuários impossibilitados");

    private final Integer codigo;
    private final String descricao;
    private final String detalhe;

    Severidade(Integer codigo, String descricao, String detalhe) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    /**
     * Retorna as horas máximas de resposta baseado na severidade
     * @return Horas para resposta inicial
     */
    public Integer getHorasResposta() {
        return switch (this) {
            case BAIXA -> 48;
            case MEDIA -> 24;
            case ALTA -> 4;
            case CRITICA -> 1;
        };
    }
}
