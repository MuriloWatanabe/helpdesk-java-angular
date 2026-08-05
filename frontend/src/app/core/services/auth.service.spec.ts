import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

/** Monta um JWT sem assinatura válida — só o payload importa para estes testes. */
function tokenComExpiracao(segundosDesdeEpoch: number | null): string {
  const payload = segundosDesdeEpoch === null ? {} : { exp: segundosDesdeEpoch };
  const base64 = btoa(JSON.stringify(payload)).replaceAll('+', '-').replaceAll('/', '_');
  return `cabecalho.${base64}.assinatura`;
}

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  describe('isLoggedIn', () => {
    it('retorna falso quando não há token', () => {
      expect(service.isLoggedIn()).toBe(false);
    });

    it('retorna verdadeiro para token dentro da validade', () => {
      const daquiUmaHora = Math.floor(Date.now() / 1000) + 3600;
      localStorage.setItem('helpdesk_token', tokenComExpiracao(daquiUmaHora));

      expect(service.isLoggedIn()).toBe(true);
    });

    it('rejeita token expirado e limpa a sessão', () => {
      const umaHoraAtras = Math.floor(Date.now() / 1000) - 3600;
      localStorage.setItem('helpdesk_token', tokenComExpiracao(umaHoraAtras));

      expect(service.isLoggedIn()).toBe(false);
      // Sem isso o usuário entrava na tela e só descobria na primeira requisição.
      expect(localStorage.getItem('helpdesk_token')).toBeNull();
    });

    it('aceita token sem campo exp, deixando a decisão para o servidor', () => {
      localStorage.setItem('helpdesk_token', tokenComExpiracao(null));

      expect(service.isLoggedIn()).toBe(true);
    });

    it('não quebra com token malformado', () => {
      localStorage.setItem('helpdesk_token', 'isto-nao-e-um-jwt');

      expect(() => service.isLoggedIn()).not.toThrow();
    });
  });

  describe('perfis', () => {
    function logar(perfis: string[]): void {
      localStorage.setItem(
        'helpdesk_user',
        JSON.stringify({ id: 1, nome: 'Teste', email: 't@t.com', perfis }),
      );
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({ providers: [provideHttpClient()] });
      service = TestBed.inject(AuthService);
    }

    it('identifica administrador como atendente', () => {
      logar(['ROLE_ADMIN']);

      expect(service.isAdmin()).toBe(true);
      expect(service.isAtendente()).toBe(true);
      expect(service.isCliente()).toBe(false);
    });

    it('identifica cliente puro', () => {
      logar(['ROLE_CLIENTE']);

      expect(service.isCliente()).toBe(true);
      expect(service.isAtendente()).toBe(false);
      expect(service.perfilLabel()).toBe('Cliente');
    });

    it('usuário com perfil de cliente e técnico não é tratado como cliente puro', () => {
      logar(['ROLE_CLIENTE', 'ROLE_TECNICO']);

      expect(service.isCliente()).toBe(false);
      expect(service.isAtendente()).toBe(true);
    });
  });

  describe('getIniciais', () => {
    it('usa as duas primeiras palavras do nome', () => {
      expect(service.getIniciais('Ana Maria Souza')).toBe('AM');
    });

    it('lida com nome vazio ou ausente', () => {
      expect(service.getIniciais('')).toBe('?');
      expect(service.getIniciais(null)).toBe('?');
    });
  });

  it('logout limpa token e usuário armazenados', () => {
    localStorage.setItem('helpdesk_token', 'x');
    localStorage.setItem('helpdesk_user', '{}');

    service.logout();

    expect(localStorage.getItem('helpdesk_token')).toBeNull();
    expect(localStorage.getItem('helpdesk_user')).toBeNull();
    expect(service.getUsuarioAtual()).toBeNull();
  });
});
