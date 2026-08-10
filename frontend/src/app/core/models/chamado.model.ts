export interface UsuarioResumo {
  id: number;
  nome: string;
  email: string;
}


export const StatusChamado = {
  ABERTO: 0,
  EM_ANDAMENTO: 1,
  ENCERRADO: 2,
  AGUARDANDO_CLIENTE: 3,
  RESOLVIDO: 4,
  CANCELADO: 5,
} as const;

export const PrioridadeChamado = {
  BAIXA: 0,
  MEDIA: 1,
  ALTA: 2,
  URGENTE: 3,
} as const;


export const STATUS_ENUM_NOME: Record<number, string> = {
  0: 'ABERTO',
  1: 'EM_ANDAMENTO',
  2: 'ENCERRADO',
  3: 'AGUARDANDO_CLIENTE',
  4: 'RESOLVIDO',
  5: 'CANCELADO',
};

export interface Chamado {
  id: number;
  numero: string;
  titulo: string;
  observacoes: string;

  status: number;
  statusLabel: string;
  prioridade: number;
  prioridadeLabel: string;
  categoria: number | null;
  categoriaLabel: string | null;

  tecnico?: UsuarioResumo | null;
  cliente: UsuarioResumo;

  dataAbertura: string;
  dataFechamento?: string | null;
  dataAtualizacao?: string | null;
  dataPrimeiraResposta?: string | null;

  prazoSla?: string | null;
  slaVencido: boolean;
  horasRestantesSla?: number | null;

  encerrado: boolean;
  avaliado: boolean;
  totalComentarios: number;
  totalAnexos: number;
}

export interface ChamadoRequest {
  titulo: string;
  observacoes: string;
  prioridade: number;
  categoria: number;
  tecnicoId?: number | null;
  clienteId?: number | null;
}


export interface ChamadoFiltro {
  q?: string;
  status?: number | null;
  prioridade?: number | null;
  categoria?: number | null;
  tecnicoId?: number | null;
  clienteId?: number | null;
  semTecnico?: boolean | null;
  slaVencido?: boolean | null;
  apenasPendentes?: boolean | null;
  dataInicio?: string | null;
  dataFim?: string | null;
}

export interface Comentario {
  id: number;
  chamadoId: number;
  texto: string;
  interno: boolean;
  editado: boolean;
  autor: UsuarioResumo;
  autorPerfil: string;
  dataCriacao: string;
  dataAtualizacao?: string | null;
}

export interface HistoricoItem {
  id: number;
  tipo: string;
  descricao: string;
  valorAnterior?: string | null;
  valorNovo?: string | null;
  usuario?: UsuarioResumo | null;
  dataAlteracao: string;
}

export interface Anexo {
  id: number;
  chamadoId: number;
  nomeArquivo: string;
  tipoMime: string;
  tamanho: number;
  tamanhoFormatado: string;
  publico: boolean;
  imagem: boolean;
  enviadoPor: UsuarioResumo;
  dataUpload: string;
}

export interface Avaliacao {
  id: number;
  chamadoId: number;
  nota: number;
  interpretacao: string;
  comentario?: string | null;
  aspectos: string[];
  avaliadoPor?: UsuarioResumo | null;
  dataAvaliacao: string;
}

export interface AvaliacaoRequest {
  nota: number;
  comentario?: string;
  aspectos?: string[];
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
