package com.murilo.helpdesk.model;

import com.murilo.helpdesk.model.enums.Perfil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"departamento", "senha"})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ser válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String cargo;

    /**
     * Usuários não são excluídos quando já possuem chamados: são desativados.
     * Um usuário inativo não consegue autenticar.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfis", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "perfil")
    @Builder.Default
    private Set<Integer> perfis = new HashSet<>();

    // Departamento ao qual o usuário pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private DepartamentoEntity departamento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataAtualizacao;

    @Column
    private LocalDateTime ultimoAcesso;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (ativo == null) ativo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Códigos desconhecidos são ignorados em vez de derrubar a sessão: antes,
     * um perfil inválido no banco quebrava o login com ArrayIndexOutOfBounds.
     */
    public Set<Perfil> getPerfis() {
        return perfis.stream()
                .filter(Perfil::codigoValido)
                .map(Perfil::fromCodigo)
                .collect(Collectors.toSet());
    }

    public Set<Integer> getPerfisCodigos() {
        return perfis;
    }

    public boolean temPerfil(Perfil perfil) {
        return perfis.contains(perfil.getCodigo());
    }

    public boolean ehAdmin() {
        return temPerfil(Perfil.ADMIN);
    }

    public boolean ehTecnico() {
        return temPerfil(Perfil.TECNICO);
    }

    /** Cliente "puro": sem privilégios de admin nem de técnico. */
    public boolean ehSomenteCliente() {
        return temPerfil(Perfil.CLIENTE) && !ehAdmin() && !ehTecnico();
    }

    /** Pode atender chamados de qualquer cliente. */
    public boolean ehAtendente() {
        return ehAdmin() || ehTecnico();
    }

    public void addPerfil(Perfil perfil) {
        this.perfis.add(perfil.getCodigo());
    }

    public void updatePerfis(Set<Integer> novosPerfis) {
        this.perfis.clear();
        if (novosPerfis != null) {
            this.perfis.addAll(novosPerfis);
        }
    }
}
