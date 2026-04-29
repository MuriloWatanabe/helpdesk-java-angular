import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario, CriarUsuarioRequest, AtualizarUsuarioRequest } from '../models/usuario.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly url = `${environment.apiUrl}/v1/usuarios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  buscarPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/${id}`);
  }

  criar(request: CriarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, request);
  }

  atualizar(id: number, request: AtualizarUsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.url}/${id}`, request);
  }

  atualizarPerfis(id: number, perfis: number[]): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.url}/${id}/perfis`, perfis);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
