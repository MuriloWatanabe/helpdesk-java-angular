package com.murilo.helpdesk.repository.spec;

import com.murilo.helpdesk.dto.request.ChamadoFiltro;
import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Monta a consulta de chamados a partir dos filtros da tela. */
public final class ChamadoSpecs {

    private ChamadoSpecs() {}

    /**
     * @param filtro          filtros informados pelo usuário (pode ser nulo)
     * @param restringirCliente quando informado, limita aos chamados desse cliente
     *                          (usado para o perfil CLIENTE, que nunca enxerga
     *                          chamados de terceiros)
     */
    public static Specification<Chamado> montar(ChamadoFiltro filtro, Long restringirCliente) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (restringirCliente != null) {
                predicados.add(cb.equal(root.get("cliente").get("id"), restringirCliente));
            }

            if (filtro != null) {
                if (filtro.temBusca()) {
                    String termo = "%" + filtro.q().trim().toLowerCase() + "%";
                    predicados.add(cb.or(
                            cb.like(cb.lower(root.<String>get("titulo")), termo),
                            cb.like(cb.lower(root.<String>get("observacoes")), termo),
                            cb.like(cb.lower(root.<String>get("numero")), termo),
                            cb.like(cb.lower(root.get("cliente").<String>get("nome")), termo)
                    ));
                }

                if (filtro.status() != null) {
                    predicados.add(cb.equal(root.get("status"), filtro.status()));
                }

                if (filtro.prioridade() != null) {
                    predicados.add(cb.equal(root.get("prioridade"), filtro.prioridade()));
                }

                if (filtro.categoria() != null) {
                    predicados.add(cb.equal(root.get("categoria"), filtro.categoria()));
                }

                if (filtro.tecnicoId() != null) {
                    predicados.add(cb.equal(root.get("tecnico").get("id"), filtro.tecnicoId()));
                }

                if (filtro.clienteId() != null && restringirCliente == null) {
                    predicados.add(cb.equal(root.get("cliente").get("id"), filtro.clienteId()));
                }

                if (Boolean.TRUE.equals(filtro.semTecnico())) {
                    predicados.add(cb.isNull(root.get("tecnico")));
                }

                if (Boolean.TRUE.equals(filtro.apenasPendentes())) {
                    predicados.add(root.get("status").in(Status.codigosPendentes()));
                }

                if (Boolean.TRUE.equals(filtro.slaVencido())) {
                    predicados.add(cb.isNotNull(root.get("prazoSla")));
                    predicados.add(cb.lessThan(root.get("prazoSla"), LocalDateTime.now()));
                    predicados.add(root.get("status").in(
                            List.of(Status.ABERTO.getCodigo(), Status.EM_ANDAMENTO.getCodigo())));
                }

                if (filtro.dataInicio() != null) {
                    predicados.add(cb.greaterThanOrEqualTo(
                            root.get("dataAbertura"), filtro.dataInicio().atStartOfDay()));
                }

                if (filtro.dataFim() != null) {
                    predicados.add(cb.lessThan(
                            root.get("dataAbertura"), filtro.dataFim().plusDays(1).atStartOfDay()));
                }
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
