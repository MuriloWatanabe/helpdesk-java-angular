package com.murilo.helpdesk.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_reset_token",   columnList = "token"),
    @Index(name = "idx_reset_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "usuario")
public class PasswordResetToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_uso")
    private LocalDateTime dataUso;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    public boolean expirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    public boolean usado() {
        return dataUso != null;
    }

    public boolean valido() {
        return !expirado() && !usado();
    }
}
