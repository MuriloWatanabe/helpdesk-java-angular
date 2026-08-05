import { Component, inject } from '@angular/core';
import { ConfirmService } from '../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (confirmService.estado(); as dialog) {
      <div class="overlay" (click)="confirmService.responder(false)">
        <div
          class="dialog"
          role="alertdialog"
          aria-modal="true"
          [attr.aria-label]="dialog.titulo"
          (click)="$event.stopPropagation()"
        >
          <h2 class="dialog-titulo">{{ dialog.titulo }}</h2>
          <p class="dialog-msg">{{ dialog.mensagem }}</p>
          <div class="dialog-acoes">
            <button type="button" class="btn-cancelar" (click)="confirmService.responder(false)">
              {{ dialog.cancelar ?? 'Cancelar' }}
            </button>
            <button
              type="button"
              class="btn-confirmar"
              [class.perigoso]="dialog.perigoso"
              (click)="confirmService.responder(true)"
            >
              {{ dialog.confirmar ?? 'Confirmar' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .overlay {
        position: fixed;
        inset: 0;
        background: rgba(15, 23, 42, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 20px;
        z-index: 1900;
      }

      .dialog {
        background: #fff;
        border-radius: 12px;
        padding: 24px;
        width: 100%;
        max-width: 420px;
        box-shadow: 0 20px 48px rgba(15, 23, 42, 0.24);
      }

      .dialog-titulo {
        font-size: 17px;
        font-weight: 700;
        color: var(--color-text-primary);
        margin-bottom: 8px;
      }

      .dialog-msg {
        font-size: 14px;
        line-height: 1.55;
        color: var(--color-text-secondary);
        margin-bottom: 22px;
      }

      .dialog-acoes {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
      }

      .dialog-acoes button {
        padding: 9px 16px;
        border-radius: 8px;
        font-size: 13.5px;
        font-weight: 600;
      }

      .btn-cancelar {
        background: #fff;
        border: 1px solid var(--color-border);
        color: var(--color-text-secondary);
      }
      .btn-cancelar:hover { background: #f8fafc; }

      .btn-confirmar {
        background: var(--color-primary);
        color: #fff;
      }
      .btn-confirmar:hover { background: var(--color-primary-hover); }

      .btn-confirmar.perigoso { background: #dc2626; }
      .btn-confirmar.perigoso:hover { background: #b91c1c; }
    `,
  ],
})
export class ConfirmDialogComponent {
  readonly confirmService = inject(ConfirmService);
}
