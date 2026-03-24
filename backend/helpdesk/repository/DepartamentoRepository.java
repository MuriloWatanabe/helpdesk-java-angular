package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, Long> {
    
    /**
     * Busca departamento pelo nome
     */
    Optional<DepartamentoEntity> findByNome(String nome);
    
    /**
     * Lista todos os departamentos ativos
     */
    List<DepartamentoEntity> findByAtivoTrue();
    
    /**
     * Busca departamentos ativos por nome contendo
     */
    List<DepartamentoEntity> findByAtivoTrueAndNomeContainingIgnoreCase(String nome);
}
