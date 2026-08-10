package com.murilo.helpdesk.model.enums;


public enum Departamento {
    TI(0, "Tecnologia da Informação", "Departamento de TI"),
    FINANCEIRO(1, "Financeiro", "Departamento Financeiro"),
    RH(2, "Recursos Humanos", "Departamento de RH"),
    VENDAS(3, "Vendas", "Departamento de Vendas"),
    MARKETING(4, "Marketing", "Departamento de Marketing"),
    OPERACIONAL(5, "Operacional", "Departamento Operacional"),
    ADMINISTRATIVO(6, "Administrativo", "Departamento Administrativo"),
    OUTRO(7, "Outro", "Outro Departamento");

    private final Integer codigo;
    private final String nome;
    private final String descricao;

    Departamento(Integer codigo, String nome, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
