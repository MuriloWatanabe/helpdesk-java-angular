package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.UsuarioRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado! ID: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado! Email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream()
                .map(Mapper::toUsuarioResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findByIdAsResponse(Long id) {
        return Mapper.toUsuarioResponse(findById(id));
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        log.info("Criando usuário: {}", request.email());
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado no sistema!");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(
                request.senha() != null && !request.senha().isBlank() ? request.senha() : "123456"));
        if (request.perfis() != null && !request.perfis().isEmpty()) {
            usuario.updatePerfis(request.perfis());
        }
        return Mapper.toUsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        log.info("Atualizando usuário ID: {}", id);
        Usuario existente = findById(id);
        existente.setNome(request.nome());

        if (!existente.getEmail().equals(request.email())) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new RuntimeException("Email já cadastrado no sistema!");
            }
            existente.setEmail(request.email());
        }

        if (request.senha() != null && !request.senha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(request.senha()));
        }

        if (request.perfis() != null) {
            existente.updatePerfis(request.perfis());
        }

        return Mapper.toUsuarioResponse(usuarioRepository.save(existente));
    }

    @Transactional
    public UsuarioResponse updatePerfis(Long id, Set<Integer> novosPerfis) {
        log.info("Atualizando perfis do usuário ID: {}", id);
        Usuario usuario = findById(id);
        usuario.updatePerfis(novosPerfis);
        return Mapper.toUsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando usuário ID: {}", id);
        findById(id);
        usuarioRepository.deleteById(id);
    }
}
