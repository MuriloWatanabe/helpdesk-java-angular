package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Anexo;
import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByChamadoOrderByDataUploadDesc(Chamado chamado);

    List<Anexo> findByChamadoIdOrderByDataUploadDesc(Long chamadoId);


    List<Anexo> findByChamadoIdAndPublicoTrueOrderByDataUploadDesc(Long chamadoId);

    long countByChamadoId(Long chamadoId);


    @Query("SELECT a.chamado.id, COUNT(a) FROM Anexo a " +
           "WHERE a.chamado.id IN :ids GROUP BY a.chamado.id")
    List<Object[]> contarPorChamados(@Param("ids") Collection<Long> ids);

    Long countByChamado(Chamado chamado);
}
