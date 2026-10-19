import { TestBed } from '@angular/core/testing';
import { ConfirmService } from './confirm.service';

describe('ConfirmService', () => {
  let service: ConfirmService;

  beforeEach(() => {
    service = TestBed.inject(ConfirmService);
  });

  it('começa sem diálogo aberto', () => {
    expect(service.estado()).toBeNull();
  });

  it('abre o diálogo com as opções informadas', () => {
    service.perguntar({ titulo: 'Excluir', mensagem: 'Tem certeza?', perigoso: true });

    expect(service.estado()).toMatchObject({ titulo: 'Excluir', mensagem: 'Tem certeza?', perigoso: true });
  });

  it('resolve true ao confirmar e fecha o diálogo', async () => {
    const resposta = service.perguntar({ titulo: 'Excluir', mensagem: 'Tem certeza?' });

    service.responder(true);

    await expect(resposta).resolves.toBe(true);
    expect(service.estado()).toBeNull();
  });

  it('resolve false ao cancelar', async () => {
    const resposta = service.perguntar({ titulo: 'Excluir', mensagem: 'Tem certeza?' });

    service.responder(false);

    await expect(resposta).resolves.toBe(false);
  });

  it('responder sem diálogo aberto não quebra', () => {
    expect(() => service.responder(true)).not.toThrow();
  });
});
