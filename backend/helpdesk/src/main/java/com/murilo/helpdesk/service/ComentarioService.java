package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ComentarioRequest;
import com.murilo.helpdesk.dto.response.ComentarioResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Comentario;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.ComentarioRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoService chamadoService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar(Long chamadoId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);
        chamadoService.garantirAcessoDeLeitura(chamado, solicitante);

        List<Comentario> comentarios = solicitante.ehAtendente()
                ? comentarioRepository.findByChamadoIdOrderByDataCriacaoAsc(chamadoId)
                : comentarioRepository.findByChamadoIdAndInternoFalseOrderByDataCriacaoAsc(chamadoId);

        return comentarios.stream().map(Mapper::toComentarioResponse).toList();
    }

    @Transactional
    public ComentarioResponse criar(Long chamadoId, ComentarioRequest request,
                                    String emailSolicitante) {
        Usuario autor = usuarioService.findByEmail(emailSolicitante);
        Chamado chamado = chamadoService.findById(chamadoId);
        chamadoService.garantirAcessoDeLeitura(chamado, autor);

        if (chamado.getStatusEnum() == Status.CANCELADO) {
            throw new BusinessException("Este chamado foi cancelado e não aceita novos comentários.");
        }


        boolean interno = Boolean.TRUE.equals(request.interno()) && autor.ehAtendente();

        Comentario comentario = Comentario.builder()
                .chamado(chamado)
                .autor(autor)
                .texto(request.texto().trim())
                .interno(interno)
                .build();

        Comentario salvo = comentarioRepository.save(comentario);
        historicoService.registrarComentario(chamado, autor);


        if (autor.ehAtendente() && !interno) {
            chamadoService.registrarPrimeiraResposta(chamado);
            chamadoService.salvar(chamado);
        }

        return Mapper.toComentarioResponse(salvo);
    }

    @Transactional
    public ComentarioResponse editar(Long comentarioId, ComentarioRequest request,
                                     String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Comentario comentario = buscar(comentarioId);

        if (!comentario.getAutor().getId().equals(solicitante.getId())) {
            throw new OperacaoNaoPermitidaException("Só é possível editar os próprios comentários.");
        }

        comentario.setTexto(request.texto().trim());
        return Mapper.toComentarioResponse(comentarioRepository.save(comentario));
    }

    @Transactional
    public void excluir(Long comentarioId, String emailSolicitante) {
        Usuario solicitante = usuarioService.findByEmail(emailSolicitante);
        Comentario comentario = buscar(comentarioId);

        boolean autor = comentario.getAutor().getId().equals(solicitante.getId());
        if (!autor && !solicitante.ehAdmin()) {
            throw new OperacaoNaoPermitidaException(
                    "Apenas o autor ou um administrador pode excluir este comentário.");
        }

        comentarioRepository.delete(comentario);
    }

    private Comentario buscar(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Comentário", id));
    }
}
