package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ChamadoRepository
        extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    List<Chamado> findByClienteId(Long clienteId);
    List<Chamado> findByTecnicoId(Long tecnicoId);
    Page<Chamado> findByClienteId(Long clienteId, Pageable pageable);
    Page<Chamado> findByTecnicoId(Long tecnicoId, Pageable pageable);

    long countByStatus(Integer status);
    long countByStatusIn(Collection<Integer> status);
    long countByClienteId(Long clienteId);
    long countByTecnicoId(Long tecnicoId);
    long countByClienteIdAndStatusIn(Long clienteId, Collection<Integer> status);
    long countByTecnicoIdAndStatusIn(Long tecnicoId, Collection<Integer> status);
    boolean existsByClienteIdOrTecnicoId(Long clienteId, Long tecnicoId);

    /** Último protocolo emitido no ano, para gerar o próximo número sequencial. */
    @Query("SELECT MAX(c.numero) FROM Chamado c WHERE c.numero LIKE CONCAT(:prefixo, '%')")
    String findUltimoNumeroDoAno(@Param("prefixo") String prefixo);

    // ------------------------------------------------------------------
    // Agregações do dashboard / relatórios
    // ------------------------------------------------------------------

    @Query("SELECT c.status, COUNT(c) FROM Chamado c GROUP BY c.status")
    List<Object[]> contarPorStatus();

    @Query("SELECT c.status, COUNT(c) FROM Chamado c WHERE c.cliente.id = :usuarioId GROUP BY c.status")
    List<Object[]> contarPorStatusDoCliente(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c.status, COUNT(c) FROM Chamado c WHERE c.tecnico.id = :usuarioId GROUP BY c.status")
    List<Object[]> contarPorStatusDoTecnico(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c.prioridade, COUNT(c) FROM Chamado c GROUP BY c.prioridade")
    List<Object[]> contarPorPrioridade();

    @Query("SELECT c.categoria, COUNT(c) FROM Chamado c GROUP BY c.categoria")
    List<Object[]> contarPorCategoria();

    @Query("""
           SELECT c.tecnico.id, c.tecnico.nome, COUNT(c)
           FROM Chamado c
           WHERE c.tecnico IS NOT NULL
           GROUP BY c.tecnico.id, c.tecnico.nome
           ORDER BY COUNT(c) DESC
           """)
    List<Object[]> contarPorTecnico();

    /**
     * Datas de abertura do período — o agrupamento por dia é feito em memória
     * para não depender de função de data específica do banco (o teste roda em
     * H2 e a aplicação em PostgreSQL).
     */
    @Query("SELECT c.dataAbertura FROM Chamado c WHERE c.dataAbertura >= :inicio")
    List<LocalDateTime> buscarAberturasDesde(@Param("inicio") LocalDateTime inicio);

    /** Chamados ainda em atendimento cujo prazo de SLA já venceu. */
    @Query("""
           SELECT COUNT(c) FROM Chamado c
           WHERE c.prazoSla IS NOT NULL
             AND c.prazoSla < :agora
             AND c.status IN :statusAtivos
           """)
    long contarSlaVencido(@Param("agora") LocalDateTime agora,
                          @Param("statusAtivos") Collection<Integer> statusAtivos);

    /** Chamados que vencem dentro da janela informada (alerta preventivo). */
    @Query("""
           SELECT COUNT(c) FROM Chamado c
           WHERE c.prazoSla IS NOT NULL
             AND c.prazoSla BETWEEN :agora AND :limite
             AND c.status IN :statusAtivos
           """)
    long contarSlaEmRisco(@Param("agora") LocalDateTime agora,
                          @Param("limite") LocalDateTime limite,
                          @Param("statusAtivos") Collection<Integer> statusAtivos);

    @Query("SELECT c.dataAbertura, c.dataFechamento FROM Chamado c WHERE c.dataFechamento IS NOT NULL")
    List<Object[]> buscarDatasDeResolucao();

    long countByDataAberturaGreaterThanEqual(LocalDateTime inicio);
}
