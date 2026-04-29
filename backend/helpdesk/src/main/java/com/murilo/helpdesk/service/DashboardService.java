package com.murilo.helpdesk.service;

import com.murilo.helpdesk.dto.response.DashboardStatsResponse;
import com.murilo.helpdesk.repository.ChamadoRepository;
import com.murilo.helpdesk.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ChamadoRepository chamadoRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long abertos     = chamadoRepository.countByStatus(0);
        long emAndamento = chamadoRepository.countByStatus(1);
        long encerrados  = chamadoRepository.countByStatus(2);
        long total       = chamadoRepository.count();

        var recentes = chamadoRepository
                .findAll(PageRequest.of(0, 5, Sort.by("dataAbertura").descending()))
                .getContent().stream()
                .map(Mapper::toChamadoResponse)
                .toList();

        return new DashboardStatsResponse(abertos, emAndamento, encerrados, total, recentes);
    }
}
