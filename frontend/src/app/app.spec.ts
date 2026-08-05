import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      // O shell monta router-outlet, toasts e diálogo de confirmação.
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();
  });

  it('deve inicializar o componente raiz', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve renderizar o outlet de rotas e os componentes globais', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelector('router-outlet')).toBeTruthy();
    expect(html.querySelector('app-toast')).toBeTruthy();
    expect(html.querySelector('app-confirm-dialog')).toBeTruthy();
  });
});
