package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.request.ChamadoFiltro;
import com.murilo.helpdesk.dto.response.ChamadoResponse;
import com.murilo.helpdesk.dto.response.ContagemResponse;
import com.murilo.helpdesk.dto.response.DashboardStatsResponse;
import com.murilo.helpdesk.dto.response.SerieDiariaResponse;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Categoria;
import com.murilo.helpdesk.model.enums.Prioridade;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.repository.AvaliacaoRepository;
import com.murilo.helpdesk.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DIAS_SERIE = 14;
    private static final int HORAS_ALERTA_SLA = 8;

    private final ChamadoRepository chamadoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ChamadoService chamadoService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(String emailSolicitante) {
        Usuario usuario = usuarioService.findByEmail(emailSolicitante);

        if (usuario.ehSomenteCliente()) {
            return statsDoCliente(usuario);
        }
        if (usuario.ehAdmin()) {
            return statsGlobais(usuario);
        }
        return statsDoTecnico(usuario);
    }


    private DashboardStatsResponse statsGlobais(Usuario usuario) {
        Map<Integer, Long> porStatus = mapear(chamadoRepository.contarPorStatus());
        LocalDateTime agora = LocalDateTime.now();
        List<Integer> ativos = List.of(Status.ABERTO.getCodigo(), Status.EM_ANDAMENTO.getCodigo());

        return new DashboardStatsResponse(
                "GLOBAL",
                total(porStatus),
                porStatus.getOrDefault(Status.ABERTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.EM_ANDAMENTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.AGUARDANDO_CLIENTE.getCodigo(), 0L),
                porStatus.getOrDefault(Status.RESOLVIDO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.ENCERRADO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.CANCELADO.getCodigo(), 0L),
                chamadoRepository.contarSlaVencido(agora, ativos),
                chamadoRepository.contarSlaEmRisco(agora, agora.plusHours(HORAS_ALERTA_SLA), ativos),
                contarSemTecnico(),
                tempoMedioResolucaoHoras(),
                avaliacaoRepository.calcularNotaMedia(),
                avaliacaoRepository.count(),
                contagensPorPrioridade(chamadoRepository.contarPorPrioridade()),
                contagensPorCategoria(chamadoRepository.contarPorCategoria()),
                contagensPorTecnico(),
                serieDeAberturas(),
                chamadosRecentes(usuario, null, null));
    }

    private DashboardStatsResponse statsDoTecnico(Usuario usuario) {
        Map<Integer, Long> porStatus = mapear(
                chamadoRepository.contarPorStatusDoTecnico(usuario.getId()));
        LocalDateTime agora = LocalDateTime.now();
        List<Integer> ativos = List.of(Status.ABERTO.getCodigo(), Status.EM_ANDAMENTO.getCodigo());

        return new DashboardStatsResponse(
                "TECNICO",
                total(porStatus),
                porStatus.getOrDefault(Status.ABERTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.EM_ANDAMENTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.AGUARDANDO_CLIENTE.getCodigo(), 0L),
                porStatus.getOrDefault(Status.RESOLVIDO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.ENCERRADO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.CANCELADO.getCodigo(), 0L),

                chamadoRepository.contarSlaVencido(agora, ativos),
                chamadoRepository.contarSlaEmRisco(agora, agora.plusHours(HORAS_ALERTA_SLA), ativos),
                contarSemTecnico(),
                tempoMedioResolucaoHoras(),
                avaliacaoRepository.calcularNotaMediaPorTecnico(usuario.getId()),
                avaliacaoRepository.count(),
                contagensPorPrioridade(chamadoRepository.contarPorPrioridade()),
                contagensPorCategoria(chamadoRepository.contarPorCategoria()),
                contagensPorTecnico(),
                serieDeAberturas(),
                chamadosRecentes(usuario, null, usuario.getId()));
    }

    private DashboardStatsResponse statsDoCliente(Usuario usuario) {
        Map<Integer, Long> porStatus = mapear(
                chamadoRepository.contarPorStatusDoCliente(usuario.getId()));

        return new DashboardStatsResponse(
                "CLIENTE",
                total(porStatus),
                porStatus.getOrDefault(Status.ABERTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.EM_ANDAMENTO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.AGUARDANDO_CLIENTE.getCodigo(), 0L),
                porStatus.getOrDefault(Status.RESOLVIDO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.ENCERRADO.getCodigo(), 0L),
                porStatus.getOrDefault(Status.CANCELADO.getCodigo(), 0L),

                0L, 0L, 0L,
                null,
                null,
                0L,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                chamadosRecentes(usuario, usuario.getId(), null));
    }


    private List<ChamadoResponse> chamadosRecentes(Usuario usuario, Long clienteId, Long tecnicoId) {
        var filtro = new ChamadoFiltro(null, null, null, null, tecnicoId, clienteId,
                null, null, null, null, null);
        var paginacao = PageRequest.of(0, 5, Sort.by("dataAbertura").descending());

        return chamadoService.listar(filtro, paginacao, usuario.getEmail()).getContent();
    }

    private long contarSemTecnico() {
        var filtro = new ChamadoFiltro(null, null, null, null, null, null,
                true, null, true, null, null);
        return chamadoRepository.count(
                com.murilo.helpdesk.repository.spec.ChamadoSpecs.montar(filtro, null));
    }


    private Double tempoMedioResolucaoHoras() {
        List<Object[]> datas = chamadoRepository.buscarDatasDeResolucao();
        if (datas.isEmpty()) return null;

        long somaMinutos = 0;
        int considerados = 0;
        for (Object[] linha : datas) {
            if (linha[0] instanceof LocalDateTime abertura
                    && linha[1] instanceof LocalDateTime fechamento) {
                somaMinutos += Duration.between(abertura, fechamento).toMinutes();
                considerados++;
            }
        }
        if (considerados == 0) return null;

        double horas = (somaMinutos / (double) considerados) / 60.0;
        return Math.round(horas * 10.0) / 10.0;
    }

    private List<SerieDiariaResponse> serieDeAberturas() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(DIAS_SERIE - 1L);

        Map<LocalDate, Long> contagem = new TreeMap<>();
        for (int i = 0; i < DIAS_SERIE; i++) {
            contagem.put(inicio.plusDays(i), 0L);
        }
        for (LocalDateTime abertura : chamadoRepository.buscarAberturasDesde(inicio.atStartOfDay())) {
            LocalDate dia = abertura.toLocalDate();
            contagem.computeIfPresent(dia, (k, v) -> v + 1);
        }

        List<SerieDiariaResponse> serie = new ArrayList<>();
        contagem.forEach((dia, total) -> serie.add(new SerieDiariaResponse(dia, total)));
        return serie;
    }

    private List<ContagemResponse> contagensPorPrioridade(List<Object[]> linhas) {
        Map<Integer, Long> mapa = mapear(linhas);
        List<ContagemResponse> resultado = new ArrayList<>();
        for (Prioridade p : Prioridade.values()) {
            resultado.add(new ContagemResponse(p.getCodigo(), p.getDescricao(),
                    mapa.getOrDefault(p.getCodigo(), 0L)));
        }
        return resultado;
    }

    private List<ContagemResponse> contagensPorCategoria(List<Object[]> linhas) {
        Map<Integer, Long> mapa = mapear(linhas);
        List<ContagemResponse> resultado = new ArrayList<>();
        for (Categoria c : Categoria.values()) {
            long total = mapa.getOrDefault(c.getCodigo(), 0L);
            if (total > 0) {
                resultado.add(new ContagemResponse(c.getCodigo(), c.getDescricao(), total));
            }
        }
        resultado.sort((a, b) -> Long.compare(b.total(), a.total()));
        return resultado;
    }

    private List<ContagemResponse> contagensPorTecnico() {
        List<ContagemResponse> resultado = new ArrayList<>();
        for (Object[] linha : chamadoRepository.contarPorTecnico()) {
            String nome = linha[1] != null ? linha[1].toString() : "Sem técnico";
            long total = linha[2] instanceof Number n ? n.longValue() : 0L;
            resultado.add(new ContagemResponse(null, nome, total));
        }
        return resultado;
    }

    private Map<Integer, Long> mapear(List<Object[]> linhas) {
        Map<Integer, Long> mapa = new HashMap<>();
        for (Object[] linha : linhas) {
            if (linha[0] instanceof Number codigo && linha[1] instanceof Number total) {
                mapa.put(codigo.intValue(), total.longValue());
            }
        }
        return mapa;
    }

    private long total(Map<Integer, Long> porStatus) {
        return porStatus.values().stream().mapToLong(Long::longValue).sum();
    }
}
