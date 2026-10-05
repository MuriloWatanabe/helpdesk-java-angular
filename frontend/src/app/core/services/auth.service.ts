import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AlterarSenhaRequest, AtualizarPerfilRequest, Usuario } from '../models/usuario.model';

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

// Maior atraso aceito por setTimeout (~24,8 dias); acima disso o timer dispararia na hora.
const MAX_TIMEOUT_MS = 2_147_483_647;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/v1/auth`;

  private readonly tokenKey = 'helpdesk_token';
  private readonly userKey = 'helpdesk_user';

  private readonly _usuario = signal<UsuarioAtual | null>(this.lerUsuarioSalvo());
  readonly usuario = this._usuario.asReadonly();

  private timerExpiracao: ReturnType<typeof setTimeout> | null = null;

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

  constructor() {
    const token = this.getToken();
    if (token && this.isLoggedIn()) {
      this.agendarExpiracao(token);
    }
  }

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
        this.agendarExpiracao(response.token);
      }),
    );
  }

  register(nome: string, email: string, senha: string, telefone?: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/register`, { nome, email, senha, telefone });
  }

  logout(): void {
    this.cancelarExpiracao();
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this._usuario.set(null);
  }

  // Sessão encerrada pelo sistema (token expirado ou recusado): limpa tudo e volta ao login.
  // Várias requisições podem receber 401 ao mesmo tempo; só a primeira redireciona.
  encerrarSessao(): void {
    if (!this.getToken()) return;
    this.logout();

    const atual = this.router.url;
    this.router.navigate(['/login'], {
      queryParams: atual.startsWith('/login') ? { expirado: '1' } : { expirado: '1', retorno: atual },
    });
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    // Token sem os dados do usuário (ou o contrário) deixaria a tela sem perfis: trata como deslogado.
    if (!this.tokenValido(token) || !this._usuario()) {
      this.logout();
      return false;
    }
    return true;
  }

  private tokenValido(token: string): boolean {
    const expiracao = this.lerExpiracao(token);
    if (expiracao === undefined) return false;
    return expiracao === null || Date.now() < expiracao;
  }

  // Retorna o exp em milissegundos, null se o token não tiver exp e undefined se estiver malformado.
  private lerExpiracao(token: string): number | null | undefined {
    try {
      const partes = token.split('.');
      if (partes.length !== 3 || !partes[1]) return undefined;

      const base64 = partes[1].replaceAll('-', '+').replaceAll('_', '/');
      const payload = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='))) as {
        exp?: unknown;
      };

      if (payload.exp === undefined) return null;
      if (typeof payload.exp !== 'number' || !Number.isFinite(payload.exp)) return undefined;
      return payload.exp * 1000;
    } catch {
      return undefined;
    }
  }

  private agendarExpiracao(token: string): void {
    this.cancelarExpiracao();
    const expiracao = this.lerExpiracao(token);
    if (expiracao === null || expiracao === undefined) return;

    const restante = expiracao - Date.now();
    if (restante <= 0 || restante > MAX_TIMEOUT_MS) return;
    this.timerExpiracao = setTimeout(() => this.encerrarSessao(), restante);
  }

  private cancelarExpiracao(): void {
    if (this.timerExpiracao !== null) {
      clearTimeout(this.timerExpiracao);
      this.timerExpiracao = null;
    }
  }

  carregarMeuPerfil(): Observable<Usuario> {
    return this.http
      .get<Usuario>(`${this.apiUrl}/me`)
      .pipe(
        tap((u) =>
          this.salvarUsuario({ id: u.id, nome: u.nome, email: u.email, perfis: u.perfis }),
        ),
      );
  }

  atualizarMeuPerfil(request: AtualizarPerfilRequest): Observable<Usuario> {
    return this.http
      .put<Usuario>(`${this.apiUrl}/me`, request)
      .pipe(
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
      const usuario = JSON.parse(bruto) as Partial<UsuarioAtual>;
      if (
        typeof usuario.id !== 'number' ||
        typeof usuario.nome !== 'string' ||
        typeof usuario.email !== 'string' ||
        !Array.isArray(usuario.perfis) ||
        !usuario.perfis.every((perfil) => typeof perfil === 'string')
      ) {
        localStorage.removeItem(this.userKey);
        return null;
      }
      return usuario as UsuarioAtual;
    } catch {
      localStorage.removeItem(this.userKey);
      return null;
    }
  }
}
