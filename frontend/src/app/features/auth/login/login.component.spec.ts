import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { LoginComponent } from './login.component';

@Component({ standalone: true, template: '<p>Destino protegido</p>' })
class DestinoTesteComponent {}

describe('LoginComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: LoginComponent },
          { path: 'chamados', component: DestinoTesteComponent },
          { path: 'dashboard', component: DestinoTesteComponent },
        ]),
      ],
    }).compileComponents();
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
    localStorage.clear();
  });

  it('oferece login e criação de conta sem expor acessos de demonstração', async () => {
    const harness = await RouterTestingHarness.create('/login');
    const page = harness.routeNativeElement as HTMLElement;
    const texto = page.textContent ?? '';

    expect(page.querySelector('.auth-hero')).toBeTruthy();
    expect(page.querySelector('input[autocomplete="username"]')).toBeTruthy();
    expect(page.querySelector('input[autocomplete="current-password"]')).toBeTruthy();
    expect(page.querySelector('a[href="/esqueci-senha"]')).toBeTruthy();
    expect(page.querySelector('a[href="/register"]')).toBeTruthy();
    expect(page.querySelector('button[type="submit"]')).toBeTruthy();
    expect(page.querySelector('.demo-grid')).toBeFalsy();
    expect(texto).not.toContain('acesso de demonstração');
    expect(texto).not.toContain('admin@helpdesk.com');
    expect(texto).not.toContain('tecnico@helpdesk.com');
    expect(texto).not.toContain('cliente@helpdesk.com');
  });

  it('autentica e retorna para a rota solicitada', async () => {
    const harness = await RouterTestingHarness.create('/login?retorno=/chamados');
    const component = harness.routeDebugElement?.componentInstance as LoginComponent;
    const http = TestBed.inject(HttpTestingController);

    component.loginForm.setValue({ email: 'pessoa@empresa.com.br', senha: 'senha123' });
    component.onSubmit();

    const request = http.expectOne('http://localhost:9090/api/v1/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'pessoa@empresa.com.br', senha: 'senha123' });
    request.flush({
      id: 7,
      token: 'token-sem-expiracao',
      tipo: 'Bearer',
      nome: 'Pessoa Teste',
      email: 'pessoa@empresa.com.br',
      perfis: ['ROLE_CLIENTE'],
      expiraEm: '2099-01-01T00:00:00Z',
    });
    await harness.fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/chamados');
  });
});
