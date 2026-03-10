package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    public Chamado findById(Long id) {
        return chamadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Chamado não encontrado! ID: " + id));
    }

    public List<Chamado> findAll() {
        return chamadoRepository.findAll();
    }

    public Chamado create(Chamado obj) {
        obj.setDataAbertura(LocalDate.now());
        return chamadoRepository.save(obj);
    }

    public Chamado updateStatus(Long id, Status novoStatus) {
        Chamado chamado = findById(id);
        chamado.setStatus(novoStatus);
        if (novoStatus == Status.ENCERRADO) {
            chamado.setDataFechamento(LocalDate.now());
        }
        return chamadoRepository.save(chamado);
    }
}
