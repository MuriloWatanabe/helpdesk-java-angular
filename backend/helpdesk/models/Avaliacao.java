package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade que representa a avaliação de satisfação de um usuário sobre um chamado.
 * Permite coletar feedback de qualidade do atendimento.
 */
@Entity
@Table(name = "avaliacoes", indexes = {
    @Index(name = "idx_chamado", columnList = "chamado_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avaliacao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota deve ser de 1 a 5")
    @Max(value = 5, message = "Nota deve ser de 1 a 5")
    @Column(nullable = false)
    private Integer nota;

    /**
     * Descrição textual da avaliação
     */
    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String comentario;

    /**
     * Aspectos avaliados (separado por vírgula)
     * Ex: "Rapidez, Competência, Cortesia"
     */
    @Column(length = 500)
    private String aspectosAvaliados;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    /**
     * Usuário que avaliou (geralmente o cliente)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioAvaliador;

    @PrePersist
    protected void onCreate() {
        dataAvaliacao = LocalDateTime.now();
    }

    /**
     * Retorna interpretação textual da nota
     */
    public String getInterpretacaoNota() {
        return switch (nota) {
            case 1 -> "Muito Ruim";
            case 2 -> "Ruim";
            case 3 -> "Regular";
            case 4 -> "Bom";
            case 5 -> "Excelente";
            default -> "Indefinida";
        };
    }

    /**
     * Indica se foi uma avaliação positiva (4 ou 5)
     */
    public Boolean ehPositiva() {
        return nota >= 4;
    }
}
