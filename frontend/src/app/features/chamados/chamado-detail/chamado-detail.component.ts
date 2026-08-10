import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { ChamadoService } from '../../../core/services/chamado.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../core/services/confirm.service';
import {
  Anexo,
  Avaliacao,
  Chamado,
  Comentario,
  HistoricoItem,
  STATUS_ENUM_NOME,
  StatusChamado,
} from '../../../core/models/chamado.model';
import { Opcao, Usuario } from '../../../core/models/usuario.model';
import {
  classePrioridade,
  classeStatus,
  iniciais,
  textoSla,
  transicoesDoCliente,
  transicoesPermitidas,
} from '../chamado-ui';

const ASPECTOS_AVALIACAO = [
  'Rapidez',
  'Cordialidade',
  'Solução completa',
  'Clareza na comunicação',
];

@Component({
  selector: 'app-chamado-detail',
  standalone: true,
  imports: [FormsModule, RouterModule, DatePipe, SidebarComponent],
  templateUrl: './chamado-detail.component.html',
  styleUrl: './chamado-detail.component.scss',
})
export class ChamadoDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly chamadoService = inject(ChamadoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);

  readonly isAdmin = this.authService.isAdmin;
  readonly isAtendente = this.authService.isAtendente;

  chamado = signal<Chamado | null>(null);
  comentarios = signal<Comentario[]>([]);
  anexos = signal<Anexo[]>([]);
  historico = signal<HistoricoItem[]>([]);
  avaliacao = signal<Avaliacao | null>(null);

  loading = signal(true);
  erro = signal('');


  novoComentario = '';
  comentarioInterno = false;
  enviandoComentario = signal(false);


  arquivoSelecionado: File | null = null;
  anexoInterno = false;
  enviandoAnexo = signal(false);


  processando = signal(false);
  tecnicos = signal<Usuario[]>([]);
  tecnicoSelecionadoId: number | null = null;
  statusOpcoes = signal<Opcao[]>([]);


  readonly aspectosDisponiveis = ASPECTOS_AVALIACAO;
  notaEscolhida = 0;
  comentarioAvaliacao = '';
  aspectosEscolhidos = new Set<string>();
  enviandoAvaliacao = signal(false);

  abaLateral: 'acoes' | 'historico' = 'acoes';
  linkCopiado = signal(false);

  readonly classeStatus = classeStatus;
  readonly classePrioridade = classePrioridade;
  readonly textoSla = textoSla;
  readonly iniciais = iniciais;


  private readonly meuId = this.authService.getUsuarioAtual()?.id ?? null;


  readonly acoesDeStatus = computed<Opcao[]>(() => {
    const atual = this.chamado();
    if (!atual) return [];

    const permitidas = this.isAtendente()
      ? transicoesPermitidas(atual.status)
      : transicoesDoCliente(atual.status);

    return this.statusOpcoes().filter((o) => permitidas.includes(o.codigo));
  });


  readonly souOResponsavel = computed(() => {
    const tecnico = this.chamado()?.tecnico;
    return !!tecnico && tecnico.id === this.meuId;
  });


  readonly tecnicosDisponiveis = computed<{ id: number; nome: string }[]>(() => {
    const lista: { id: number; nome: string }[] = this.tecnicos();
    const atual = this.chamado()?.tecnico;
    if (!atual || lista.some((t) => t.id === atual.id)) return lista;
    return [...lista, { id: atual.id, nome: atual.nome }];
  });

  readonly ehMeuChamado = computed(() => this.chamado()?.cliente?.id === this.meuId);


  readonly podeAvaliar = computed(() => {
    const atual = this.chamado();
    if (!atual || this.avaliacao()) return false;
    const resolvido =
      atual.status === StatusChamado.RESOLVIDO || atual.status === StatusChamado.ENCERRADO;
    return resolvido && this.ehMeuChamado();
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.erro.set('Chamado inválido.');
      this.loading.set(false);
      return;
    }

    this.carregarChamado(id);

    this.usuarioService.metadados().subscribe({
      next: (meta) => this.statusOpcoes.set(meta.status),
    });

    if (this.isAtendente()) {
      this.usuarioService.listar({ perfil: 2, ativo: true }).subscribe({
        next: (lista) => this.tecnicos.set(lista),
      });
    }
  }

  private carregarChamado(id: number): void {
    this.chamadoService.buscarPorId(id).subscribe({
      next: (dados) => {
        this.chamado.set(dados);
        this.tecnicoSelecionadoId = dados.tecnico?.id ?? null;
        this.loading.set(false);
        this.carregarRelacionados(id);
      },
      error: (err) => {
        this.erro.set(
          err.status === 403
            ? 'Você não tem acesso a este chamado.'
            : 'Chamado não encontrado.',
        );
        this.loading.set(false);
      },
    });
  }

  private carregarRelacionados(id: number): void {
    this.chamadoService.listarComentarios(id).subscribe({
      next: (lista) => this.comentarios.set(lista),
    });
    this.chamadoService.listarAnexos(id).subscribe({
      next: (lista) => this.anexos.set(lista),
    });
    this.chamadoService.historico(id).subscribe({
      next: (lista) => this.historico.set(lista),
    });
    this.chamadoService.buscarAvaliacao(id).subscribe({
      next: (dados) => this.avaliacao.set(dados ?? null),
    });
  }

  private recarregar(): void {
    const atual = this.chamado();
    if (atual) this.carregarChamado(atual.id);
  }


  enviarComentario(): void {
    const atual = this.chamado();
    const texto = this.novoComentario.trim();
    if (!atual || texto.length < 2 || this.enviandoComentario()) return;

    this.enviandoComentario.set(true);
    this.chamadoService.comentar(atual.id, texto, this.comentarioInterno).subscribe({
      next: (comentario) => {
        this.comentarios.update((lista) => [...lista, comentario]);
        this.novoComentario = '';
        this.comentarioInterno = false;
        this.enviandoComentario.set(false);
        this.chamadoService.historico(atual.id).subscribe({
          next: (lista) => this.historico.set(lista),
        });
      },
      error: (err) => {
        this.enviandoComentario.set(false);
        this.toast.erroDaApi(err, 'Não foi possível enviar o comentário.');
      },
    });
  }

  podeExcluirComentario(comentario: Comentario): boolean {
    return this.isAdmin() || comentario.autor?.id === this.meuId;
  }

  async excluirComentario(comentario: Comentario): Promise<void> {
    const atual = this.chamado();
    if (!atual) return;

    const confirmado = await this.confirmService.perguntar({
      titulo: 'Excluir comentário',
      mensagem: 'Esta ação não pode ser desfeita. Deseja continuar?',
      confirmar: 'Excluir',
      perigoso: true,
    });
    if (!confirmado) return;

    this.chamadoService.excluirComentario(atual.id, comentario.id).subscribe({
      next: () => {
        this.comentarios.update((lista) => lista.filter((c) => c.id !== comentario.id));
        this.toast.sucesso('Comentário excluído.');
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível excluir o comentário.'),
    });
  }


  aoEscolherArquivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.arquivoSelecionado = input.files?.[0] ?? null;
  }

  enviarAnexo(input: HTMLInputElement): void {
    const atual = this.chamado();
    if (!atual || !this.arquivoSelecionado || this.enviandoAnexo()) return;

    this.enviandoAnexo.set(true);
    this.chamadoService
      .enviarAnexo(atual.id, this.arquivoSelecionado, this.anexoInterno)
      .subscribe({
        next: (anexo) => {
          this.anexos.update((lista) => [anexo, ...lista]);
          this.arquivoSelecionado = null;
          this.anexoInterno = false;
          input.value = '';
          this.enviandoAnexo.set(false);
          this.toast.sucesso('Anexo enviado.');
        },
        error: (err) => {
          this.enviandoAnexo.set(false);
          this.toast.erroDaApi(err, 'Não foi possível enviar o anexo.');
        },
      });
  }


  baixarAnexo(anexo: Anexo): void {
    const atual = this.chamado();
    if (!atual) return;

    this.chamadoService.baixarAnexo(atual.id, anexo.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = anexo.nomeArquivo;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível baixar o arquivo.'),
    });
  }

  podeExcluirAnexo(anexo: Anexo): boolean {
    return this.isAdmin() || anexo.enviadoPor?.id === this.meuId;
  }

  async excluirAnexo(anexo: Anexo): Promise<void> {
    const atual = this.chamado();
    if (!atual) return;

    const confirmado = await this.confirmService.perguntar({
      titulo: 'Excluir anexo',
      mensagem: `O arquivo "${anexo.nomeArquivo}" será removido definitivamente.`,
      confirmar: 'Excluir',
      perigoso: true,
    });
    if (!confirmado) return;

    this.chamadoService.excluirAnexo(atual.id, anexo.id).subscribe({
      next: () => {
        this.anexos.update((lista) => lista.filter((a) => a.id !== anexo.id));
        this.toast.sucesso('Anexo excluído.');
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível excluir o anexo.'),
    });
  }


  alterarStatus(codigo: number): void {
    const atual = this.chamado();
    if (!atual || this.processando()) return;

    this.processando.set(true);
    this.chamadoService.alterarStatus(atual.id, STATUS_ENUM_NOME[codigo]).subscribe({
      next: (atualizado) => {
        this.chamado.set(atualizado);
        this.processando.set(false);
        this.toast.sucesso(`Chamado agora está "${atualizado.statusLabel}".`);
        this.carregarRelacionados(atualizado.id);
      },
      error: (err) => {
        this.processando.set(false);
        this.toast.erroDaApi(err, 'Não foi possível alterar o status.');
      },
    });
  }

  assumir(): void {
    const atual = this.chamado();
    if (!atual || this.processando()) return;

    this.processando.set(true);
    this.chamadoService.assumir(atual.id).subscribe({
      next: (atualizado) => {
        this.chamado.set(atualizado);
        this.tecnicoSelecionadoId = atualizado.tecnico?.id ?? null;
        this.processando.set(false);
        this.toast.sucesso('Chamado atribuído a você.');
        this.carregarRelacionados(atualizado.id);
      },
      error: (err) => {
        this.processando.set(false);
        this.toast.erroDaApi(err, 'Não foi possível assumir o chamado.');
      },
    });
  }


  async desassumir(): Promise<void> {
    const atual = this.chamado();
    if (!atual?.tecnico || this.processando()) return;

    const proprio = this.souOResponsavel();
    const confirmado = await this.confirmService.perguntar({
      titulo: proprio ? 'Desassumir este chamado?' : `Liberar o chamado ${atual.numero}?`,
      mensagem: proprio
        ? 'O chamado volta para a fila, sem responsável, até que alguém o assuma.'
        : `${atual.tecnico.nome} deixa de ser o responsável e o chamado volta para a fila.`,
      confirmar: proprio ? 'Desassumir' : 'Liberar',
    });
    if (!confirmado) return;

    this.processando.set(true);
    this.chamadoService.atribuirTecnico(atual.id, null).subscribe({
      next: (atualizado) => {
        this.chamado.set(atualizado);
        this.tecnicoSelecionadoId = null;
        this.processando.set(false);
        this.toast.sucesso('Chamado devolvido para a fila.');
        this.carregarRelacionados(atualizado.id);
      },
      error: (err) => {
        this.processando.set(false);
        this.toast.erroDaApi(err, 'Não foi possível liberar o chamado.');
      },
    });
  }

  alterarTecnico(): void {
    const atual = this.chamado();
    if (!atual || this.processando()) return;

    this.processando.set(true);
    this.chamadoService.atribuirTecnico(atual.id, this.tecnicoSelecionadoId).subscribe({
      next: (atualizado) => {
        this.chamado.set(atualizado);
        this.tecnicoSelecionadoId = atualizado.tecnico?.id ?? null;
        this.processando.set(false);
        this.toast.sucesso('Responsável atualizado.');
        this.carregarRelacionados(atualizado.id);
      },
      error: (err) => {
        this.processando.set(false);
        this.toast.erroDaApi(err, 'Não foi possível alterar o técnico.');
      },
    });
  }

  async excluirChamado(): Promise<void> {
    const atual = this.chamado();
    if (!atual) return;

    const confirmado = await this.confirmService.perguntar({
      titulo: `Excluir o chamado ${atual.numero}?`,
      mensagem:
        'Comentários, anexos e histórico serão apagados junto. Esta ação não pode ser desfeita.',
      confirmar: 'Excluir chamado',
      perigoso: true,
    });
    if (!confirmado) return;

    this.chamadoService.excluir(atual.id).subscribe({
      next: () => {
        this.toast.sucesso('Chamado excluído.');
        this.router.navigate(['/chamados']);
      },
      error: (err) => this.toast.erroDaApi(err, 'Não foi possível excluir o chamado.'),
    });
  }


  alternarAspecto(aspecto: string): void {
    if (this.aspectosEscolhidos.has(aspecto)) {
      this.aspectosEscolhidos.delete(aspecto);
    } else {
      this.aspectosEscolhidos.add(aspecto);
    }
  }

  enviarAvaliacao(): void {
    const atual = this.chamado();
    if (!atual || this.notaEscolhida < 1 || this.enviandoAvaliacao()) return;

    this.enviandoAvaliacao.set(true);
    this.chamadoService
      .avaliar(atual.id, {
        nota: this.notaEscolhida,
        comentario: this.comentarioAvaliacao.trim() || undefined,
        aspectos: [...this.aspectosEscolhidos],
      })
      .subscribe({
        next: (resultado) => {
          this.avaliacao.set(resultado);
          this.enviandoAvaliacao.set(false);
          this.toast.sucesso('Obrigado pela avaliação!');
          this.recarregar();
        },
        error: (err) => {
          this.enviandoAvaliacao.set(false);
          this.toast.erroDaApi(err, 'Não foi possível registrar a avaliação.');
        },
      });
  }


  copiarLink(): void {
    navigator.clipboard
      .writeText(globalThis.location.href)
      .then(() => {
        this.linkCopiado.set(true);
        setTimeout(() => this.linkCopiado.set(false), 2000);
      })
      .catch(() => this.toast.erro('Não foi possível copiar o link.'));
  }

  voltar(): void {
    this.router.navigate(['/chamados']);
  }

  iconeHistorico(tipo: string): string {
    const icones: Record<string, string> = {
      CRIACAO: '✚',
      STATUS: '⇄',
      TECNICO: '👤',
      PRIORIDADE: '⚑',
      COMENTARIO: '💬',
      FECHAMENTO: '✓',
      REABERTURA: '↺',
      TITULO: '✎',
    };
    return icones[tipo] ?? '•';
  }
}
