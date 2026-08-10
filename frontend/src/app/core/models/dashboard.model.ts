import { Chamado } from './chamado.model';

export interface Contagem {
  codigo: number | null;
  rotulo: string;
  total: number;
}

export interface SerieDiaria {
  data: string;
  total: number;
}


export interface DashboardStats {
  escopo: 'GLOBAL' | 'TECNICO' | 'CLIENTE';

  totalChamados: number;
  totalAbertos: number;
  totalEmAndamento: number;
  totalAguardandoCliente: number;
  totalResolvidos: number;
  totalEncerrados: number;
  totalCancelados: number;

  totalSlaVencido: number;
  totalSlaEmRisco: number;
  totalSemTecnico: number;

  tempoMedioResolucaoHoras: number | null;
  notaMediaAtendimento: number | null;
  totalAvaliacoes: number;

  porPrioridade: Contagem[];
  porCategoria: Contagem[];
  porTecnico: Contagem[];
  aberturasPorDia: SerieDiaria[];

  chamadosRecentes: Chamado[];
}
