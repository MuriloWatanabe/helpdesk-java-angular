package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "comentarios", indexes = {
    @Index(name = "idx_chamado", columnList = "chamado_id"),
    @Index(name = "idx_autor", columnList = "autor_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @NotBlank(message = "Comentário não pode estar vazio")
    @Size(min = 5, max = 2000, message = "Comentário deve ter entre 5 e 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = true)
    private LocalDateTime dataAtualizacao;


    @Column(nullable = false)
    private Boolean editado = false;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
        editado = true;
    }
}
