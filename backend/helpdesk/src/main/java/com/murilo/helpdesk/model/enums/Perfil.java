package com.murilo.helpdesk.model.enums;

import java.util.Arrays;

public enum Perfil {

    ADMIN(0, "ROLE_ADMIN", "Administrador"),
    CLIENTE(1, "ROLE_CLIENTE", "Cliente"),
    TECNICO(2, "ROLE_TECNICO", "Técnico");

    private final Integer codigo;
    private final String descricao;
    private final String rotulo;

    Perfil(Integer codigo, String descricao, String rotulo) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.rotulo = rotulo;
    }

    public Integer getCodigo() { return codigo; }


    public String getDescricao() { return descricao; }


    public String getRotulo() { return rotulo; }


    public static Perfil fromCodigo(Integer codigo) {
        if (codigo == null) return null;
        return Arrays.stream(values())
                .filter(p -> p.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil inválido: " + codigo));
    }

    public static boolean codigoValido(Integer codigo) {
        return codigo != null && Arrays.stream(values()).anyMatch(p -> p.codigo.equals(codigo));
    }
}
