import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Anexo,
  Avaliacao,
  AvaliacaoRequest,
  Chamado,
  ChamadoFiltro,
  ChamadoRequest,
  Comentario,
  HistoricoItem,
  PageResponse,
  STATUS_ENUM_NOME,
} from '../models/chamado.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/v1/chamados`;

  // ------------------------------------------------------------------
  // Chamados
  // ------------------------------------------------------------------

  /**
   * Filtro e busca são resolvidos no servidor. Antes eram aplicados apenas
   * sobre a página já carregada, então "Encerrados" só encontrava o que
   * estivesse entre os 10 primeiros resultados.
   */
  listar(filtro: ChamadoFiltro = {}, page = 0, size = 10): Observable<PageResponse<Chamado>> {
    let params = new HttpParams().set('page', page).set('size', size);

    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(chave, String(valor));
      }
    }

    return this.http.get<PageResponse<Chamado>>(this.url, { params });
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

  /** Aceita o código numérico ou o nome do status. */
  alterarStatus(id: number, status: number | string): Observable<Chamado> {
    const nome = typeof status === 'number' ? STATUS_ENUM_NOME[status] : status;
    return this.http.patch<Chamado>(`${this.url}/${id}/status/${nome}`, {});
  }

  assumir(id: number): Observable<Chamado> {
    return this.http.patch<Chamado>(`${this.url}/${id}/assumir`, {});
  }

  atribuirTecnico(id: number, tecnicoId: number | null): Observable<Chamado> {
    return this.http.patch<Chamado>(`${this.url}/${id}/tecnico`, { tecnicoId });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  historico(id: number): Observable<HistoricoItem[]> {
    return this.http.get<HistoricoItem[]>(`${this.url}/${id}/historico`);
  }

  // ------------------------------------------------------------------
  // Comentários
  // ------------------------------------------------------------------

  listarComentarios(chamadoId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.url}/${chamadoId}/comentarios`);
  }

  comentar(chamadoId: number, texto: string, interno = false): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.url}/${chamadoId}/comentarios`, {
      texto,
      interno,
    });
  }

  editarComentario(chamadoId: number, comentarioId: number, texto: string): Observable<Comentario> {
    return this.http.put<Comentario>(`${this.url}/${chamadoId}/comentarios/${comentarioId}`, {
      texto,
    });
  }

  excluirComentario(chamadoId: number, comentarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${chamadoId}/comentarios/${comentarioId}`);
  }

  // ------------------------------------------------------------------
  // Anexos
  // ------------------------------------------------------------------

  listarAnexos(chamadoId: number): Observable<Anexo[]> {
    return this.http.get<Anexo[]>(`${this.url}/${chamadoId}/anexos`);
  }

  enviarAnexo(chamadoId: number, arquivo: File, interno = false): Observable<Anexo> {
    const form = new FormData();
    form.append('arquivo', arquivo);
    form.append('interno', String(interno));
    return this.http.post<Anexo>(`${this.url}/${chamadoId}/anexos`, form);
  }

  /** Baixa via blob para que o header Authorization seja enviado. */
  baixarAnexo(chamadoId: number, anexoId: number): Observable<Blob> {
    return this.http.get(`${this.url}/${chamadoId}/anexos/${anexoId}/download`, {
      responseType: 'blob',
    });
  }

  excluirAnexo(chamadoId: number, anexoId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${chamadoId}/anexos/${anexoId}`);
  }

  // ------------------------------------------------------------------
  // Avaliação
  // ------------------------------------------------------------------

  /** Responde 204 (corpo vazio) quando o chamado ainda não foi avaliado. */
  buscarAvaliacao(chamadoId: number): Observable<Avaliacao | null> {
    return this.http.get<Avaliacao | null>(`${this.url}/${chamadoId}/avaliacao`);
  }

  avaliar(chamadoId: number, request: AvaliacaoRequest): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(`${this.url}/${chamadoId}/avaliacao`, request);
  }
}
