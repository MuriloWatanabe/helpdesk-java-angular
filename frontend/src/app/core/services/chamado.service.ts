import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Chamado, PageResponse } from '../models/chamado.model';

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly url = 'http://localhost:8080/api/v1/chamados';

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<PageResponse<Chamado>> {
    return this.http.get<PageResponse<Chamado>>(`${this.url}?page=${page}&size=${size}`);
  }

  buscarPorId(id: number): Observable<Chamado> {
    return this.http.get<Chamado>(`${this.url}/${id}`);
  }

  criar(chamado: Partial<Chamado>): Observable<Chamado> {
    return this.http.post<Chamado>(this.url, chamado);
  }

  atualizar(id: number, chamado: Partial<Chamado>): Observable<Chamado> {
    return this.http.put<Chamado>(`${this.url}/${id}`, chamado);
  }

  alterarStatus(id: number, status: number): Observable<Chamado> {
    return this.http.patch<Chamado>(`${this.url}/${id}/status/${status}`, {});
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
