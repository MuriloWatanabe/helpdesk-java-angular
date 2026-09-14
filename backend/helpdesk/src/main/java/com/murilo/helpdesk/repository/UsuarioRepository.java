package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    // usado por garantirOutroAdminAtivo — evita carregar todos os usuários em memória
    boolean existsByPerfisContainsAndAtivoTrueAndIdNot(Integer perfilAdmin, Long idIgnorado);
}
