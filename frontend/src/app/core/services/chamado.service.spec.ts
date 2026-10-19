import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ChamadoService } from './chamado.service';
import { environment } from '../../../environments/environment';

const URL = `${environment.apiUrl}/v1/chamados`;

describe('ChamadoService', () => {
  let service: ChamadoService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ChamadoService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  describe('listar', () => {
    it('envia paginação padrão', () => {
      service.listar().subscribe();

      const req = backend.expectOne((r) => r.url === URL);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      req.flush({ content: [] });
    });

    it('ignora filtros vazios, nulos ou indefinidos', () => {
      service
        .listar({ busca: 'rede', status: 0, prioridade: null, categoria: undefined, tecnicoId: '' } as never, 2, 20)
        .subscribe();

      const req = backend.expectOne((r) => r.url === URL);
      const params = req.request.params;
      expect(params.get('busca')).toBe('rede');
      expect(params.get('status')).toBe('0');
      expect(params.has('prioridade')).toBe(false);
      expect(params.has('categoria')).toBe(false);
      expect(params.has('tecnicoId')).toBe(false);
      expect(params.get('page')).toBe('2');
      expect(params.get('size')).toBe('20');
      req.flush({ content: [] });
    });
  });

  describe('alterarStatus', () => {
    it('converte o código numérico no nome do enum', () => {
      service.alterarStatus(7, 4).subscribe();

      const req = backend.expectOne(`${URL}/7/status/RESOLVIDO`);
      expect(req.request.method).toBe('PATCH');
      req.flush({});
    });

    it('aceita o nome do status diretamente', () => {
      service.alterarStatus(7, 'CANCELADO').subscribe();
      backend.expectOne(`${URL}/7/status/CANCELADO`).flush({});
    });
  });

  it('atribuirTecnico envia o id no corpo', () => {
    service.atribuirTecnico(3, 9).subscribe();

    const req = backend.expectOne(`${URL}/3/tecnico`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ tecnicoId: 9 });
    req.flush({});
  });

  it('comentar envia texto e flag de interno (padrão falso)', () => {
    service.comentar(3, 'Olá').subscribe();

    const req = backend.expectOne(`${URL}/3/comentarios`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ texto: 'Olá', interno: false });
    req.flush({});
  });

  it('excluirComentario usa DELETE na rota do comentário', () => {
    service.excluirComentario(3, 8).subscribe();

    const req = backend.expectOne(`${URL}/3/comentarios/8`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('enviarAnexo monta um FormData com arquivo e flag interno', () => {
    const arquivo = new File(['conteudo'], 'print.png', { type: 'image/png' });
    service.enviarAnexo(3, arquivo, true).subscribe();

    const req = backend.expectOne(`${URL}/3/anexos`);
    const form = req.request.body as FormData;
    expect(form.get('arquivo')).toBeInstanceOf(File);
    expect(form.get('interno')).toBe('true');
    req.flush({});
  });

  it('baixarAnexo pede a resposta como blob', () => {
    service.baixarAnexo(3, 4).subscribe();

    const req = backend.expectOne(`${URL}/3/anexos/4/download`);
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['x']));
  });

  it('avaliar envia a avaliação via POST', () => {
    service.avaliar(3, { nota: 5, comentario: 'Ótimo' } as never).subscribe();

    const req = backend.expectOne(`${URL}/3/avaliacao`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nota: 5, comentario: 'Ótimo' });
    req.flush({});
  });
});
