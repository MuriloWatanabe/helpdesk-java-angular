package com.murilo.helpdesk.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Enums de domínio — conversão por código e transições")
class StatusTest {

    @Test
    @DisplayName("fromCodigo respeita os códigos gravados, não a ordem de declaração")
    void fromCodigoUsaCodigoNaoOrdinal() {

        assertThat(Status.fromCodigo(2)).isEqualTo(Status.ENCERRADO);
        assertThat(Status.fromCodigo(3)).isEqualTo(Status.AGUARDANDO_CLIENTE);
        assertThat(Status.fromCodigo(0)).isEqualTo(Status.ABERTO);
    }

    @Test
    @DisplayName("código desconhecido gera erro claro em vez de estourar índice")
    void codigoInvalido() {
        assertThatThrownBy(() -> Status.fromCodigo(42))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status inválido");

        assertThatThrownBy(() -> Prioridade.fromCodigo(42))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Perfil.fromCodigo(42))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fromCodigo(null) devolve null sem lançar")
    void codigoNulo() {
        assertThat(Status.fromCodigo(null)).isNull();
        assertThat(Prioridade.fromCodigo(null)).isNull();
        assertThat(Categoria.fromCodigo(null)).isNull();
    }

    @Test
    @DisplayName("transições coerentes são permitidas e as demais não")
    void transicoes() {
        assertThat(Status.ABERTO.podeIrPara(Status.EM_ANDAMENTO)).isTrue();
        assertThat(Status.RESOLVIDO.podeIrPara(Status.ENCERRADO)).isTrue();
        assertThat(Status.ENCERRADO.podeIrPara(Status.EM_ANDAMENTO)).isTrue();


        assertThat(Status.CANCELADO.podeIrPara(Status.EM_ANDAMENTO)).isFalse();
        assertThat(Status.ABERTO.podeIrPara(Status.ENCERRADO)).isFalse();
    }

    @Test
    @DisplayName("estados finais e pendentes são classificados corretamente")
    void classificacao() {
        assertThat(Status.ENCERRADO.ehFinal()).isTrue();
        assertThat(Status.CANCELADO.ehFinal()).isTrue();
        assertThat(Status.RESOLVIDO.ehFinal()).isFalse();

        assertThat(Status.codigosPendentes())
                .containsExactlyInAnyOrder(
                        Status.ABERTO.getCodigo(),
                        Status.EM_ANDAMENTO.getCodigo(),
                        Status.AGUARDANDO_CLIENTE.getCodigo());
    }

    @Test
    @DisplayName("prazo de SLA fica mais curto conforme a prioridade sobe")
    void slaPorPrioridade() {
        assertThat(Prioridade.BAIXA.getHorasSla()).isGreaterThan(Prioridade.MEDIA.getHorasSla());
        assertThat(Prioridade.MEDIA.getHorasSla()).isGreaterThan(Prioridade.ALTA.getHorasSla());
        assertThat(Prioridade.ALTA.getHorasSla()).isGreaterThan(Prioridade.URGENTE.getHorasSla());
    }
}
