package com.murilo.helpdesk.util;

import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.dto.response.UsuarioResumoResponse;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Usuario;

import java.util.stream.Collectors;

public final class Mapper {

    private Mapper() {}

    public static ChamadoResponse toChamadoResponse(Chamado c) {
        UsuarioResumoResponse tecnico = c.getTecnico() != null
                ? new UsuarioResumoResponse(c.getTecnico().getId(), c.getTecnico().getNome(), c.getTecnico().getEmail())
                : null;
        UsuarioResumoResponse cliente = new UsuarioResumoResponse(
                c.getCliente().getId(), c.getCliente().getNome(), c.getCliente().getEmail());
        return new ChamadoResponse(
                c.getId(), c.getTitulo(), c.getObservacoes(),
                c.getStatus(), c.getPrioridade(),
                tecnico, cliente,
                c.getDataAbertura(), c.getDataFechamento(), c.getDataAtualizacao());
    }

    public static UsuarioResponse toUsuarioResponse(Usuario u) {
        var perfis = u.getPerfis().stream()
                .map(p -> p.getDescricao())
                .collect(Collectors.toSet());
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), perfis, u.getDataCriacao());
    }

    public static UsuarioResumoResponse toUsuarioResumo(Usuario u) {
        return new UsuarioResumoResponse(u.getId(), u.getNome(), u.getEmail());
    }
}
