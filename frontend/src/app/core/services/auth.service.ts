import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginResponse {
  id: number;
  token: string;
  tipo: string;
  nome: string;
  email: string;
  perfis: string[];
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
  private readonly apiUrl = `${environment.apiUrl}/v1/auth`;
  private readonly tokenKey = 'helpdesk_token';
  private readonly userKey = 'helpdesk_user';

  constructor(private http: HttpClient) {}

  register(nome: string, email: string, senha: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/v1/auth/register`, { nome, email, senha });
  }

  login(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, senha }).pipe(
      tap((response) => {
        if (response?.token) {
          localStorage.setItem(this.tokenKey, response.token);
          localStorage.setItem(this.userKey, JSON.stringify({
            id: response.id,
            nome: response.nome,
            email: response.email,
            perfis: response.perfis
          } as UsuarioAtual));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUsuarioAtual(): UsuarioAtual | null {
    const raw = localStorage.getItem(this.userKey);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }

  isAdmin(): boolean {
    return this.getUsuarioAtual()?.perfis?.includes('ROLE_ADMIN') ?? false;
  }

  isTecnico(): boolean {
    return this.getUsuarioAtual()?.perfis?.includes('ROLE_TECNICO') ?? false;
  }

  isCliente(): boolean {
    const perfis = this.getUsuarioAtual()?.perfis ?? [];
    return perfis.includes('ROLE_CLIENTE') && !perfis.includes('ROLE_ADMIN') && !perfis.includes('ROLE_TECNICO');
  }

  getIniciais(nome: string): string {
    if (!nome) return '?';
    return nome.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  getPerfilLabel(perfis: string[]): string {
    if (perfis?.includes('ROLE_ADMIN')) return 'Administrador';
    if (perfis?.includes('ROLE_TECNICO')) return 'Técnico';
    return 'Cliente';
  }
}
