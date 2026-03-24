package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade que registra o histórico de mudanças em um chamado.
 * Fornece auditoria completa de todas as alterações.
 */
@Entity
@Table(name = "historico_chamados", indexes = {
    @Index(name = "idx_chamado", columnList = "chamado_id"),
    @Index(name = "idx_usuario", columnList = "usuario_id"),
    @Index(name = "idx_data", columnList = "data_alteracao")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoChamado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioAlteracao;

    /**
     * Tipo de alteração: STATUS, PRIORIDADE, TECNICO, TITULO, etc
     */
    @Column(nullable = false, length = 50)
    private String tipoAlteracao;

    /**
     * Descrição legível da alteração
     */
    @Column(length = 200)
    private String descricao;

    /**
     * Valor anterior (como string)
     */
    @Column(columnDefinition = "TEXT")
    private String valorAnterior;

    /**
     * Valor novo (como string)
     */
    @Column(columnDefinition = "TEXT")
    private String valorNovo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist
    protected void onCreate() {
        dataAlteracao = LocalDateTime.now();
    }

    /**
     * Factory method para criar histórico de mudança de status
     */
    public static HistoricoChamado criarMudancaStatus(
            Chamado chamado, Usuario usuario, String statusAnterior, String statusNovo) {
        return HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao("STATUS")
                .descricao("Status alterado de " + statusAnterior + " para " + statusNovo)
                .valorAnterior(statusAnterior)
                .valorNovo(statusNovo)
                .build();
    }

    /**
     * Factory method para criar histórico de mudança de prioridade
     */
    public static HistoricoChamado criarMudancaPrioridade(
            Chamado chamado, Usuario usuario, String prioridadeAnterior, String prioridadeNova) {
        return HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao("PRIORIDADE")
                .descricao("Prioridade alterada de " + prioridadeAnterior + " para " + prioridadeNova)
                .valorAnterior(prioridadeAnterior)
                .valorNovo(prioridadeNova)
                .build();
    }

    /**
     * Factory method para criar histórico de atribuição de técnico
     */
    public static HistoricoChamado criarAtribuicaoTecnico(
            Chamado chamado, Usuario usuario, String tecnicoAnterior, String tecnicoNovo) {
        return HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao("TECNICO")
                .descricao("Técnico alterado de " + tecnicoAnterior + " para " + tecnicoNovo)
                .valorAnterior(tecnicoAnterior)
                .valorNovo(tecnicoNovo)
                .build();
    }

    /**
     * Factory method para criar histórico de comentário adicionado
     */
    public static HistoricoChamado criarComentarioAdicionado(Chamado chamado, Usuario usuario) {
        return HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao("COMENTARIO")
                .descricao("Novo comentário adicionado")
                .build();
    }

    /**
     * Factory method para criar histórico de fechamento
     */
    public static HistoricoChamado criarFechamento(Chamado chamado, Usuario usuario) {
        return HistoricoChamado.builder()
                .chamado(chamado)
                .usuarioAlteracao(usuario)
                .tipoAlteracao("FECHAMENTO")
                .descricao("Chamado foi finalizado")
                .valorNovo("ENCERRADO")
                .build();
    }
}
