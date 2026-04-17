import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, switchMap, map, catchError, of } from 'rxjs';

export interface LoginResponse {
  token: string;
  tipo: string;
  email: string;
  perfis: string[];
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioAtual {
  nome: string;
  email: string;
  perfis: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/auth';
  private readonly usuariosUrl = 'http://localhost:8080/api/v1/usuarios';
  private readonly tokenKey = 'helpdesk_token';
  private readonly userKey = 'helpdesk_user';

  constructor(private http: HttpClient) {}

  login(email: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, senha }).pipe(
      tap((response) => {
        if (response?.token) {
          localStorage.setItem(this.tokenKey, response.token);
          localStorage.setItem(this.userKey, JSON.stringify({
            nome: '',
            email: response.email,
            perfis: response.perfis
          }));
        }
      }),
      switchMap(response =>
        this.http.get<any[]>(this.usuariosUrl).pipe(
          tap(users => {
            const user = users.find((u: any) => u.email === response.email);
            if (user) {
              localStorage.setItem(this.userKey, JSON.stringify({
                nome: user.nome,
                email: response.email,
                perfis: response.perfis
              }));
            }
          }),
          map(() => response),
          catchError(() => of(response))
        )
      )
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
