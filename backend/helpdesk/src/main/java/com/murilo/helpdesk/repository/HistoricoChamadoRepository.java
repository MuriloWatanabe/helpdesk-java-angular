package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {

    List<HistoricoChamado> findByChamadoOrderByDataAlteracaoDesc(Chamado chamado);

    // Tipo correto: enum TipoAlteracao, não String
    List<HistoricoChamado> findByChamadoAndTipoAlteracao(
            Chamado chamado, HistoricoChamado.TipoAlteracao tipoAlteracao);

    Long countByChamado(Chamado chamado);

    @Query("SELECT h FROM HistoricoChamado h " +
           "WHERE h.chamado.id = :chamadoId " +
           "AND h.dataAlteracao BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY h.dataAlteracao DESC")
    List<HistoricoChamado> findHistoricoPorPeriodo(
            @Param("chamadoId") Long chamadoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT h FROM HistoricoChamado h " +
           "WHERE h.chamado.id = :chamadoId AND h.tipoAlteracao = 'STATUS' " +
           "ORDER BY h.dataAlteracao DESC")
    List<HistoricoChamado> findMudancasStatus(@Param("chamadoId") Long chamadoId);
}
