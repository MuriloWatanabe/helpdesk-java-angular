import { Component, input } from '@angular/core';

/**
 * Marca do FixLab: símbolo + nome.
 *
 * O símbolo é inline (e não um <img>) para acompanhar o tamanho da fonte e
 * evitar um request extra em cada tela. O arquivo public/logo-fixlab.svg guarda
 * a versão fechada da marca, usada fora da aplicação (README, relatório).
 */
@Component({
  selector: 'app-logo',
  standalone: true,
  template: `
    <span class="logo" [class.logo-escuro]="tema() === 'escuro'">
      <svg
        class="logo-simbolo"
        viewBox="0 0 100 100"
        aria-hidden="true"
        focusable="false"
      >
        <circle cx="50" cy="50" r="50" fill="#4F46E5" />
        <g transform="rotate(-45 50 50)" fill="#FFFFFF">
          <rect x="43" y="21" width="14" height="19" rx="6.5" />
          <rect x="37" y="35.5" width="26" height="7" rx="3.5" />
          <path
            d="M46.5 42.5h7v25l-3.5 11-3.5-11z"
            stroke="#FFFFFF"
            stroke-width="2.5"
            stroke-linejoin="round"
          />
        </g>
      </svg>
      <span class="logo-nome">
        <span class="logo-fix">Fix</span><span class="logo-lab">Lab</span>
      </span>
    </span>
  `,
  styles: [
    `
      .logo {
        display: inline-flex;
        align-items: center;
        gap: 0.42em;
        font-size: 22px;
        line-height: 1;
      }

      .logo-simbolo {
        width: 1.55em;
        height: 1.55em;
        flex-shrink: 0;
      }

      .logo-nome {
        font-size: 1em;
        font-weight: 800;
        letter-spacing: -0.02em;
        white-space: nowrap;
      }

      .logo-fix {
        color: #1f2a44;
      }

      .logo-lab {
        color: #4f46e5;
      }

      /* Sobre o azul da barra lateral o azul-escuro do "Fix" sumiria. */
      .logo-escuro .logo-fix {
        color: #ffffff;
      }

      .logo-escuro .logo-lab {
        color: #a5b4fc;
      }
    `,
  ],
})
export class LogoComponent {
  /** 'claro' para fundo branco, 'escuro' para a barra lateral. */
  readonly tema = input<'claro' | 'escuro'>('claro');
}
