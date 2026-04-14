package com.murilo.helpdesk.model;

import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "chamados", indexes = {
    @Index(name = "idx_chamado_cliente",  columnList = "cliente_id"),
    @Index(name = "idx_chamado_tecnico",  columnList = "tecnico_id"),
    @Index(name = "idx_chamado_status",   columnList = "status")
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

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "Observações são obrigatórias")
    @Size(min = 10, max = 2000, message = "Observações devem ter entre 10 e 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "Status é obrigatório")
    @Column(nullable = false)
    private Integer status;

    @NotNull(message = "Prioridade é obrigatória")
    @Column(nullable = false)
    private Integer prioridade;

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

    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Status getStatusEnum() {
        return Status.values()[this.status];
    }

    public void setStatusEnum(Status status) {
        this.status = status.getCodigo();
    }

    public Prioridade getPrioridadeEnum() {
        return Prioridade.values()[this.prioridade];
    }

    public void setPrioridadeEnum(Prioridade prioridade) {
        this.prioridade = prioridade.getCodigo();
    }
}
