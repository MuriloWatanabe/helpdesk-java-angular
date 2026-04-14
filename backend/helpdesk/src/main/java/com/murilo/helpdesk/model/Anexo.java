package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "anexos", indexes = {
    @Index(name = "idx_anexo_chamado",  columnList = "chamado_id"),
    @Index(name = "idx_anexo_usuario",  columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"chamado", "uploadPor"})
public class Anexo implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] UNIDADES = {"B", "KB", "MB", "GB", "TB"};

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain", "text/csv"
    );

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario uploadPor;

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Column(nullable = false, length = 255)
    private String nomeArquivo;

    @NotBlank(message = "Caminho/URL do arquivo é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String caminhoArquivo;

    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Column(nullable = false)
    private Long tamanho;

    @NotBlank(message = "Tipo MIME é obrigatório")
    @Column(nullable = false, length = 100)
    private String tipoMime;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Builder.Default
    @Column(nullable = false)
    private Boolean publico = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    @PrePersist
    protected void onCreate() {
        dataUpload = LocalDateTime.now();
    }

    public String getTamanhoFormatado() {
        if (tamanho == null) return "0 B";
        double valor = tamanho;
        int unidade = 0;
        while (valor >= 1024 && unidade < UNIDADES.length - 1) {
            valor /= 1024;
            unidade++;
        }
        return String.format("%.2f %s", valor, UNIDADES[unidade]);
    }

    public boolean ehTipoPermitido() {
        return TIPOS_PERMITIDOS.contains(tipoMime);
    }
}
