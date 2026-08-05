package com.murilo.helpdesk.util;

import com.murilo.helpdesk.dto.response.AnexoResponse;
import com.murilo.helpdesk.dto.response.AvaliacaoResponse;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.dto.response.ComentarioResponse;
import com.murilo.helpdesk.dto.response.HistoricoResponse;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.dto.response.UsuarioResumoResponse;
import com.murilo.helpdesk.model.Anexo;
import com.murilo.helpdesk.model.Avaliacao;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.Comentario;
import com.murilo.helpdesk.model.HistoricoChamado;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class Mapper {

    private Mapper() {}

    // ------------------------------------------------------------------
    // Chamado
    // ------------------------------------------------------------------

    public static ChamadoResponse toChamadoResponse(Chamado c) {
        return toChamadoResponse(c, false, 0L, 0L);
    }

    public static ChamadoResponse toChamadoResponse(Chamado c, boolean avaliado,
                                                    long totalComentarios, long totalAnexos) {
        Status status = c.getStatusEnum();
        Prioridade prioridade = c.getPrioridadeEnum();
        Categoria categoria = c.getCategoria() == null ? null : c.getCategoriaEnum();

        return new ChamadoResponse(
                c.getId(),
                c.getNumero(),
                c.getTitulo(),
                c.getObservacoes(),
                c.getStatus(),
                status != null ? status.getDescricao() : null,
                c.getPrioridade(),
                prioridade != null ? prioridade.getDescricao() : null,
                c.getCategoria(),
                categoria != null ? categoria.getDescricao() : null,
                toUsuarioResumo(c.getTecnico()),
                toUsuarioResumo(c.getCliente()),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getDataAtualizacao(),
                c.getDataPrimeiraResposta(),
                c.getPrazoSla(),
                c.isSlaVencido(),
                c.getHorasRestantesSla(),
                status != null && status.ehFinal(),
                avaliado,
                totalComentarios,
                totalAnexos);
    }

    // ------------------------------------------------------------------
    // Usuário
    // ------------------------------------------------------------------

    public static UsuarioResponse toUsuarioResponse(Usuario u) {
        Set<Perfil> perfis = u.getPerfis();
        Set<String> roles = perfis.stream()
                .map(Perfil::getDescricao)
                .collect(Collectors.toSet());

        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone(),
                u.getCargo(),
                !Boolean.FALSE.equals(u.getAtivo()),
                roles,
                new HashSet<>(u.getPerfisCodigos()),
                perfilPrincipal(perfis),
                u.getDataCriacao(),
                u.getUltimoAcesso());
    }

    /** Perfil de maior privilégio — rótulo único exibido na interface. */
    public static String perfilPrincipal(Set<Perfil> perfis) {
        if (perfis.contains(Perfil.ADMIN))   return Perfil.ADMIN.getRotulo();
        if (perfis.contains(Perfil.TECNICO)) return Perfil.TECNICO.getRotulo();
        if (perfis.contains(Perfil.CLIENTE)) return Perfil.CLIENTE.getRotulo();
        return "Sem perfil";
    }

    public static UsuarioResumoResponse toUsuarioResumo(Usuario u) {
        if (u == null) return null;
        return new UsuarioResumoResponse(u.getId(), u.getNome(), u.getEmail());
    }

    // ------------------------------------------------------------------
    // Comentário / Histórico / Anexo / Avaliação
    // ------------------------------------------------------------------

    public static ComentarioResponse toComentarioResponse(Comentario c) {
        return new ComentarioResponse(
                c.getId(),
                c.getChamado() != null ? c.getChamado().getId() : null,
                c.getTexto(),
                Boolean.TRUE.equals(c.getInterno()),
                Boolean.TRUE.equals(c.getEditado()),
                toUsuarioResumo(c.getAutor()),
                c.getAutor() != null ? perfilPrincipal(c.getAutor().getPerfis()) : null,
                c.getDataCriacao(),
                c.getDataAtualizacao());
    }

    public static HistoricoResponse toHistoricoResponse(HistoricoChamado h) {
        return new HistoricoResponse(
                h.getId(),
                h.getTipoAlteracao() != null ? h.getTipoAlteracao().name() : null,
                h.getDescricao(),
                h.getValorAnterior(),
                h.getValorNovo(),
                toUsuarioResumo(h.getUsuarioAlteracao()),
                h.getDataAlteracao());
    }

    public static AnexoResponse toAnexoResponse(Anexo a) {
        String mime = a.getTipoMime() != null ? a.getTipoMime() : "";
        return new AnexoResponse(
                a.getId(),
                a.getChamado() != null ? a.getChamado().getId() : null,
                a.getNomeArquivo(),
                a.getTipoMime(),
                a.getTamanho(),
                a.getTamanhoFormatado(),
                !Boolean.FALSE.equals(a.getPublico()),
                mime.startsWith("image/"),
                toUsuarioResumo(a.getUploadPor()),
                a.getDataUpload());
    }

    public static AvaliacaoResponse toAvaliacaoResponse(Avaliacao a) {
        return new AvaliacaoResponse(
                a.getId(),
                a.getChamado() != null ? a.getChamado().getId() : null,
                a.getNota(),
                a.getInterpretacaoNota(),
                a.getComentario(),
                a.getAspectosAvaliados() == null ? Set.of() : new HashSet<>(a.getAspectosAvaliados()),
                toUsuarioResumo(a.getUsuarioAvaliador()),
                a.getDataAvaliacao());
    }
}
