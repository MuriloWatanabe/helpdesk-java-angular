export interface UsuarioResumo {
  id: number;
  nome: string;
  email: string;
}

export interface Chamado {
  id: number;
  titulo: string;
  observacoes: string;
  status: number;       // 0=ABERTO, 1=EM_ANDAMENTO, 2=ENCERRADO
  prioridade: number;   // 0=BAIXA, 1=MEDIA, 2=ALTA
  tecnico?: UsuarioResumo;
  cliente: UsuarioResumo;
  dataAbertura: string;
  dataFechamento?: string;
  dataAtualizacao?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
  number: number;
  size: number;
}
