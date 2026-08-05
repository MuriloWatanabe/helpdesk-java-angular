package com.murilo.helpdesk.dto.response;

/** Par rótulo/total usado nos gráficos e tabelas de relatório. */
public record ContagemResponse(Integer codigo, String rotulo, long total) {}
