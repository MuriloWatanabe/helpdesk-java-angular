export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfis: string[];
  dataCriacao: string;
}

export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfis: number[];
}

export interface AtualizarUsuarioRequest {
  nome: string;
  email: string;
  senha?: string;
  perfis?: number[];
}
