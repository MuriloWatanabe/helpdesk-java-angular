package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service responsável pela lógica de negócios de usuários.
 * Implementa CRUD completo e gerenciamento de perfis de usuário.
 */
@Slf4j
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    /**
     * Busca um usuário pelo ID.
     * @param id ID do usuário
     * @return Usuário encontrado
     * @throws RuntimeException se não encontrar o usuário
     */
    public Usuario findById(Long id) {
        log.info("Buscando usuário com ID: {}", id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com ID: {}", id);
                    return new RuntimeException("Usuário não encontrado! ID: " + id);
                });
    }

    /**
     * Busca um usuário pelo email.
     * @param email Email do usuário
     * @return Usuário encontrado
     */
    public Usuario findByEmail(String email) {
        log.info("Buscando usuário com email: {}", email);
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com email: {}", email);
                    return new RuntimeException("Usuário não encontrado! Email: " + email);
                });
    }

    /**
     * Lista todos os usuários do sistema.
     * @return Lista de usuários
     */
    public List<Usuario> findAll() {
        log.info("Listando todos os usuários");
        return usuarioRepository.findAll();
    }

    /**
     * Cria um novo usuário no sistema.
     * @param usuario Dados do usuário
     * @return Usuário criado
     */
    @Transactional
    public Usuario create(Usuario usuario) {
        log.info("Criando novo usuário com email: {}", usuario.getEmail());
        
        // Verifica se email já existe
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(u -> {
            log.error("Email já cadastrado: {}", usuario.getEmail());
            throw new RuntimeException("Email já cadastrado no sistema!");
        });
        
        // Codifica a senha se houver PasswordEncoder configurado
        if (passwordEncoder != null) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        
        Usuario usuarioCriado = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso! ID: {}", usuarioCriado.getId());
        return usuarioCriado;
    }

    /**
     * Atualiza um usuário existente.
     * @param id ID do usuário
     * @param usuario Dados atualizados
     * @return Usuário atualizado
     */
    @Transactional
    public Usuario update(Long id, Usuario usuario) {
        log.info("Atualizando usuário com ID: {}", id);
        
        Usuario usuarioExistente = findById(id);
        usuarioExistente.setNome(usuario.getNome());
        
        // Se o email foi alterado, verifica se já existe
        if (!usuarioExistente.getEmail().equals(usuario.getEmail())) {
            usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(u -> {
                log.error("Email já cadastrado: {}", usuario.getEmail());
                throw new RuntimeException("Email já cadastrado no sistema!");
            });
            usuarioExistente.setEmail(usuario.getEmail());
        }
        
        // Se a senha foi alterada, codifica a nova senha
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            if (passwordEncoder != null) {
                usuarioExistente.setSenha(passwordEncoder.encode(usuario.getSenha()));
            } else {
                usuarioExistente.setSenha(usuario.getSenha());
            }
        }
        
        Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
        log.info("Usuário atualizado com sucesso! ID: {}", id);
        return usuarioAtualizado;
    }

    /**
     * Deleta um usuário do sistema.
     * @param id ID do usuário
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando usuário com ID: {}", id);
        Usuario usuario = findById(id);
        usuarioRepository.deleteById(id);
        log.info("Usuário deletado com sucesso! ID: {}", id);
    }
}
