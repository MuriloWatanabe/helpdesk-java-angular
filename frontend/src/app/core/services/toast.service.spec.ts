import { TestBed } from '@angular/core/testing';
import { ToastService, mensagemDoErro } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();
    service = TestBed.inject(ToastService);
  });

  afterEach(() => vi.useRealTimers());

  it('adiciona toasts com ids diferentes e o tipo correto', () => {
    service.sucesso('Salvo');
    service.aviso('Atenção');
    service.info('Dica');

    const toasts = service.toasts();
    expect(toasts.map((t) => t.tipo)).toEqual(['sucesso', 'aviso', 'info']);
    expect(new Set(toasts.map((t) => t.id)).size).toBe(3);
  });

  it('remove o toast de sucesso após 4 segundos', () => {
    service.sucesso('Salvo');

    vi.advanceTimersByTime(3999);
    expect(service.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(1);
    expect(service.toasts()).toHaveLength(0);
  });

  it('mantém o toast de erro por 6 segundos', () => {
    service.erro('Falhou');

    vi.advanceTimersByTime(4000);
    expect(service.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(2000);
    expect(service.toasts()).toHaveLength(0);
  });

  it('remover tira apenas o toast indicado', () => {
    service.sucesso('A');
    service.sucesso('B');
    const [primeiro] = service.toasts();

    service.remover(primeiro.id);

    expect(service.toasts().map((t) => t.mensagem)).toEqual(['B']);
  });

  it('erroDaApi usa a mensagem do corpo da resposta', () => {
    service.erroDaApi({ error: { message: 'E-mail já cadastrado' } });
    expect(service.toasts()[0]).toMatchObject({ tipo: 'erro', mensagem: 'E-mail já cadastrado' });
  });

  describe('mensagemDoErro', () => {
    it('usa a alternativa quando não há mensagem', () => {
      expect(mensagemDoErro(null, 'padrão')).toBe('padrão');
      expect(mensagemDoErro({}, 'padrão')).toBe('padrão');
      expect(mensagemDoErro({ error: { message: '   ' } }, 'padrão')).toBe('padrão');
    });

    it('remove espaços da mensagem da API', () => {
      expect(mensagemDoErro({ error: { message: '  Erro  ' } }, 'padrão')).toBe('Erro');
    });
  });
});
