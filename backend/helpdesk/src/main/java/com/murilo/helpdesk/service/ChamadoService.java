package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ChamadoFiltro;
import com.murilo.helpdesk.dto.request.ChamadoRequest;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AnexoRepository;
import com.murilo.helpdesk.repository.AvaliacaoRepository;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.repository.ComentarioRepository;
import com.murilo.helpdesk.repository.spec.ChamadoSpecs;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ComentarioRepository comentarioRepository;
    private final AnexoRepository anexoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Chamado findById(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Chamado", id));
    }

    /**
     * Busca com verificação de acesso. Sem isso, um cliente conseguia ler o
     * chamado de qualquer outro apenas trocando o ID na URL.
     */
    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = findById(id);
        garantirAcessoDeLeitura(chamado, solicitante);
        return montarResposta(chamado);
    }

    /**
     * Listagem paginada e filtrada no banco. O escopo é definido pelo papel:
     * cliente enxerga apenas os próprios chamados; técnico e admin, todos.
     */
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(ChamadoFiltro filtro, Pageable pageable,
                                        String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Long restricaoCliente = solicitante.ehSomenteCliente() ? solicitante.getId() : null;

        Page<Chamado> pagina = chamadoRepository
                .findAll(ChamadoSpecs.montar(filtro, restricaoCliente), pageable);

        List<Long> ids = pagina.getContent().stream().map(Chamado::getId).toList();

        // Página vazia não consulta nada: um IN com lista vazia é inválido.
        if (ids.isEmpty()) {
            return pagina.map(chamado -> Mapper.toChamadoResponse(chamado, false, 0L, 0L));
        }

        // Contadores buscados de uma vez para a página inteira: montar cada
        // resposta isoladamente geraria três consultas por linha.
        Map<Long, Long> comentarios = agruparContagem(comentarioRepository.contarPorChamados(ids));
        Map<Long, Long> anexos = agruparContagem(anexoRepository.contarPorChamados(ids));
        Set<Long> avaliados = new HashSet<>(avaliacaoRepository.idsAvaliados(ids));

        return pagina.map(chamado -> Mapper.toChamadoResponse(
                chamado,
                avaliados.contains(chamado.getId()),
                comentarios.getOrDefault(chamado.getId(), 0L),
                anexos.getOrDefault(chamado.getId(), 0L)));
    }

    private Map<Long, Long> agruparContagem(List<Object[]> linhas) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] linha : linhas) {
            if (linha[0] instanceof Number chamadoId && linha[1] instanceof Number total) {
                mapa.put(chamadoId.longValue(), total.longValue());
            }
        }
        return mapa;
    }

    // ------------------------------------------------------------------
    // Escrita
    // ------------------------------------------------------------------

    @Transactional
    public ChamadoResponse criar(ChamadoRequest request, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);

        // Cliente sempre abre em seu próprio nome; atendente pode abrir por terceiros.
        Long clienteId = solicitante.ehSomenteCliente() ? solicitante.getId() : request.clienteId();
        if (clienteId == null) {
            throw new BusinessException("Selecione o cliente para quem o chamado será aberto.");
        }

        Prioridade prioridade = converterPrioridade(request.prioridade());
        Categoria categoria = converterCategoria(request.categoria());

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo().trim());
        chamado.setObservacoes(request.observacoes().trim());
        chamado.setStatus(Status.ABERTO.getCodigo());
        chamado.setPrioridadeEnum(prioridade);
        chamado.setCategoriaEnum(categoria);
        chamado.setCliente(usuarioService.findById(clienteId));
        chamado.setNumero(gerarNumeroProtocolo());

        if (request.tecnicoId() != null) {
            chamado.setTecnico(carregarTecnico(request.tecnicoId()));
        }

        Chamado salvo = chamadoRepository.save(chamado);
        historicoService.registrarCriacao(salvo, solicitante);
        log.info("Chamado {} aberto por {}", salvo.getNumero(), solicitante.getEmail());

        return montarResposta(salvo);
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = findById(id);
        garantirEdicao(chamado, solicitante);

        Prioridade prioridadeAnterior = chamado.getPrioridadeEnum();
        Prioridade novaPrioridade = converterPrioridade(request.prioridade());

        chamado.setTitulo(request.titulo().trim());
        chamado.setObservacoes(request.observacoes().trim());
        chamado.setCategoriaEnum(converterCategoria(request.categoria()));

        if (prioridadeAnterior != novaPrioridade) {
            chamado.setPrioridadeEnum(novaPrioridade);
            // Mudar a prioridade muda o prazo prometido ao cliente.
            chamado.calcularPrazoSla();
            historicoService.registrarMudancaPrioridade(chamado, solicitante,
                    prioridadeAnterior != null ? prioridadeAnterior.getDescricao() : "—",
                    novaPrioridade.getDescricao());
        }

        // Reatribuição só é permitida a quem atende.
        if (solicitante.ehAtendente()) {
            aplicarTecnico(chamado, request.tecnicoId(), solicitante);
            if (request.clienteId() != null
                    && !request.clienteId().equals(chamado.getCliente().getId())) {
                chamado.setCliente(usuarioService.findById(request.clienteId()));
            }
        }

        return montarResposta(chamadoRepository.save(chamado));
    }

    /**
     * Muda o status respeitando as transições válidas e o que cada papel pode
     * fazer. Registra a mudança no histórico e controla as datas derivadas.
     */
    @Transactional
    public ChamadoResponse alterarStatus(Long id, Status novoStatus, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = findById(id);
        garantirAcessoDeLeitura(chamado, solicitante);

        Status atual = chamado.getStatusEnum();
        if (atual == novoStatus) {
            return montarResposta(chamado);
        }
        if (!atual.podeIrPara(novoStatus)) {
            throw new BusinessException(
                    "Não é possível mudar de \"" + atual.getDescricao()
                    + "\" para \"" + novoStatus.getDescricao() + "\".");
        }
        garantirTransicaoPermitida(solicitante, atual, novoStatus);

        chamado.setStatusEnum(novoStatus);
        aplicarEfeitosDoStatus(chamado, solicitante, atual, novoStatus);

        historicoService.registrarMudancaStatus(chamado, solicitante,
                atual.getDescricao(), novoStatus.getDescricao());

        return montarResposta(chamadoRepository.save(chamado));
    }

    /** Técnico assume um chamado da fila e já o coloca em andamento. */
    @Transactional
    public ChamadoResponse assumir(Long id, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        if (!solicitante.ehAtendente()) {
            throw new OperacaoNaoPermitidaException("Apenas técnicos podem assumir chamados.");
        }

        Chamado chamado = findById(id);
        Status atual = chamado.getStatusEnum();
        if (atual.ehFinal()) {
            throw new BusinessException("Este chamado já foi finalizado.");
        }
        if (chamado.getTecnico() != null
                && !chamado.getTecnico().getId().equals(solicitante.getId())) {
            throw new BusinessException(
                    "Chamado já atribuído a " + chamado.getTecnico().getNome() + ".");
        }

        String anterior = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "—";
        chamado.setTecnico(solicitante);
        historicoService.registrarAtribuicaoTecnico(chamado, solicitante, anterior,
                solicitante.getNome());

        if (atual == Status.ABERTO) {
            chamado.setStatusEnum(Status.EM_ANDAMENTO);
            registrarPrimeiraResposta(chamado);
            historicoService.registrarMudancaStatus(chamado, solicitante,
                    atual.getDescricao(), Status.EM_ANDAMENTO.getDescricao());
        }

        return montarResposta(chamadoRepository.save(chamado));
    }

    /** Atribui (ou remove, com tecnicoId nulo) o técnico responsável. */
    @Transactional
    public ChamadoResponse atribuirTecnico(Long id, Long tecnicoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = findById(id);
        garantirEdicao(chamado, solicitante);

        aplicarTecnico(chamado, tecnicoId, solicitante);
        return montarResposta(chamadoRepository.save(chamado));
    }

    @Transactional
    public void excluir(Long id, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        if (!solicitante.ehAdmin()) {
            throw new OperacaoNaoPermitidaException("Apenas administradores podem excluir chamados.");
        }
        Chamado chamado = findById(id);

        // Remove os dependentes antes: sem isso o banco recusava a exclusão e o
        // erro chegava ao usuário como 500.
        avaliacaoRepository.findByChamadoId(id).ifPresent(avaliacaoRepository::delete);
        anexoRepository.deleteAll(anexoRepository.findByChamadoIdOrderByDataUploadDesc(id));
        comentarioRepository.deleteAll(comentarioRepository.findByChamadoIdOrderByDataCriacaoAsc(id));

        chamadoRepository.delete(chamado);
        log.info("Chamado {} excluído por {}", chamado.getNumero(), solicitante.getEmail());
    }

    // ------------------------------------------------------------------
    // Regras de acesso (reutilizadas pelos serviços de comentário/anexo/avaliação)
    // ------------------------------------------------------------------

    /** Cliente só enxerga os próprios chamados; atendentes enxergam todos. */
    public void garantirAcessoDeLeitura(Chamado chamado, Usuario usuario) {
        if (usuario.ehAtendente()) return;
        if (chamado.getCliente() != null
                && chamado.getCliente().getId().equals(usuario.getId())) return;
        throw new OperacaoNaoPermitidaException("Você não tem acesso a este chamado.");
    }

    /** Edição de conteúdo/atribuição: apenas admin e técnico. */
    public void garantirEdicao(Chamado chamado, Usuario usuario) {
        if (!usuario.ehAtendente()) {
            throw new OperacaoNaoPermitidaException(
                    "Somente a equipe de suporte pode alterar os dados do chamado.");
        }
        if (chamado.getStatusEnum() == Status.CANCELADO) {
            throw new BusinessException("Chamado cancelado não pode ser alterado.");
        }
    }

    /** Marca o primeiro retorno da equipe, se ainda não houver. */
    public void registrarPrimeiraResposta(Chamado chamado) {
        if (chamado.getDataPrimeiraResposta() == null) {
            chamado.setDataPrimeiraResposta(LocalDateTime.now());
        }
    }

    @Transactional
    public void salvar(Chamado chamado) {
        chamadoRepository.save(chamado);
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private void garantirTransicaoPermitida(Usuario solicitante, Status atual, Status novo) {
        if (solicitante.ehAtendente()) return;

        // Cliente tem um conjunto restrito de ações sobre o próprio chamado:
        // cancelar enquanto ninguém atendeu, confirmar a solução ou reabrir.
        boolean cancelarProprio = novo == Status.CANCELADO && atual == Status.ABERTO;
        boolean confirmarSolucao = novo == Status.ENCERRADO && atual == Status.RESOLVIDO;
        boolean reabrir = novo == Status.EM_ANDAMENTO
                && (atual == Status.RESOLVIDO || atual == Status.ENCERRADO);

        if (!cancelarProprio && !confirmarSolucao && !reabrir) {
            throw new OperacaoNaoPermitidaException(
                    "Como cliente você pode cancelar um chamado ainda não atendido, "
                    + "confirmar a solução ou reabrir o chamado.");
        }
    }

    private void aplicarEfeitosDoStatus(Chamado chamado, Usuario solicitante,
                                        Status atual, Status novo) {
        if (novo.ehFinal()) {
            chamado.setDataFechamento(LocalDateTime.now());
            if (novo == Status.ENCERRADO) {
                historicoService.registrarFechamento(chamado, solicitante);
            }
        } else {
            // Reabertura: o chamado volta a contar prazo e perde a data de fechamento.
            if (atual.ehFinal() || atual == Status.RESOLVIDO) {
                historicoService.registrarReabertura(chamado, solicitante);
            }
            chamado.setDataFechamento(null);
        }

        if (novo == Status.EM_ANDAMENTO && solicitante.ehAtendente()) {
            registrarPrimeiraResposta(chamado);
            if (chamado.getTecnico() == null) {
                chamado.setTecnico(solicitante);
            }
        }
    }

    private void aplicarTecnico(Chamado chamado, Long tecnicoId, Usuario solicitante) {
        Long atualId = chamado.getTecnico() != null ? chamado.getTecnico().getId() : null;
        if (Objects.equals(atualId, tecnicoId)) {
            return;
        }

        String anterior = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "—";
        if (tecnicoId == null) {
            chamado.setTecnico(null);
            historicoService.registrarAtribuicaoTecnico(chamado, solicitante, anterior, "—");
            return;
        }

        Usuario tecnico = carregarTecnico(tecnicoId);
        chamado.setTecnico(tecnico);
        historicoService.registrarAtribuicaoTecnico(chamado, solicitante, anterior, tecnico.getNome());
    }

    private Usuario carregarTecnico(Long tecnicoId) {
        Usuario tecnico = usuarioService.findById(tecnicoId);
        if (!tecnico.ehAtendente()) {
            throw new BusinessException(
                    "O usuário " + tecnico.getNome() + " não possui perfil de técnico.");
        }
        if (Boolean.FALSE.equals(tecnico.getAtivo())) {
            throw new BusinessException("Não é possível atribuir a um usuário inativo.");
        }
        return tecnico;
    }

    private Prioridade converterPrioridade(Integer codigo) {
        try {
            return Prioridade.fromCodigo(codigo);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Prioridade inválida.");
        }
    }

    private Categoria converterCategoria(Integer codigo) {
        if (codigo == null) return Categoria.OUTRO;
        try {
            return Categoria.fromCodigo(codigo);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Categoria inválida.");
        }
    }

    /** Protocolo legível no formato CH-AAAA-NNNNNN. */
    private String gerarNumeroProtocolo() {
        String prefixo = "CH-" + Year.now().getValue() + "-";
        String ultimo = chamadoRepository.findUltimoNumeroDoAno(prefixo);

        long sequencial = 1L;
        if (ultimo != null && ultimo.length() > prefixo.length()) {
            try {
                sequencial = Long.parseLong(ultimo.substring(prefixo.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("Protocolo fora do padrão esperado: {}", ultimo);
            }
        }
        return prefixo + String.format("%06d", sequencial);
    }

    /** Completa a resposta com os contadores usados na listagem e no detalhe. */
    private ChamadoResponse montarResposta(Chamado chamado) {
        Long id = chamado.getId();
        return Mapper.toChamadoResponse(
                chamado,
                avaliacaoRepository.existsByChamadoId(id),
                comentarioRepository.countByChamadoId(id),
                anexoRepository.countByChamadoId(id));
    }

    /** Usado pelos testes e por serviços vizinhos que já têm o usuário em mãos. */
    public boolean ehCliente(Usuario usuario) {
        return usuario.temPerfil(Perfil.CLIENTE);
    }
}
