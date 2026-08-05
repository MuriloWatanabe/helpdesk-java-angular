package com.murilo.helpdesk.model.enums;

import java.util.Arrays;

/**
 * Prioridade do chamado. Cada nível define o prazo de SLA usado para calcular
 * a data limite de atendimento.
 */
public enum Prioridade {

    BAIXA(0,   "Baixa",   72),
    MEDIA(1,   "Média",   24),
    ALTA(2,    "Alta",     8),
    URGENTE(3, "Urgente",  2);

    private final Integer codigo;
    private final String descricao;
    private final int horasSla;

    Prioridade(Integer codigo, String descricao, int horasSla) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.horasSla = horasSla;
    }

    public Integer getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }

    /** Prazo máximo de resolução, em horas, a partir da abertura. */
    public int getHorasSla() { return horasSla; }

    public static Prioridade fromCodigo(Integer codigo) {
        if (codigo == null) return null;
        return Arrays.stream(values())
                .filter(p -> p.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prioridade inválida: " + codigo));
    }
}
