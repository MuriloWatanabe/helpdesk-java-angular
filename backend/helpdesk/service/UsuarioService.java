package com.murilo.helpdesk.service;

import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado! ID: " + id));
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario create(Usuario obj) {
        return usuarioRepository.save(obj);
    }

    public Usuario update(Long id, Usuario obj) {
        Usuario entity = findById(id);
        entity.setNome(obj.getNome());
        entity.setEmail(obj.getEmail());
        return usuarioRepository.save(entity);
    }

    public void delete(Long id) {
        findById(id);
        usuarioRepository.deleteById(id);
    }
}
