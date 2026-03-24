package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Anexo;
import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
    
    /**
     * Busca todos os anexos de um chamado
     */
    List<Anexo> findByChamadoOrderByDataUploadDesc(Chamado chamado);
    
    /**
     * Conta quantos anexos um chamado tem
     */
    Long countByChamado(Chamado chamado);
    
    /**
     * Busca anexos por tipo MIME
     */
    List<Anexo> findByChamadoAndTipoMimeContaining(Chamado chamado, String tipoMime);
    
    /**
     * Query customizada para buscar anexos públicos de um chamado
     */
    @Query("SELECT a FROM Anexo a WHERE a.chamado.id = :chamadoId AND a.publico = true ORDER BY a.dataUpload DESC")
    List<Anexo> findAnexosPublicos(@Param("chamadoId") Long chamadoId);
}
