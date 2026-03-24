package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Entidade que representa um anexo/arquivo em um chamado.
 * Permite compartilhar screenshots, logs, arquivos de diagnóstico, etc.
 */
@Entity
@Table(name = "anexos", indexes = {
    @Index(name = "idx_chamado", columnList = "chamado_id"),
    @Index(name = "idx_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anexo implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String caminhoArquivo; // Pode ser caminho local ou URL S3/Cloud

    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Column(nullable = false)
    private Long tamanho; // em bytes

    @NotBlank(message = "Tipo MIME é obrigatório")
    @Column(nullable = false, length = 100)
    private String tipoMime; // application/pdf, image/png, etc

    /**
     * Descrição opcional do arquivo
     */
    @Column(columnDefinition = "TEXT")
    private String descricao;

    /**
     * Indica se é um arquivo público ou privado
     */
    @Column(nullable = false)
    private Boolean publico = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    @PrePersist
    protected void onCreate() {
        dataUpload = LocalDateTime.now();
    }

    /**
     * Retorna uma representação legível do tamanho do arquivo
     * @return Tamanho formatado (KB, MB, GB)
     */
    public String getTamanhoFormatado() {
        if (tamanho == null) return "0 B";
        
        long bytes = tamanho;
        int unidade = 0;
        double tamanhoFormatado = bytes;
        
        while (tamanhoFormatado >= 1024 && unidade < 4) {
            tamanhoFormatado /= 1024;
            unidade++;
        }
        
        String[] unidades = {"B", "KB", "MB", "GB", "TB"};
        return String.format("%.2f %s", tamanhoFormatado, unidades[unidade]);
    }

    /**
     * Valida se o arquivo é permitido
     * @return true se o arquivo tem tipo permitido
     */
    public Boolean ehTipoPermitido() {
        String[] tiposPermitidos = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "text/csv"
        };
        
        for (String tipo : tiposPermitidos) {
            if (tipo.equals(tipoMime)) return true;
        }
        return false;
    }
}
