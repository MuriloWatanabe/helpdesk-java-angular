export interface Usuario {
  id: number;
  nome: string;
  email: string;
  telefone?: string | null;
  cargo?: string | null;
  ativo: boolean;
  perfis: string[];
  perfisCodigos: number[];
  perfilPrincipal: string;
  dataCriacao: string;
  ultimoAcesso?: string | null;
}

export const PerfilCodigo = {
  ADMIN: 0,
  CLIENTE: 1,
  TECNICO: 2,
} as const;

export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  senha?: string;
  telefone?: string | null;
  cargo?: string | null;
  ativo?: boolean;
  perfis: number[];
}

export type AtualizarUsuarioRequest = CriarUsuarioRequest;


export interface AtualizarPerfilRequest {
  nome: string;
  email: string;
  telefone?: string | null;
  cargo?: string | null;
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}


export interface Opcao {
  codigo: number;
  nome: string;
  rotulo: string;
  horasSla?: number;
}

export interface Metadados {
  status: Opcao[];
  prioridades: Opcao[];
  categorias: Opcao[];
  perfis: Opcao[];
}
