import { Chamado, StatusChamado } from '../../core/models/chamado.model';


const CLASSES_STATUS: Record<number, string> = {
  0: 'badge-aberto',
  1: 'badge-andamento',
  2: 'badge-encerrado',
  3: 'badge-aguardando',
  4: 'badge-resolvido',
  5: 'badge-cancelado',
};

const CLASSES_PRIORIDADE: Record<number, string> = {
  0: 'badge-baixa',
  1: 'badge-media',
  2: 'badge-alta',
  3: 'badge-urgente',
};

export function classeStatus(status: number): string {
  return CLASSES_STATUS[status] ?? 'badge-encerrado';
}

export function classePrioridade(prioridade: number): string {
  return CLASSES_PRIORIDADE[prioridade] ?? 'badge-baixa';
}


export function textoSla(chamado: Chamado): string {
  if (!chamado.prazoSla || chamado.encerrado) return '';

  const horas = chamado.horasRestantesSla;
  if (horas === null || horas === undefined) return '';

  if (horas < 0) {
    const atraso = Math.abs(horas);
    return atraso >= 24
      ? `Vencido há ${Math.floor(atraso / 24)}d`
      : `Vencido há ${atraso}h`;
  }
  if (horas < 1) return 'Vence em menos de 1h';
  if (horas < 24) return `Faltam ${horas}h`;
  return `Faltam ${Math.floor(horas / 24)}d`;
}


const TRANSICOES: Record<number, number[]> = {
  0: [1, 3, 4, 5],
  1: [3, 4, 0, 5],
  3: [1, 4, 5],
  4: [2, 1],
  2: [1],
  5: [],
};

export function transicoesPermitidas(status: number): number[] {
  return TRANSICOES[status] ?? [];
}


export function transicoesDoCliente(status: number): number[] {
  if (status === 0) return [5];
  if (status === 4) return [2, 1];
  if (status === 2) return [1];
  return [];
}


export function iniciais(nome?: string | null): string {
  if (!nome?.trim()) return '?';
  return nome
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p[0])
    .join('')
    .toUpperCase();
}


export type EstadoEtapa = 'completa' | 'atual' | 'pendente';

export interface EtapaProgresso {
  rotulo: string;
  estado: EstadoEtapa;
}

const ETAPAS_FLUXO: { rotulo: string; status: number }[] = [
  { rotulo: 'Aberto', status: StatusChamado.ABERTO },
  { rotulo: 'Em andamento', status: StatusChamado.EM_ANDAMENTO },
  { rotulo: 'Aguardando cliente', status: StatusChamado.AGUARDANDO_CLIENTE },
  { rotulo: 'Resolvido', status: StatusChamado.RESOLVIDO },
  { rotulo: 'Encerrado', status: StatusChamado.ENCERRADO },
];

export function etapasProgresso(status: number): EtapaProgresso[] {
  const atual = ETAPAS_FLUXO.findIndex((etapa) => etapa.status === status);
  return ETAPAS_FLUXO.map((etapa, indice) => ({
    rotulo: etapa.rotulo,
    estado: atual < 0 || indice > atual ? 'pendente' : indice === atual ? 'atual' : 'completa',
  }));
}
