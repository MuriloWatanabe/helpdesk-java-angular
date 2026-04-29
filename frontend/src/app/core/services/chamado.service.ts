import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Chamado, PageResponse } from '../models/chamado.model';
import { environment } from '../../../environments/environment';

export interface ChamadoRequest {
  titulo: string;
  observacoes: string;
  prioridade: number;
  tecnicoId?: number;
  clienteId?: number;
}

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly url = `${environment.apiUrl}/v1/chamados`;

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<PageResponse<Chamado>> {
    return this.http.get<PageResponse<Chamado>>(`${this.url}?page=${page}&size=${size}`);
  }

  buscarPorId(id: number): Observable<Chamado> {
    return this.http.get<Chamado>(`${this.url}/${id}`);
  }

  criar(request: ChamadoRequest): Observable<Chamado> {
    return this.http.post<Chamado>(this.url, request);
  }

  atualizar(id: number, request: ChamadoRequest): Observable<Chamado> {
    return this.http.put<Chamado>(`${this.url}/${id}`, request);
  }

  alterarStatus(id: number, status: string): Observable<Chamado> {
    return this.http.patch<Chamado>(`${this.url}/${id}/status/${status}`, {});
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
