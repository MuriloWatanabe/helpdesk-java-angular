package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public Chamado findById(Long id) {
        log.info("Buscando chamado com ID: {}", id);
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado! ID: " + id));
    }

    public List<Chamado> findAll() {
        return chamadoRepository.findAll();
    }

    public Page<Chamado> findAll(Pageable pageable) {
        return chamadoRepository.findAll(pageable);
    }

    public List<Chamado> findByCliente(Long clienteId) {
        return chamadoRepository.findByClienteId(clienteId);
    }

    public List<Chamado> findByTecnico(Long tecnicoId) {
        return chamadoRepository.findByTecnicoId(tecnicoId);
    }

    @Transactional
    public Chamado create(Chamado chamado) {
        log.info("Criando novo chamado: {}", chamado.getTitulo());
        chamado.setStatus(Status.ABERTO.getCodigo());
        Chamado criado = chamadoRepository.save(chamado);
        log.info("Chamado criado com sucesso! ID: {}", criado.getId());
        return criado;
    }

    @Transactional
    public Chamado updateStatus(Long id, Status novoStatus) {
        log.info("Atualizando status do chamado ID: {} para {}", id, novoStatus);
        Chamado chamado = findById(id);
        chamado.setStatus(novoStatus.getCodigo());
        if (novoStatus == Status.ENCERRADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        return chamadoRepository.save(chamado);
    }

    @Transactional
    public Chamado update(Long id, Chamado chamado) {
        log.info("Atualizando chamado com ID: {}", id);
        Chamado existente = findById(id);
        existente.setTitulo(chamado.getTitulo());
        existente.setObservacoes(chamado.getObservacoes());
        existente.setPrioridade(chamado.getPrioridade());
        if (chamado.getTecnico() != null) {
            existente.setTecnico(chamado.getTecnico());
        }
        Chamado atualizado = chamadoRepository.save(existente);
        log.info("Chamado atualizado com sucesso! ID: {}", id);
        return atualizado;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando chamado com ID: {}", id);
        findById(id); // valida existência
        chamadoRepository.deleteById(id);
        log.info("Chamado deletado com sucesso! ID: {}", id);
    }
}
