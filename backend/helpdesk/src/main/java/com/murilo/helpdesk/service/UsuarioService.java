package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.AlterarSenhaRequest;
import com.murilo.helpdesk.dto.request.AtualizarPerfilRequest;
import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.dto.response.UsuarioDiretorioResponse;
import com.murilo.helpdesk.exception.BusinessException;
import com.murilo.helpdesk.exception.OperacaoNaoPermitidaException;
import com.murilo.helpdesk.exception.ResourceNotFoundException;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.repository.UsuarioRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String EMAIL_DUPLICADO = "Já existe um usuário com este e-mail.";

    private final UsuarioRepository usuarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuário", id));
    }

    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado para o e-mail informado."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findByIdAsResponse(Long id) {
        return Mapper.toUsuarioResponse(findById(id));
    }


    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Integer perfil, Boolean ativo, String busca) {
        String termo = busca == null ? null : busca.trim().toLowerCase();

        return usuarioRepository.findAll().stream()
                .filter(u -> perfil == null || u.getPerfisCodigos().contains(perfil))
                .filter(u -> ativo == null || ativo.equals(!Boolean.FALSE.equals(u.getAtivo())))
                .filter(u -> termo == null || termo.isEmpty()
                        || u.getNome().toLowerCase().contains(termo)
                        || u.getEmail().toLowerCase().contains(termo))
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(Mapper::toUsuarioResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return listar(null, null, null);
    }

    /**
     * Diretório enxuto para os atendentes selecionarem clientes e responsáveis.
     * Não expõe telefone, cargo, situação administrativa nem datas de acesso.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDiretorioResponse> listarDiretorioAtivo() {
        return usuarioRepository.findAll().stream()
                .filter(u -> !Boolean.FALSE.equals(u.getAtivo()))
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(u -> new UsuarioDiretorioResponse(
                        u.getId(),
                        u.getNome(),
                        u.getEmail(),
                        u.getPerfis().stream().map(Perfil::getDescricao).collect(java.util.stream.Collectors.toSet())))
                .toList();
    }


    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw BusinessException.conflito(EMAIL_DUPLICADO);
        }
        validarPerfis(request.perfis());

        if (request.senha() == null || request.senha().isBlank()) {
            throw new BusinessException("Informe uma senha para o novo usuário.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setTelefone(request.telefone());
        usuario.setCargo(request.cargo());
        usuario.setAtivo(request.ativo() == null || request.ativo());
        usuario.updatePerfis(request.perfis());

        log.info("Usuário criado: {}", email);
        return Mapper.toUsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario existente = findById(id);
        String email = normalizarEmail(request.email());

        if (!existente.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw BusinessException.conflito(EMAIL_DUPLICADO);
        }
        validarPerfis(request.perfis());


        if (existente.ehAdmin() && !request.perfis().contains(Perfil.ADMIN.getCodigo())) {
            garantirOutroAdminAtivo(existente.getId());
        }

        existente.setNome(request.nome().trim());
        existente.setEmail(email);
        existente.setTelefone(request.telefone());
        existente.setCargo(request.cargo());
        existente.updatePerfis(request.perfis());

        if (request.ativo() != null) {
            if (!request.ativo() && existente.ehAdmin()) {
                garantirOutroAdminAtivo(existente.getId());
            }
            existente.setAtivo(request.ativo());
        }

        if (request.senha() != null && !request.senha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(request.senha()));
        }

        return Mapper.toUsuarioResponse(usuarioRepository.save(existente));
    }

    @Transactional
    public UsuarioResponse updatePerfis(Long id, Set<Integer> novosPerfis) {
        validarPerfis(novosPerfis);
        Usuario usuario = findById(id);

        if (usuario.ehAdmin() && !novosPerfis.contains(Perfil.ADMIN.getCodigo())) {
            garantirOutroAdminAtivo(usuario.getId());
        }

        usuario.updatePerfis(novosPerfis);
        return Mapper.toUsuarioResponse(usuarioRepository.save(usuario));
    }


    @Transactional
    public UsuarioResponse alterarSituacao(Long id, boolean ativo, String emailSolicitante) {
        Usuario solicitante = findByEmail(emailSolicitante);
        Usuario alvo = findById(id);

        if (alvo.getId().equals(solicitante.getId())) {
            throw new BusinessException("Você não pode desativar a própria conta.");
        }
        if (!ativo && alvo.ehAdmin()) {
            garantirOutroAdminAtivo(alvo.getId());
        }

        alvo.setAtivo(ativo);
        log.info("Usuário {} {}", alvo.getEmail(), ativo ? "reativado" : "desativado");
        return Mapper.toUsuarioResponse(usuarioRepository.save(alvo));
    }


    @Transactional
    public void delete(Long id, String emailSolicitante) {
        Usuario solicitante = findByEmail(emailSolicitante);
        Usuario alvo = findById(id);

        if (alvo.getId().equals(solicitante.getId())) {
            throw new BusinessException("Você não pode excluir a própria conta.");
        }
        if (chamadoRepository.existsByClienteIdOrTecnicoId(id, id)) {
            throw BusinessException.conflito(
                    "Este usuário possui chamados vinculados e não pode ser excluído. "
                    + "Desative a conta para bloquear o acesso mantendo o histórico.");
        }
        if (alvo.ehAdmin()) {
            garantirOutroAdminAtivo(alvo.getId());
        }

        usuarioRepository.delete(alvo);
        log.info("Usuário {} excluído por {}", alvo.getEmail(), solicitante.getEmail());
    }


    @Transactional
    public void delete(Long id) {
        Usuario alvo = findById(id);
        if (chamadoRepository.existsByClienteIdOrTecnicoId(id, id)) {
            throw BusinessException.conflito(
                    "Este usuário possui chamados vinculados e não pode ser excluído.");
        }
        usuarioRepository.delete(alvo);
    }


    @Transactional
    public UsuarioResponse atualizarMeuPerfil(String emailAtual, AtualizarPerfilRequest request) {
        Usuario usuario = findByEmail(emailAtual);
        String novoEmail = normalizarEmail(request.email());

        if (!usuario.getEmail().equals(novoEmail) && usuarioRepository.existsByEmail(novoEmail)) {
            throw BusinessException.conflito(EMAIL_DUPLICADO);
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(novoEmail);
        usuario.setTelefone(request.telefone());
        usuario.setCargo(request.cargo());

        return Mapper.toUsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(String emailAtual, AlterarSenhaRequest request) {
        Usuario usuario = findByEmail(emailAtual);

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("A senha atual está incorreta.");
        }
        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new BusinessException("A nova senha deve ser diferente da atual.");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        log.info("Senha alterada para {}", usuario.getEmail());
    }

    @Transactional
    public void registrarAcesso(String email) {
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            u.setUltimoAcesso(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }


    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }


    private void validarPerfis(Set<Integer> perfis) {
        if (perfis == null || perfis.isEmpty()) {
            throw new BusinessException("Selecione ao menos um perfil.");
        }
        for (Integer codigo : perfis) {
            if (!Perfil.codigoValido(codigo)) {
                throw new BusinessException("Perfil inválido: " + codigo);
            }
        }
    }

    private void garantirOutroAdminAtivo(Long idIgnorado) {
        boolean existeOutro = usuarioRepository.findAll().stream()
                .anyMatch(u -> !u.getId().equals(idIgnorado)
                        && u.ehAdmin()
                        && !Boolean.FALSE.equals(u.getAtivo()));

        if (!existeOutro) {
            throw new BusinessException(
                    "Este é o último administrador ativo do sistema. "
                    + "Promova outro usuário antes de continuar.");
        }
    }


    @Transactional
    public void definirSenha(Usuario usuario, String novaSenha) {
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Usuario> buscarPorEmailOpcional(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email));
    }


    public void garantirPodeVerUsuario(Usuario solicitante, Long idAlvo) {
        if (solicitante.ehAtendente() || solicitante.getId().equals(idAlvo)) return;
        throw new OperacaoNaoPermitidaException("Você só pode consultar os próprios dados.");
    }
}
