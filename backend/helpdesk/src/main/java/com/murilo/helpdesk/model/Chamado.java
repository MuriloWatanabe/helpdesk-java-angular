package com.murilo.helpdesk.model;

import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "chamados", indexes = {
    @Index(name = "idx_chamado_cliente",    columnList = "cliente_id"),
    @Index(name = "idx_chamado_tecnico",    columnList = "tecnico_id"),
    @Index(name = "idx_chamado_status",     columnList = "status"),
    @Index(name = "idx_chamado_categoria",  columnList = "categoria"),
    @Index(name = "idx_chamado_abertura",   columnList = "data_abertura")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tecnico", "cliente"})
public class Chamado implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "numero", unique = true, length = 20)
    private String numero;

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "Descrição deve ter entre 10 e 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "Status é obrigatório")
    @Column(nullable = false)
    private Integer status;

    @NotNull(message = "Prioridade é obrigatória")
    @Column(nullable = false)
    private Integer prioridade;


    @Column(name = "categoria")
    private Integer categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataFechamento;

    @Column
    private LocalDateTime dataAtualizacao;


    @Column(name = "prazo_sla")
    private LocalDateTime prazoSla;


    @Column(name = "data_primeira_resposta")
    private LocalDateTime dataPrimeiraResposta;

    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        dataAbertura = agora;
        dataAtualizacao = agora;
        if (prazoSla == null) {
            calcularPrazoSla();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }


    public Status getStatusEnum() {
        return Status.fromCodigo(this.status);
    }

    public void setStatusEnum(Status status) {
        this.status = status.getCodigo();
    }

    public Prioridade getPrioridadeEnum() {
        return Prioridade.fromCodigo(this.prioridade);
    }

    public void setPrioridadeEnum(Prioridade prioridade) {
        this.prioridade = prioridade.getCodigo();
    }

    public Categoria getCategoriaEnum() {
        return Categoria.fromCodigo(this.categoria);
    }

    public void setCategoriaEnum(Categoria categoria) {
        this.categoria = categoria == null ? null : categoria.getCodigo();
    }


    public void calcularPrazoSla() {
        Prioridade p = getPrioridadeEnum();
        if (p == null) return;
        LocalDateTime base = dataAbertura != null ? dataAbertura : LocalDateTime.now();
        this.prazoSla = base.plusHours(p.getHorasSla());
    }


    public boolean isSlaVencido() {
        if (prazoSla == null) return false;
        Status s = getStatusEnum();
        if (s != null && !s.contaParaSla()) return false;
        return LocalDateTime.now().isAfter(prazoSla);
    }


    public Long getHorasRestantesSla() {
        if (prazoSla == null) return null;
        Status s = getStatusEnum();
        if (s != null && !s.contaParaSla()) return null;
        return Duration.between(LocalDateTime.now(), prazoSla).toHours();
    }
}
