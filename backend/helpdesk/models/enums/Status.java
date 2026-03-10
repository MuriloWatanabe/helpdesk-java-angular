package com.murilo.helpdesk.model.enums;

public enum Status {
    ABERTO(0, "ABERTO"),
    EM_ANDAMENTO(1, "EM_ANDAMENTO"),
    ENCERRADO(2, "ENCERRADO");

    private Integer codigo;
    private String descricao;

    Status(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}
