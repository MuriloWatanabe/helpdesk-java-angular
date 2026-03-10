package com.murilo.helpdesk.repository;

import com.murilo.helpdesk.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findByClienteId(Long clienteId);
    List<Chamado> findByTecnicoId(Long tecnicoId);
}
