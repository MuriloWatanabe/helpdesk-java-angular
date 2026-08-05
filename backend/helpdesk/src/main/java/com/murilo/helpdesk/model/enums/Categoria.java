package com.murilo.helpdesk.model.enums;

import java.util.Arrays;

/** Categoria do problema — usada para triagem e para os relatórios. */
public enum Categoria {

    REDE(0, "Rede e internet"),
    HARDWARE(1, "Hardware"),
    SOFTWARE(2, "Software"),
    ACESSO(3, "Acesso e permissões"),
    EMAIL(4, "E-mail"),
    VPN(5, "VPN / acesso remoto"),
    IMPRESSORA(6, "Impressora"),
    BACKUP(7, "Backup e recuperação"),
    INSTALACAO(8, "Instalação de software"),
    OUTRO(9, "Outro");

    private final Integer codigo;
    private final String descricao;

    Categoria(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }

    public static Categoria fromCodigo(Integer codigo) {
        if (codigo == null) return null;
        return Arrays.stream(values())
                .filter(c -> c.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria inválida: " + codigo));
    }
}
