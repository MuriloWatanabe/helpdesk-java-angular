package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ChamadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsável pela lógica de negócios de chamados.
 * Implementa gerenciamento completo do ciclo de vida dos chamados técnicos.
 */
@Slf4j
@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    /**
     * Busca um chamado pelo ID.
     * @param id ID do chamado
     * @return Chamado encontrado
     * @throws RuntimeException se não encontrar o chamado
     */
    public Chamado findById(Long id) {
        log.info("Buscando chamado com ID: {}", id);
        return chamadoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Chamado não encontrado com ID: {}", id);
                    return new RuntimeException("Chamado não encontrado! ID: " + id);
                });
    }

    /**
     * Lista todos os chamados do sistema.
     * @return Lista de chamados
     */
    public List<Chamado> findAll() {
        log.info("Listando todos os chamados");
        return chamadoRepository.findAll();
    }

    /**
     * Cria um novo chamado no sistema.
     * @param chamado Dados do chamado
     * @return Chamado criado
     */
    @Transactional
    public Chamado create(Chamado chamado) {
        log.info("Criando novo chamado: {}", chamado.getTitulo());
        
        if (chamado.getStatus() == null) {
            chamado.setStatus(Status.ABERTO.getCodigo());
        }
        
        Chamado chamadoCriado = chamadoRepository.save(chamado);
        log.info("Chamado criado com sucesso! ID: {}", chamadoCriado.getId());
        return chamadoCriado;
    }

    /**
     * Atualiza o status de um chamado.
     * @param id ID do chamado
     * @param novoStatus Novo status
     * @return Chamado com status atualizado
     */
    @Transactional
    public Chamado updateStatus(Long id, Status novoStatus) {
        log.info("Atualizando status do chamado ID: {} para {}", id, novoStatus.getDescricao());
        
        Chamado chamado = findById(id);
        chamado.setStatus(novoStatus.getCodigo());
        
        // Se o novo status é ENCERRADO, registra a data de fechamento
        if (novoStatus == Status.ENCERRADO) {
            chamado.setDataFechamento(LocalDateTime.now());
            log.info("Chamado fechado em: {}", chamado.getDataFechamento());
        }
        
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        log.info("Status do chamado atualizado com sucesso!");
        return chamadoAtualizado;
    }

    /**
     * Atualiza os dados de um chamado.
     * @param id ID do chamado
     * @param chamado Dados atualizados
     * @return Chamado atualizado
     */
    @Transactional
    public Chamado update(Long id, Chamado chamado) {
        log.info("Atualizando chamado com ID: {}", id);
        
        Chamado chamadoExistente = findById(id);
        chamadoExistente.setTitulo(chamado.getTitulo());
        chamadoExistente.setObservacoes(chamado.getObservacoes());
        chamadoExistente.setPrioridade(chamado.getPrioridade());
        
        if (chamado.getTecnico() != null) {
            chamadoExistente.setTecnico(chamado.getTecnico());
        }
        
        Chamado chamadoAtualizado = chamadoRepository.save(chamadoExistente);
        log.info("Chamado atualizado com sucesso! ID: {}", id);
        return chamadoAtualizado;
    }

    /**
     * Deleta um chamado do sistema.
     * @param id ID do chamado
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando chamado com ID: {}", id);
        Chamado chamado = findById(id);
        chamadoRepository.deleteById(id);
        log.info("Chamado deletado com sucesso! ID: {}", id);
    }
}
