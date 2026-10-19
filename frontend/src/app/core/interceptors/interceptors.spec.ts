import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { jwtInterceptor } from './jwt.interceptor';
import { authErrorInterceptor } from './auth-error.interceptor';
import { environment } from '../../../environments/environment';

const API = environment.apiUrl;

describe('Interceptors HTTP', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  const auth = { getToken: vi.fn(), encerrarSessao: vi.fn() };
  const toast = { erro: vi.fn() };

  beforeEach(() => {
    vi.resetAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor, authErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  describe('jwtInterceptor', () => {
    it('adiciona o token nas chamadas à API', () => {
      auth.getToken.mockReturnValue('abc');
      http.get(`${API}/v1/chamados`).subscribe();

      const req = backend.expectOne(`${API}/v1/chamados`);
      expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
      req.flush([]);
    });

    it('não envia token sem usuário logado', () => {
      auth.getToken.mockReturnValue(null);
      http.get(`${API}/v1/chamados`).subscribe();

      const req = backend.expectOne(`${API}/v1/chamados`);
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush([]);
    });

    it.each(['login', 'register', 'recuperar-senha', 'redefinir-senha'])(
      'não envia token para a rota pública %s',
      (rota) => {
        auth.getToken.mockReturnValue('abc');
        http.post(`${API}/v1/auth/${rota}`, {}).subscribe();

        const req = backend.expectOne(`${API}/v1/auth/${rota}`);
        expect(req.request.headers.has('Authorization')).toBe(false);
        req.flush({});
      },
    );

    it('não vaza o token para outros domínios', () => {
      auth.getToken.mockReturnValue('abc');
      http.get('https://outro-site.com/dados').subscribe();

      const req = backend.expectOne('https://outro-site.com/dados');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });
  });

  describe('authErrorInterceptor', () => {
    function falhar(status: number, corpo: object | null = null): unknown {
      let erro: unknown;
      http.get(`${API}/v1/chamados`).subscribe({ error: (e) => (erro = e) });
      backend.expectOne(`${API}/v1/chamados`).flush(corpo, { status, statusText: 'Erro' });
      return erro;
    }

    it('401 em requisição autenticada encerra a sessão', () => {
      auth.getToken.mockReturnValue('abc');
      const erro = falhar(401);

      expect(auth.encerrarSessao).toHaveBeenCalled();
      expect((erro as { status: number }).status).toBe(401);
    });

    it('401 sem token (ex.: login inválido) não encerra a sessão', () => {
      auth.getToken.mockReturnValue(null);
      falhar(401);

      expect(auth.encerrarSessao).not.toHaveBeenCalled();
    });

    it('403 mostra a mensagem enviada pela API', () => {
      falhar(403, { message: 'Sem permissão para excluir' });
      expect(toast.erro).toHaveBeenCalledWith('Sem permissão para excluir');
    });

    it('403 sem mensagem usa o texto padrão', () => {
      falhar(403);
      expect(toast.erro).toHaveBeenCalledWith('Você não tem permissão para executar esta ação.');
    });

    it('erro de rede avisa que o servidor está fora', () => {
      let erro: unknown;
      http.get(`${API}/v1/chamados`).subscribe({ error: (e) => (erro = e) });
      backend.expectOne(`${API}/v1/chamados`).error(new ProgressEvent('error'), { status: 0 });

      expect(toast.erro).toHaveBeenCalledWith(expect.stringContaining('servidor'));
      expect(erro).toBeTruthy();
    });

    it('outros erros apenas são repassados', () => {
      const erro = falhar(500);

      expect(toast.erro).not.toHaveBeenCalled();
      expect(auth.encerrarSessao).not.toHaveBeenCalled();
      expect((erro as { status: number }).status).toBe(500);
    });
  });
});
