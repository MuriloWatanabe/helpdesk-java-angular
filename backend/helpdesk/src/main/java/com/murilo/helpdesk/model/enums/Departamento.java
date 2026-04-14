package com.murilo.helpdesk.model.enums;

public enum Departamento {
    TI(0, "Tecnologia da Informação"),
    FINANCEIRO(1, "Financeiro"),
    RH(2, "Recursos Humanos"),
    VENDAS(3, "Vendas"),
    MARKETING(4, "Marketing"),
    OPERACIONAL(5, "Operacional"),
    ADMINISTRATIVO(6, "Administrativo"),
    OUTRO(7, "Outro");

    private final Integer codigo;
    private final String nome;

    Departamento(Integer codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public Integer getCodigo() { return codigo; }
    public String getNome() { return nome; }
}
