package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "avaliacoes", indexes = {
    @Index(name = "idx_avaliacao_chamado",  columnList = "chamado_id"),
    @Index(name = "idx_avaliacao_usuario",  columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"chamado", "usuarioAvaliador"})
public class Avaliacao implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota deve ser de 1 a 5")
    @Max(value = 5, message = "Nota deve ser de 1 a 5")
    @Column(nullable = false)
    private Integer nota;

    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String comentario;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "avaliacao_aspectos",
        joinColumns = @JoinColumn(name = "avaliacao_id")
    )
    @Column(name = "aspecto", length = 100)
    @Builder.Default
    private Set<String> aspectosAvaliados = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioAvaliador;

    @PrePersist
    protected void onCreate() {
        dataAvaliacao = LocalDateTime.now();
    }


    public String getInterpretacaoNota() {
        if (nota == null) return "Indefinida";
        return switch (nota) {
            case 1 -> "Muito Ruim";
            case 2 -> "Ruim";
            case 3 -> "Regular";
            case 4 -> "Bom";
            case 5 -> "Excelente";
            default -> "Indefinida";
        };
    }


    public boolean ehPositiva() {
        return nota != null && nota >= 4;
    }
}
