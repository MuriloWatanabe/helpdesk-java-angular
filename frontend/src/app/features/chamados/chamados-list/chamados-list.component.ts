import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Chamado, ChamadoFiltro } from '../../../core/models/chamado.model';
import { Opcao, UsuarioDiretorio } from '../../../core/models/usuario.model';
import { classeStatus, classePrioridade, textoSla } from '../chamado-ui';

type Modo = 'todos' | 'fila' | 'meus';

@Component({
  selector: 'app-chamados-list',
  standalone: true,
  imports: [RouterModule, FormsModule, DatePipe, SidebarComponent],
  templateUrl: './chamados-list.component.html',
  styleUrl: './chamados-list.component.scss',
})
export class ChamadosListComponent implements OnInit {
  private readonly chamadoService = inject(ChamadoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly isAtendente = this.authService.isAtendente;
  readonly isCliente = this.authService.isCliente;

  chamados = signal<Chamado[]>([]);
  loading = signal(false);
  error = signal('');

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  readonly pageSize = 10;

  busca = '';
  filtroStatus: number | null = null;
  filtroPrioridade: number | null = null;
  filtroCategoria: number | null = null;
  filtroTecnico: number | null = null;
  soSlaVencido = false;
  soSemTecnico = false;
  soPendentes = false;

  statusOpcoes = signal<Opcao[]>([]);
  prioridadeOpcoes = signal<Opcao[]>([]);
  categoriaOpcoes = signal<Opcao[]>([]);
  tecnicos = signal<UsuarioDiretorio[]>([]);

  modo: Modo = 'todos';
  mostrarFiltrosAvancados = false;

  private readonly buscaSubject = new Subject<string>();
  private ultimaRequisicao = 0;

  readonly classeStatus = classeStatus;
  readonly classePrioridade = classePrioridade;
  readonly textoSla = textoSla;

  ngOnInit(): void {
    this.modo = (this.route.snapshot.data['modo'] as Modo) ?? 'todos';
    this.aplicarPreDefinicoesDoModo();
    this.aplicarFiltrosDaUrl();

    this.buscaSubject
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage = 0;
        this.carregar();
      });

