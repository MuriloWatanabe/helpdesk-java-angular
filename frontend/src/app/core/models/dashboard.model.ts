import { Chamado } from './chamado.model';

export interface DashboardStats {
  totalAbertos: number;
  totalEmAndamento: number;
  totalEncerrados: number;
  totalChamados: number;
  chamadosRecentes: Chamado[];
}
