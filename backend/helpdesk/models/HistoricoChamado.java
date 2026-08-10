package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "historico_chamados", indexes = {
    @Index(name = "idx_historico_chamado", columnList = "chamado_id"),
    @Index(name = "idx_historico_usuario", columnList = "usuario_id"),
    @Index(name = "idx_historico_data", columnList = "data_alteracao"),
    @Index(name = "idx_historico_tipo", columnList = "tipo_alteracao")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoChamado implements Serializable {

    private static final long serialVersionUID = 1L;


    public enum TipoAlteracao {
        STATUS,
        PRIORIDADE,
        TECNICO,
        TITULO,
        COMENTARIO,
        FECHAMENTO,
        REABERTURA,
        CRIACAO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioAlteracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alteracao", nullable = false, length = 50)
    private TipoAlteracao tipoAlteracao;

    @Column(length = 200)
    private String descricao;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_novo", columnDefinition = "TEXT")
    private String valorNovo;

    @Column(name = "data_alteracao", nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist
    protected void onCreate() {
        dataAlteracao = LocalDateTime.now();
    }


    public static HistoricoChamado criarMudancaStatus(
            Chamado chamado, Usuario usuario, String statusAnterior, String statusNovo) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.STATUS)
                .descricao(String.format("Status alterado de '%s' para '%s'", statusAnterior, statusNovo))
                .valorAnterior(statusAnterior)
                .valorNovo(statusNovo)
                .build();
    }

    public static HistoricoChamado criarMudancaPrioridade(
            Chamado chamado, Usuario usuario, String prioridadeAnterior, String prioridadeNova) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.PRIORIDADE)
                .descricao(String.format("Prioridade alterada de '%s' para '%s'", prioridadeAnterior, prioridadeNova))
                .valorAnterior(prioridadeAnterior)
                .valorNovo(prioridadeNova)
                .build();
    }

    public static HistoricoChamado criarAtribuicaoTecnico(
            Chamado chamado, Usuario usuario, String tecnicoAnterior, String tecnicoNovo) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.TECNICO)
                .descricao(String.format("Técnico alterado de '%s' para '%s'", tecnicoAnterior, tecnicoNovo))
                .valorAnterior(tecnicoAnterior)
                .valorNovo(tecnicoNovo)
                .build();
    }

    public static HistoricoChamado criarComentarioAdicionado(Chamado chamado, Usuario usuario) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.COMENTARIO)
                .descricao("Novo comentário adicionado")
                .build();
    }

    public static HistoricoChamado criarFechamento(Chamado chamado, Usuario usuario) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.FECHAMENTO)
                .descricao("Chamado finalizado")
                .valorNovo("ENCERRADO")
                .build();
    }

    public static HistoricoChamado criarReabertura(Chamado chamado, Usuario usuario) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.REABERTURA)
                .descricao("Chamado reaberto")
                .valorNovo("ABERTO")
                .build();
    }

    public static HistoricoChamado criarCriacao(Chamado chamado, Usuario usuario) {
        return builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao(TipoAlteracao.CRIACAO)
                .descricao("Chamado criado")
                .build();
    }
}
 