package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {
    
    /**
     * Busca todo histórico de um chamado
     */
    List<HistoricoChamado> findByChamadoOrderByDataAlteracaoDesc(Chamado chamado);
    
    /**
     * Busca histórico por tipo de alteração
     */
    List<HistoricoChamado> findByChamadoAndTipoAlteracao(Chamado chamado, String tipoAlteracao);
    
    /**
     * Conta quantas alterações um chamado teve
     */
    Long countByChamado(Chamado chamado);
    
    /**
     * Query customizada para buscar histórico em um período
     */
    @Query("SELECT h FROM HistoricoChamado h WHERE h.chamado.id = :chamadoId AND h.dataAlteracao BETWEEN :dataInicio AND :dataFim ORDER BY h.dataAlteracao DESC")
    List<HistoricoChamado> findHistoricoPorPeriodo(
            @Param("chamadoId") Long chamadoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
    
    /**
     * Query para buscar todas as mudanças de status
     */
    @Query("SELECT h FROM HistoricoChamado h WHERE h.chamado.id = :chamadoId AND h.tipoAlteracao = 'STATUS' ORDER BY h.dataAlteracao DESC")
    List<HistoricoChamado> findMudancasStatus(@Param("chamadoId") Long chamadoId);
}
