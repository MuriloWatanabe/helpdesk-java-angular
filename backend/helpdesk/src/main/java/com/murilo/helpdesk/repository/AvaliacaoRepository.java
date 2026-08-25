package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Avaliacao;
import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByChamado(Chamado chamado);

    Optional<Avaliacao> findByChamadoId(Long chamadoId);

    boolean existsByChamadoId(Long chamadoId);


    @Query("SELECT a.chamado.id FROM Avaliacao a WHERE a.chamado.id IN :ids")
    List<Long> idsAvaliados(@Param("ids") java.util.Collection<Long> ids);

    List<Avaliacao> findByNotaGreaterThanEqualOrderByDataAvaliacaoDesc(Integer nota);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a")
    Double calcularNotaMedia();


    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.chamado.tecnico.id = :tecnicoId")
    Double calcularNotaMediaPorTecnico(@Param("tecnicoId") Long tecnicoId);

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.chamado.tecnico.id = :tecnicoId")
    long contarPorTecnico(@Param("tecnicoId") Long tecnicoId);

    @Query("SELECT a.nota, COUNT(a) FROM Avaliacao a GROUP BY a.nota ORDER BY a.nota")
    List<Object[]> contarPorNota();

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.nota >= 4")
    Long contarAvaliacoesPositivas();

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.nota <= 2")
    Long contarAvaliacoesNegativas();
}