    this.carregarOpcoes();
    this.carregar();
  }

  get titulo(): string {
    if (this.modo === 'fila') return 'Fila de atendimento';
    if (this.modo === 'meus') return 'Atribuídos a mim';
    return this.isCliente() ? 'Meus chamados' : 'Todos os chamados';
  }

  get subtitulo(): string {
    if (this.modo === 'fila') return 'Chamados aguardando atribuição ou com prazo estourado';
    if (this.modo === 'meus') return 'Chamados sob sua responsabilidade';
    return this.isCliente()
      ? 'Acompanhe as solicitações que você abriu'
      : 'Todos os chamados registrados no sistema';
  }

  private aplicarPreDefinicoesDoModo(): void {
    if (this.modo === 'fila') {
      this.soSemTecnico = true;
      this.mostrarFiltrosAvancados = true;
    } else if (this.modo === 'meus') {
      this.filtroTecnico = this.authService.getUsuarioAtual()?.id ?? null;
    }
  }

  private aplicarFiltrosDaUrl(): void {
    const params = this.route.snapshot.queryParamMap;
    this.filtroStatus = this.numeroDoParametro(params.get('status'));
    this.filtroPrioridade = this.numeroDoParametro(params.get('prioridade'));
    this.filtroCategoria = this.numeroDoParametro(params.get('categoria'));
    this.soSlaVencido = params.get('slaVencido') === 'true';
    this.soSemTecnico = this.soSemTecnico || params.get('semTecnico') === 'true';
    this.soPendentes = params.get('apenasPendentes') === 'true';

    if (this.isAtendente()) {
      this.filtroTecnico = this.numeroDoParametro(params.get('tecnicoId')) ?? this.filtroTecnico;
    }
    this.mostrarFiltrosAvancados = this.mostrarFiltrosAvancados || !!(
      this.filtroPrioridade !== null ||
      this.filtroCategoria !== null ||
      this.soSlaVencido ||
      this.soSemTecnico
    );
  }

  private numeroDoParametro(valor: string | null): number | null {
    if (valor === null || valor.trim() === '') return null;
    const numero = Number(valor);
    return Number.isInteger(numero) && numero >= 0 ? numero : null;
  }

  private carregarOpcoes(): void {
    this.usuarioService.metadados().subscribe({
      next: (meta) => {
        this.statusOpcoes.set(meta.status);
        this.prioridadeOpcoes.set(meta.prioridades);
        this.categoriaOpcoes.set(meta.categorias);
      },
    });

    if (this.isAtendente()) {
      this.usuarioService.listarDiretorio().subscribe({
        next: (lista) =>
          this.tecnicos.set(
            lista.filter(
              (usuario) =>
                usuario.perfis.includes('ROLE_TECNICO') || usuario.perfis.includes('ROLE_ADMIN'),
            ),
          ),
      });
    }
  }

  carregar(): void {
    const requisicao = ++this.ultimaRequisicao;
    this.loading.set(true);
    this.error.set('');

    this.chamadoService.listar(this.montarFiltro(), this.currentPage, this.pageSize).subscribe({
      next: (page) => {
        if (requisicao !== this.ultimaRequisicao) return;
        this.chamados.set(page.content);
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.loading.set(false);
      },
      error: () => {
        if (requisicao !== this.ultimaRequisicao) return;
        this.error.set('Não foi possível carregar os chamados. Tente novamente.');
        this.loading.set(false);
      },
    });
  }

  private montarFiltro(): ChamadoFiltro {
    return {
      q: this.busca.trim() || undefined,
      status: this.filtroStatus,
      prioridade: this.filtroPrioridade,
      categoria: this.filtroCategoria,
      tecnicoId: this.filtroTecnico,
      semTecnico: this.soSemTecnico || undefined,
      slaVencido: this.soSlaVencido || undefined,
      apenasPendentes: this.soPendentes || undefined,
    };
  }

  aoDigitarBusca(valor: string): void {
    this.busca = valor;
    this.buscaSubject.next(valor);
  }

  filtrar(): void {
    this.currentPage = 0;
    this.carregar();
  }

  filtrarStatus(status: number | null): void {
    this.filtroStatus = status;
    this.filtrar();
  }

  limparFiltros(): void {
    this.busca = '';
    this.filtroStatus = null;
    this.filtroPrioridade = null;
    this.filtroCategoria = null;
    this.soSlaVencido = false;
    this.soSemTecnico = this.modo === 'fila';
    this.soPendentes = false;
    this.filtroTecnico = this.modo === 'meus' ? this.filtroTecnico : null;
    this.filtrar();
  }

  get temFiltroAtivo(): boolean {
    return !!(
      this.busca ||
      this.filtroStatus !== null ||
      this.filtroPrioridade !== null ||
      this.filtroCategoria !== null ||
      (this.filtroTecnico !== null && this.modo !== 'meus') ||
      this.soSlaVencido ||
      this.soPendentes ||
      (this.soSemTecnico && this.modo !== 'fila')
    );
  }

  assumir(chamado: Chamado, evento: Event): void {
    evento.stopPropagation();
    evento.preventDefault();

    this.chamadoService.assumir(chamado.id).subscribe({
      next: () => {
        this.toast.sucesso(`Chamado ${chamado.numero} atribuído a você.`);
        this.carregar();
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível assumir o chamado.'),
    });
  }

  paginaAnterior(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.carregar();
    }
  }

  proximaPagina(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.carregar();
    }
  }

  irParaPagina(p: number): void {
    this.currentPage = p;
    this.carregar();
  }

  get paginas(): number[] {
    const maximo = Math.min(this.totalPages, 7);
    let inicio = Math.max(0, this.currentPage - 3);
    if (inicio + maximo > this.totalPages) {
      inicio = Math.max(0, this.totalPages - maximo);
    }
    return Array.from({ length: maximo }, (_, i) => inicio + i);
  }
}
