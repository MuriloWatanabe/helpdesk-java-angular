package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);


    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.dataUso = :agora " +
           "WHERE t.usuario.id = :usuarioId AND t.dataUso IS NULL")
    void invalidarPendentesDoUsuario(@Param("usuarioId") Long usuarioId,
                                     @Param("agora") LocalDateTime agora);
}
