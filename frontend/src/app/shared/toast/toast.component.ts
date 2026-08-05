import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-wrap" role="status" aria-live="polite">
      @for (t of toastService.toasts(); track t.id) {
        <div class="toast" [class]="'toast-' + t.tipo">
          <span class="toast-icone" aria-hidden="true">
            @switch (t.tipo) {
              @case ('sucesso') { ✓ }
              @case ('erro') { ! }
              @case ('aviso') { ! }
              @default { i }
            }
          </span>
          <span class="toast-msg">{{ t.mensagem }}</span>
          <button
            class="toast-fechar"
            type="button"
            aria-label="Fechar aviso"
            (click)="toastService.remover(t.id)"
          >×</button>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .toast-wrap {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 2000;
        display: flex;
        flex-direction: column;
        gap: 10px;
        max-width: min(380px, calc(100vw - 40px));
      }

      .toast {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 12px 14px;
        border-radius: 10px;
        background: #fff;
        border-left: 4px solid var(--color-text-muted);
        box-shadow: 0 8px 24px rgba(15, 23, 42, 0.16);
        animation: toast-in 0.2s ease-out;
      }

      @keyframes toast-in {
        from { opacity: 0; transform: translateX(16px); }
        to   { opacity: 1; transform: translateX(0); }
      }

      @media (prefers-reduced-motion: reduce) {
        .toast { animation: none; }
      }

      .toast-icone {
        flex-shrink: 0;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 700;
        color: #fff;
        background: var(--color-text-muted);
      }

      .toast-msg {
        flex: 1;
        font-size: 13.5px;
        line-height: 1.45;
        color: var(--color-text-primary);
      }

      .toast-fechar {
        background: none;
        border: none;
        font-size: 18px;
        line-height: 1;
        color: var(--color-text-muted);
        cursor: pointer;
        padding: 0 2px;
      }
      .toast-fechar:hover { color: var(--color-text-primary); }

      .toast-sucesso { border-left-color: #059669; }
      .toast-sucesso .toast-icone { background: #059669; }

      .toast-erro { border-left-color: #dc2626; }
      .toast-erro .toast-icone { background: #dc2626; }

      .toast-aviso { border-left-color: #d97706; }
      .toast-aviso .toast-icone { background: #d97706; }

      .toast-info { border-left-color: var(--color-primary); }
      .toast-info .toast-icone { background: var(--color-primary); }

      @media (max-width: 640px) {
        .toast-wrap {
          top: auto;
          bottom: 16px;
          left: 16px;
          right: 16px;
          max-width: none;
        }
      }
    `,
  ],
})
export class ToastComponent {
  readonly toastService = inject(ToastService);
}
