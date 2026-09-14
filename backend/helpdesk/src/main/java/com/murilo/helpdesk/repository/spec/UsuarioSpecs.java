package com.murilo.helpdesk.repository.spec;

import com.murilo.helpdesk.model.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public final class UsuarioSpecs {

    private UsuarioSpecs() {}


    public static Specification<Usuario> montar(Integer perfil, Boolean ativo, String busca) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (perfil != null) {
                predicados.add(cb.isMember(perfil, root.get("perfis")));
            }
            if (ativo != null) {
                predicados.add(cb.equal(root.get("ativo"), ativo));
            }
            if (busca != null && !busca.isBlank()) {
                String termo = "%" + busca.trim().toLowerCase() + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), termo),
                        cb.like(cb.lower(root.get("email")), termo)));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
