package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByChamadoOrderByDataCriacaoDesc(Chamado chamado);

    /** Ordem cronológica — é assim que a conversa é exibida na tela do chamado. */
    List<Comentario> findByChamadoIdOrderByDataCriacaoAsc(Long chamadoId);

    /** Somente os comentários visíveis ao cliente (exclui notas internas). */
    List<Comentario> findByChamadoIdAndInternoFalseOrderByDataCriacaoAsc(Long chamadoId);

    long countByChamadoId(Long chamadoId);

    /** Contagem em lote para a listagem, evitando uma consulta por linha. */
    @Query("SELECT c.chamado.id, COUNT(c) FROM Comentario c " +
           "WHERE c.chamado.id IN :ids GROUP BY c.chamado.id")
    List<Object[]> contarPorChamados(@Param("ids") Collection<Long> ids);

    long countByChamadoIdAndInternoFalse(Long chamadoId);

    Long countByChamado(Chamado chamado);

    void deleteByChamadoId(Long chamadoId);
}
