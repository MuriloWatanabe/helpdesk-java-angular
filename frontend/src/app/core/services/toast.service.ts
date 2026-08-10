import { Injectable, signal } from '@angular/core';

export type ToastTipo = 'sucesso' | 'erro' | 'aviso' | 'info';

export interface Toast {
  id: number;
  tipo: ToastTipo;
  mensagem: string;
}


@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();

  private proximoId = 1;

  sucesso(mensagem: string): void {
    this.mostrar('sucesso', mensagem);
  }

  erro(mensagem: string): void {
    this.mostrar('erro', mensagem, 6000);
  }

  aviso(mensagem: string): void {
    this.mostrar('aviso', mensagem);
  }

  info(mensagem: string): void {
    this.mostrar('info', mensagem);
  }


  erroDaApi(erro: unknown, alternativa = 'Não foi possível concluir a operação.'): void {
    this.erro(mensagemDoErro(erro, alternativa));
  }

  remover(id: number): void {
    this._toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  private mostrar(tipo: ToastTipo, mensagem: string, duracao = 4000): void {
    const id = this.proximoId++;
    this._toasts.update((lista) => [...lista, { id, tipo, mensagem }]);
    setTimeout(() => this.remover(id), duracao);
  }
}


export function mensagemDoErro(erro: unknown, alternativa: string): string {
  const corpo = (erro as { error?: { message?: string } } | null)?.error;
  return corpo?.message?.trim() || alternativa;
}
