package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios", indexes = {
    @Index(name = "idx_comentario_chamado", columnList = "chamado_id"),
    @Index(name = "idx_comentario_autor",   columnList = "autor_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"chamado", "autor"})
public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @NotBlank(message = "O comentário não pode estar vazio")
    @Size(min = 2, max = 2000, message = "O comentário deve ter entre 2 e 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    /**
     * Nota interna: visível apenas para técnicos e administradores.
     * O cliente nunca recebe esses comentários na listagem.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean interno = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataAtualizacao;

    @Builder.Default
    @Column(nullable = false)
    private Boolean editado = false;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (interno == null) interno = false;
        if (editado == null) editado = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
        editado = true;
    }
}
