import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AlterarSenhaRequest,
  AtualizarPerfilRequest,
  Usuario,
} from '../models/usuario.model';

export interface LoginResponse {
  id: number;
  token: string;
  tipo: string;
  nome: string;
  email: string;
  perfis: string[];
  expiraEm: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioAtual {
  id: number;
  nome: string;
  email: string;
  perfis: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/v1/auth`;

  private readonly tokenKey = 'helpdesk_token';
  private readonly userKey = 'helpdesk_user';


  private readonly _usuario = signal<UsuarioAtual | null>(this.lerUsuarioSalvo());
  readonly usuario = this._usuario.asReadonly();

  readonly isAdmin = computed(() => this.temPerfil('ROLE_ADMIN'));
  readonly isTecnico = computed(() => this.temPerfil('ROLE_TECNICO'));
  readonly isAtendente = computed(() => this.isAdmin() || this.isTecnico());
  readonly isCliente = computed(() => this.temPerfil('ROLE_CLIENTE') && !this.isAtendente());

  readonly perfilLabel = computed(() => {
    if (this.isAdmin()) return 'Administrador';
    if (this.isTecnico()) return 'Técnico';
    if (this.temPerfil('ROLE_CLIENTE')) return 'Cliente';
    return 'Usuário';
  });


  login(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, senha }).pipe(
      tap((response) => {
        if (!response?.token) return;
        localStorage.setItem(this.tokenKey, response.token);
        this.salvarUsuario({
          id: response.id,
          nome: response.nome,
          email: response.email,
          perfis: response.perfis,
        });
      }),
    );
  }

  register(nome: string, email: string, senha: string, telefone?: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/register`, { nome, email, senha, telefone });
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this._usuario.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }


  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    if (this.tokenExpirado(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  private tokenExpirado(token: string): boolean {
    const exp = this.lerExpiracao(token);
    if (exp === null) return false;
    return Date.now() >= exp;
  }


  private lerExpiracao(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const json = JSON.parse(
        atob(payload.replaceAll('-', '+').replaceAll('_', '/')),
      ) as { exp?: number };
      return json.exp ? json.exp * 1000 : null;
    } catch {
      return null;
    }
  }


  carregarMeuPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/me`).pipe(
      tap((u) =>
        this.salvarUsuario({ id: u.id, nome: u.nome, email: u.email, perfis: u.perfis }),
      ),
    );
  }


  atualizarMeuPerfil(request: AtualizarPerfilRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/me`, request).pipe(
      tap((u) =>
        this.salvarUsuario({ id: u.id, nome: u.nome, email: u.email, perfis: u.perfis }),
      ),
    );
  }

  alterarSenha(request: AlterarSenhaRequest): Observable<{ mensagem: string }> {
    return this.http.post<{ mensagem: string }>(`${this.apiUrl}/alterar-senha`, request);
  }

  recuperarSenha(email: string): Observable<{ mensagem: string; detalhe?: string }> {
    return this.http.post<{ mensagem: string; detalhe?: string }>(
      `${this.apiUrl}/recuperar-senha`,
      { email },
    );
  }

  redefinirSenha(token: string, novaSenha: string): Observable<{ mensagem: string }> {
    return this.http.post<{ mensagem: string }>(`${this.apiUrl}/redefinir-senha`, {
      token,
      novaSenha,
    });
  }

  getUsuarioAtual(): UsuarioAtual | null {
    return this._usuario();
  }


  getIniciais(nome?: string | null): string {
    if (!nome?.trim()) return '?';
    return nome
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase();
  }

  getPerfilLabel(perfis: string[] | undefined | null): string {
    if (perfis?.includes('ROLE_ADMIN')) return 'Administrador';
    if (perfis?.includes('ROLE_TECNICO')) return 'Técnico';
    return 'Cliente';
  }


  private temPerfil(role: string): boolean {
    return this._usuario()?.perfis?.includes(role) ?? false;
  }

  private salvarUsuario(usuario: UsuarioAtual): void {
    localStorage.setItem(this.userKey, JSON.stringify(usuario));
    this._usuario.set(usuario);
  }

  private lerUsuarioSalvo(): UsuarioAtual | null {
    const bruto = localStorage.getItem(this.userKey);
    if (!bruto) return null;
    try {
      return JSON.parse(bruto) as UsuarioAtual;
    } catch {
      return null;
    }
  }
}
