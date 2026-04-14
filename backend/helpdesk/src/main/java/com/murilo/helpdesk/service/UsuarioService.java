package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario findById(Long id) {
        log.info("Buscando usuário com ID: {}", id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado! ID: " + id));
    }

    public Usuario findByEmail(String email) {
        log.info("Buscando usuário com email: {}", email);
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado! Email: " + email));
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario create(Usuario usuario) {
        log.info("Criando novo usuário com email: {}", usuario.getEmail());
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já cadastrado no sistema!");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario criado = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso! ID: {}", criado.getId());
        return criado;
    }

    @Transactional
    public Usuario update(Long id, Usuario usuario) {
        log.info("Atualizando usuário com ID: {}", id);
        Usuario existente = findById(id);
        existente.setNome(usuario.getNome());

        if (!existente.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new RuntimeException("Email já cadastrado no sistema!");
            }
            existente.setEmail(usuario.getEmail());
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        Usuario atualizado = usuarioRepository.save(existente);
        log.info("Usuário atualizado com sucesso! ID: {}", id);
        return atualizado;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando usuário com ID: {}", id);
        findById(id); // valida existência
        usuarioRepository.deleteById(id);
        log.info("Usuário deletado com sucesso! ID: {}", id);
    }
}
