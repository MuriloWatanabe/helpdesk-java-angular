package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Chamado findById(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado! ID: " + id));
    }

    @Transactional(readOnly = true)
    public ChamadoResponse findByIdAsResponse(Long id) {
        return Mapper.toChamadoResponse(findById(id));
    }

    /**
     * Listagem inteligente por papel:
     * CLIENTE → somente seus próprios chamados
     * ADMIN / TECNICO → todos
     */
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> findAll(Pageable pageable, String currentEmail) {
        var usuario = usuarioService.findByEmail(currentEmail);
        boolean isCliente = usuario.getPerfis().stream().anyMatch(p -> p == Perfil.CLIENTE);

        Page<Chamado> page = isCliente
                ? chamadoRepository.findByClienteId(usuario.getId(), pageable)
                : chamadoRepository.findAll(pageable);

        return page.map(Mapper::toChamadoResponse);
    }

    @Transactional
    public ChamadoResponse create(ChamadoRequest request, String currentEmail) {
        log.info("Criando chamado: {}", request.titulo());
        var currentUser = usuarioService.findByEmail(currentEmail);
        boolean isCliente = currentUser.getPerfis().stream().anyMatch(p -> p == Perfil.CLIENTE);

        Long clienteId = isCliente ? currentUser.getId() : request.clienteId();
        if (clienteId == null) {
            throw new RuntimeException("clienteId é obrigatório");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo());
        chamado.setObservacoes(request.observacoes());
        chamado.setPrioridade(request.prioridade());
        chamado.setStatus(Status.ABERTO.getCodigo());
        chamado.setCliente(usuarioService.findById(clienteId));

        if (request.tecnicoId() != null) {
            chamado.setTecnico(usuarioService.findById(request.tecnicoId()));
        }

        return Mapper.toChamadoResponse(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse update(Long id, ChamadoRequest request) {
        log.info("Atualizando chamado ID: {}", id);
        Chamado existente = findById(id);
        existente.setTitulo(request.titulo());
        existente.setObservacoes(request.observacoes());
        existente.setPrioridade(request.prioridade());

        if (request.tecnicoId() != null) {
            existente.setTecnico(usuarioService.findById(request.tecnicoId()));
        }
        if (request.clienteId() != null) {
            existente.setCliente(usuarioService.findById(request.clienteId()));
        }

        return Mapper.toChamadoResponse(chamadoRepository.save(existente));
    }

    @Transactional
    public ChamadoResponse updateStatus(Long id, Status novoStatus) {
        log.info("Atualizando status do chamado ID: {} → {}", id, novoStatus);
        Chamado chamado = findById(id);
        chamado.setStatus(novoStatus.getCodigo());
        if (novoStatus == Status.ENCERRADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        return Mapper.toChamadoResponse(chamadoRepository.save(chamado));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando chamado ID: {}", id);
        findById(id);
        chamadoRepository.deleteById(id);
    }
}
