import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  titulo: string;
  mensagem: string;
  confirmar?: string;
  cancelar?: string;
  perigoso?: boolean;
}

interface ConfirmState extends ConfirmOptions {
  resolver: (confirmado: boolean) => void;
}


@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly _estado = signal<ConfirmState | null>(null);
  readonly estado = this._estado.asReadonly();

  perguntar(options: ConfirmOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this._estado.set({ ...options, resolver: resolve });
    });
  }

  responder(confirmado: boolean): void {
    const atual = this._estado();
    this._estado.set(null);
    atual?.resolver(confirmado);
  }
}
