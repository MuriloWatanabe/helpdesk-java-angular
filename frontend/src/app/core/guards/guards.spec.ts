import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { adminGuard, atendenteGuard } from './admin.guard';

describe('Guards de rota', () => {
  const auth = {
    isLoggedIn: vi.fn(),
    isAdmin: vi.fn(),
    isAtendente: vi.fn(),
  };

  beforeEach(() => {
    vi.resetAllMocks();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  function executar(guard: CanActivateFn, url = '/chamados') {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );
  }

  function urlDe(resultado: unknown): string {
    expect(resultado).toBeInstanceOf(UrlTree);
    return TestBed.inject(Router).serializeUrl(resultado as UrlTree);
  }

  describe('authGuard', () => {
    it('libera usuário logado', () => {
      auth.isLoggedIn.mockReturnValue(true);
      expect(executar(authGuard)).toBe(true);
    });

    it('redireciona para o login guardando a URL de retorno', () => {
      auth.isLoggedIn.mockReturnValue(false);
      expect(urlDe(executar(authGuard, '/chamados/5'))).toBe('/login?retorno=%2Fchamados%2F5');
    });
  });

  describe('adminGuard', () => {
    it('libera administrador', () => {
      auth.isLoggedIn.mockReturnValue(true);
      auth.isAdmin.mockReturnValue(true);
      expect(executar(adminGuard)).toBe(true);
    });

    it('envia usuário sem perfil de admin para acesso negado', () => {
      auth.isLoggedIn.mockReturnValue(true);
      auth.isAdmin.mockReturnValue(false);
      expect(urlDe(executar(adminGuard))).toBe('/acesso-negado');
    });

    it('envia usuário deslogado para o login', () => {
      auth.isLoggedIn.mockReturnValue(false);
      expect(urlDe(executar(adminGuard, '/usuarios'))).toBe('/login?retorno=%2Fusuarios');
      expect(auth.isAdmin).not.toHaveBeenCalled();
    });
  });

  describe('atendenteGuard', () => {
    it('libera técnico ou admin', () => {
      auth.isLoggedIn.mockReturnValue(true);
      auth.isAtendente.mockReturnValue(true);
      expect(executar(atendenteGuard)).toBe(true);
    });

    it('envia cliente para acesso negado', () => {
      auth.isLoggedIn.mockReturnValue(true);
      auth.isAtendente.mockReturnValue(false);
      expect(urlDe(executar(atendenteGuard))).toBe('/acesso-negado');
    });

    it('envia usuário deslogado para o login', () => {
      auth.isLoggedIn.mockReturnValue(false);
      expect(urlDe(executar(atendenteGuard, '/relatorios'))).toBe('/login?retorno=%2Frelatorios');
    });
  });
});
