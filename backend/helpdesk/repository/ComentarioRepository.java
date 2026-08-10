package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Comentario;
import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {


    List<Comentario> findByChamadoOrderByDataCriacaoDesc(Chamado chamado);


    Long countByChamado(Chamado chamado);


    @Query("SELECT c FROM Comentario c WHERE c.chamado.id = :chamadoId ORDER BY c.dataCriacao DESC")
    List<Comentario> findComentariosPorChamado(@Param("chamadoId") Long chamadoId);
}
