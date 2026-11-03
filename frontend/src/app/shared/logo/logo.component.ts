import { Component, input } from '@angular/core';


@Component({
  selector: 'app-logo',
  standalone: true,
  template: `
    <span class="logo" [class.logo-escuro]="tema() === 'escuro'">
      <span class="logo-marca">Help Desk</span>
      <span class="logo-subtitulo">Suporte técnico interno</span>
    </span>
  `,
  styles: [
    `
      .logo {
        display: inline-flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 3px;
        line-height: 1.15;
      }

      .logo-marca {
        color: #171a1c;
        font-size: 20px;
        font-weight: 750;
        letter-spacing: -0.035em;
        white-space: nowrap;
      }

      .logo-subtitulo {
        color: #747979;
        font-size: 11px;
        font-weight: 500;
        letter-spacing: 0.01em;
        white-space: nowrap;
      }

      .logo-escuro .logo-marca {
        color: #f7f7f3;
      }

      .logo-escuro .logo-subtitulo {
        color: #8f9697;
      }
    `,
  ],
})
export class LogoComponent {
  readonly tema = input<'claro' | 'escuro'>('claro');
}
