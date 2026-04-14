package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.DepartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, Long> {

    Optional<DepartamentoEntity> findByNome(String nome);

    List<DepartamentoEntity> findByAtivoTrue();

    List<DepartamentoEntity> findByAtivoTrueAndNomeContainingIgnoreCase(String nome);
}
