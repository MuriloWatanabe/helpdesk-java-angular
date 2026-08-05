package com.murilo.helpdesk.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Ciclo de vida do chamado.
 *
 * Os códigos 0/1/2 são os originais e permanecem estáveis para não invalidar
 * dados já gravados; os estados novos usam códigos a partir de 3. Por isso a
 * conversão é feita por {@link #fromCodigo(Integer)} e nunca por
 * {@code values()[codigo]}, que dependia da ordem de declaração.
 */
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

    /** Estado final: não conta como pendente e libera a avaliação do atendimento. */
    public boolean ehFinal() {
        return this == ENCERRADO || this == CANCELADO;
    }

    /** Chamado ainda em aberto do ponto de vista do cliente. */
    public boolean ehPendente() {
        return !ehFinal() && this != RESOLVIDO;
    }

    /** O relógio do SLA só corre enquanto o chamado não foi resolvido. */
    public boolean contaParaSla() {
        return this == ABERTO || this == EM_ANDAMENTO;
    }

    /**
     * Transições permitidas. Impede saltos incoerentes, como reabrir um chamado
     * cancelado ou "resolver" um chamado que nunca foi atendido.
     */
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

    /** Estados considerados "em aberto" nas consultas de dashboard. */
    public static List<Integer> codigosPendentes() {
        return Arrays.stream(values())
                .filter(Status::ehPendente)
                .map(Status::getCodigo)
                .toList();
    }
}
