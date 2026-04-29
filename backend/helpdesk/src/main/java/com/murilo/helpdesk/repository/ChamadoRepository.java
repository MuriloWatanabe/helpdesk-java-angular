package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findByClienteId(Long clienteId);
    List<Chamado> findByTecnicoId(Long tecnicoId);
    Page<Chamado> findByClienteId(Long clienteId, Pageable pageable);
    Page<Chamado> findByTecnicoId(Long tecnicoId, Pageable pageable);
    long countByStatus(Integer status);
}
