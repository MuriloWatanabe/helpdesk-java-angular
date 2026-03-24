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
    
    /**
     * Busca avaliação de um chamado específico
     */
    Optional<Avaliacao> findByChamado(Chamado chamado);
    
    /**
     * Busca todas as avaliações com nota igual ou superior a um valor
     */
    List<Avaliacao> findByNotaGreaterThanEqualOrderByDataAvaliacaoDesc(Integer nota);
    
    /**
     * Query customizada para calcular nota média
     */
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.nota >= :notaMinima")
    Double calcularNotaMedia(@Param("notaMinima") Integer notaMinima);
    
    /**
     * Query para contar avaliações positivas (4 ou 5)
     */
    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.nota >= 4")
    Long contarAvaliacoesPositivas();
    
    /**
     * Query para contar avaliações negativas (1 ou 2)
     */
    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.nota <= 2")
    Long contarAvaliacoesNegativas();
}
