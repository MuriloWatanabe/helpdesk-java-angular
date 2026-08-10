package com.murilo.helpdesk.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


public enum Status {

    ABERTO(0, "Aberto"),
    EM_ANDAMENTO(1, "Em andamento"),
    AGUARDANDO_CLIENTE(3, "Aguardando cliente"),
    RESOLVIDO(4, "Resolvido"),
    ENCERRADO(2, "Encerrado"),
    CANCELADO(5, "Cancelado");

    private final Integer codigo;
    private final String descricao;

    Status(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }

    public static Status fromCodigo(Integer codigo) {
        if (codigo == null) return null;
        return Arrays.stream(values())
                .filter(s -> s.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status inválido: " + codigo));
    }


    public boolean ehFinal() {
        return this == ENCERRADO || this == CANCELADO;
    }


    public boolean ehPendente() {
        return !ehFinal() && this != RESOLVIDO;
    }


    public boolean contaParaSla() {
        return this == ABERTO || this == EM_ANDAMENTO;
    }


    public Set<Status> proximosPermitidos() {
        return switch (this) {
            case ABERTO             -> Set.of(EM_ANDAMENTO, AGUARDANDO_CLIENTE, RESOLVIDO, CANCELADO);
            case EM_ANDAMENTO       -> Set.of(AGUARDANDO_CLIENTE, RESOLVIDO, ABERTO, CANCELADO);
            case AGUARDANDO_CLIENTE -> Set.of(EM_ANDAMENTO, RESOLVIDO, CANCELADO);
            case RESOLVIDO          -> Set.of(ENCERRADO, EM_ANDAMENTO);
            case ENCERRADO          -> Set.of(EM_ANDAMENTO);
            case CANCELADO          -> Set.of();
        };
    }

    public boolean podeIrPara(Status destino) {
        return destino != null && proximosPermitidos().contains(destino);
    }


    public static List<Integer> codigosPendentes() {
        return Arrays.stream(values())
                .filter(Status::ehPendente)
                .map(Status::getCodigo)
                .toList();
    }
}
