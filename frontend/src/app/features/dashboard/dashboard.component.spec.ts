import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats } from '../../core/models/dashboard.model';
import { Chamado } from '../../core/models/chamado.model';

function chamado(parcial: Partial<Chamado>): Chamado {
  return {
    id: 1,
    numero: 'CH-2026-000001',
    titulo: 'Impressora sem rede',
    observacoes: 'Detalhes',
    status: 1,
    statusLabel: 'Em andamento',
    prioridade: 2,
    prioridadeLabel: 'Alta',
    categoria: 0,
    categoriaLabel: 'Rede e internet',
    cliente: { id: 10, nome: 'Ana', email: 'ana@empresa.com.br' },
    dataAbertura: '2026-08-01T10:00:00',
    prazoSla: '2026-08-02T10:00:00',
    horasRestantesSla: 5,
    slaVencido: false,
    encerrado: false,
    avaliado: false,
    totalComentarios: 0,
    totalAnexos: 0,
    ...parcial,
  } as Chamado;
}

function stats(parcial: Partial<DashboardStats> = {}): DashboardStats {
  return {
    escopo: 'GLOBAL',
    totalChamados: 20,
    totalAbertos: 4,
    totalEmAndamento: 3,
    totalAguardandoCliente: 2,
    totalResolvidos: 5,
    totalEncerrados: 6,
    totalCancelados: 0,
    totalSlaVencido: 1,
    totalSlaEmRisco: 7,
    totalSemTecnico: 2,
    tempoMedioResolucaoHoras: 10,
    notaMediaAtendimento: 4.5,
    totalAvaliacoes: 9,
    porPrioridade: [],
    porCategoria: [],
    porTecnico: [],
    aberturasPorDia: [],
    chamadosRecentes: [chamado({})],
    ...parcial,
  };
}

async function renderizar(perfis: string[], dados: DashboardStats): Promise<HTMLElement> {
  localStorage.setItem('helpdesk_user', JSON.stringify({ id: 1, nome: 'Pessoa Teste', email: 'p@e.com', perfis }));
  await TestBed.configureTestingModule({
    imports: [DashboardComponent],
    providers: [
      provideHttpClient(),
      provideRouter([]),
      { provide: DashboardService, useValue: { getStats: () => of(dados), invalidateCache: () => {} } },
    ],
  }).compileComponents();
  const fixture = TestBed.createComponent(DashboardComponent);
  fixture.detectChanges();
  await fixture.whenStable();
  fixture.detectChanges();
  return fixture.nativeElement as HTMLElement;
}

function cardPorStatus(page: HTMLElement, status: number): HTMLAnchorElement | undefined {
  return Array.from(page.querySelectorAll<HTMLAnchorElement>('.stat-card')).find((a) =>
    new URL(a.href, 'http://x').searchParams.get('status') === String(status),
  );
}

describe('DashboardComponent', () => {
  afterEach(() => localStorage.clear());

  describe('cliente', () => {
    it('o card Resolvidos mostra e abre os chamados RESOLVIDO (4)', async () => {
      const page = await renderizar(['ROLE_CLIENTE'], stats());
      const card = cardPorStatus(page, 4);

      expect(card).toBeTruthy();
      expect(card!.querySelector('strong')?.textContent?.trim()).toBe('5');
      expect(card!.textContent).toContain('Resolvidos');
      expect(card!.textContent).toContain('aguardando confirmação');
      expect(cardPorStatus(page, 2)).toBeUndefined();
    });

    it('destaca os chamados que aguardam ação do cliente (3)', async () => {
      const page = await renderizar(['ROLE_CLIENTE'], stats());
      const card = cardPorStatus(page, 3);

      expect(card).toBeTruthy();
      expect(card!.querySelector('strong')?.textContent?.trim()).toBe('2');
      expect(card!.textContent).toContain('Aguardando você');
    });
  });

  describe('atendente', () => {
    it('mantém prazos em risco e total de avaliações', async () => {
      const texto = (await renderizar(['ROLE_TECNICO'], stats())).textContent ?? '';

      expect(texto).toContain('7 vencem nas próximas 8h');
      expect(texto).toContain('9 avaliação(ões)');
    });

    it('mostra prioridade e prazo em cada chamado recente', async () => {
      const page = await renderizar(['ROLE_ADMIN'], stats());
      const item = page.querySelector('.recent-list li');

      expect(item?.textContent).toContain('Alta');
      expect(item?.textContent).toContain('Faltam 5h');
      expect(item?.querySelector('.badge-alta')).toBeTruthy();
    });
  });
});
