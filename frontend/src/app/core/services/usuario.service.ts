import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import {
  AtualizarUsuarioRequest,
  CriarUsuarioRequest,
  Metadados,
  Usuario,
} from '../models/usuario.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/v1/usuarios`;


  private metadados$?: Observable<Metadados>;

  listar(filtros: { perfil?: number; ativo?: boolean; q?: string } = {}): Observable<Usuario[]> {
    let params = new HttpParams();
    if (filtros.perfil !== undefined && filtros.perfil !== null) {
      params = params.set('perfil', filtros.perfil);
    }
    if (filtros.ativo !== undefined && filtros.ativo !== null) {
      params = params.set('ativo', filtros.ativo);
    }
    if (filtros.q) {
      params = params.set('q', filtros.q);
    }
    return this.http.get<Usuario[]>(this.url, { params });
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


  alterarSituacao(id: number, ativo: boolean): Observable<Usuario> {
    const params = new HttpParams().set('ativo', ativo);
    return this.http.patch<Usuario>(`${this.url}/${id}/situacao`, {}, { params });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }


  metadados(): Observable<Metadados> {
    this.metadados$ ??= this.http
      .get<Metadados>(`${environment.apiUrl}/v1/metadados`)
      .pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.metadados$;
  }
}
