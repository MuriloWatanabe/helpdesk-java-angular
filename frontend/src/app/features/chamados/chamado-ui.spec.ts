import { Chamado } from '../../core/models/chamado.model';
import {
  classePrioridade,
  classeStatus,
  iniciais,
  textoSla,
  transicoesDoCliente,
  transicoesPermitidas,
} from './chamado-ui';

function chamado(parcial: Partial<Chamado>): Chamado {
  return {
    id: 1,
    numero: 'CH-2026-000001',
    titulo: 'Teste',
    observacoes: 'Descrição do problema de teste',
    status: 1,
    statusLabel: 'Em andamento',
    prioridade: 2,
    prioridadeLabel: 'Alta',
    categoria: 0,
    categoriaLabel: 'Rede e internet',
    cliente: { id: 10, nome: 'Ana', email: 'ana@test.com' },
    dataAbertura: '2026-08-01T10:00:00',
    slaVencido: false,
    encerrado: false,
    avaliado: false,
    totalComentarios: 0,
    totalAnexos: 0,
    ...parcial,
  } as Chamado;
}

describe('chamado-ui', () => {
  describe('classes visuais', () => {
    it('mapeia cada status para a sua classe', () => {
      expect(classeStatus(0)).toBe('badge-aberto');
      expect(classeStatus(2)).toBe('badge-encerrado');
      expect(classeStatus(4)).toBe('badge-resolvido');
    });

    it('usa uma classe neutra para código desconhecido', () => {
      expect(classeStatus(99)).toBe('badge-encerrado');
      expect(classePrioridade(99)).toBe('badge-baixa');
    });

    it('mapeia urgente para a classe mais forte', () => {
      expect(classePrioridade(3)).toBe('badge-urgente');
    });
  });

  describe('textoSla', () => {
    it('mostra o atraso quando o prazo já passou', () => {
      const texto = textoSla(
        chamado({ prazoSla: '2026-08-01T12:00:00', horasRestantesSla: -5, slaVencido: true }),
      );
      expect(texto).toBe('Vencido há 5h');
    });

    it('converte atrasos longos para dias', () => {
      const texto = textoSla(
        chamado({ prazoSla: '2026-07-01T12:00:00', horasRestantesSla: -50, slaVencido: true }),
      );
      expect(texto).toBe('Vencido há 2d');
    });

    it('mostra o tempo restante quando ainda há prazo', () => {
      expect(textoSla(chamado({ prazoSla: '2026-08-09T12:00:00', horasRestantesSla: 6 })))
        .toBe('Faltam 6h');
    });

    it('avisa quando falta menos de uma hora', () => {
      expect(textoSla(chamado({ prazoSla: '2026-08-09T12:00:00', horasRestantesSla: 0 })))
        .toBe('Vence em menos de 1h');
    });

    it('não exibe prazo para chamado já encerrado', () => {
      expect(textoSla(chamado({ prazoSla: '2026-08-09T12:00:00', encerrado: true }))).toBe('');
    });

    it('não exibe prazo quando o chamado não tem SLA', () => {
      expect(textoSla(chamado({ prazoSla: null }))).toBe('');
    });
  });

  describe('transições de status', () => {
    it('espelha as regras do backend para a equipe', () => {
      expect(transicoesPermitidas(0)).toContain(1); // Aberto → Em andamento
      expect(transicoesPermitidas(4)).toContain(2); // Resolvido → Encerrado
      expect(transicoesPermitidas(5)).toHaveLength(0); // Cancelado é terminal
    });

    it('limita o cliente a cancelar, confirmar e reabrir', () => {
      expect(transicoesDoCliente(0)).toEqual([5]); // cancelar o que ninguém atendeu
      expect(transicoesDoCliente(4)).toEqual([2, 1]); // confirmar ou reabrir
      expect(transicoesDoCliente(1)).toEqual([]); // nada durante o atendimento
    });
  });

  describe('iniciais', () => {
    it('usa as duas primeiras palavras', () => {
      expect(iniciais('Carlos Eduardo Silva')).toBe('CE');
    });

    it('devolve interrogação para valores vazios', () => {
      expect(iniciais('')).toBe('?');
      expect(iniciais(undefined)).toBe('?');
    });
  });
});
