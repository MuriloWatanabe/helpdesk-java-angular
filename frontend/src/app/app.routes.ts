import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard, atendenteGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },

  // ---------------------------------------------------------------
  // Público
  // ---------------------------------------------------------------
  {
    path: 'login',
    title: 'Entrar · FixLab',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    title: 'Criar conta · FixLab',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'esqueci-senha',
    title: 'Recuperar senha · FixLab',
    loadComponent: () =>
      import('./features/auth/esqueci-senha/esqueci-senha.component').then(
        (m) => m.EsqueciSenhaComponent,
      ),
  },
  {
    path: 'redefinir-senha',
    title: 'Definir nova senha · FixLab',
    loadComponent: () =>
      import('./features/auth/redefinir-senha/redefinir-senha.component').then(
        (m) => m.RedefinirSenhaComponent,
      ),
  },

  // ---------------------------------------------------------------
  // Área autenticada
  // ---------------------------------------------------------------
  {
    path: 'dashboard',
    title: 'Painel · FixLab',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'chamados',
    title: 'Chamados · FixLab',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'chamados/novo',
    title: 'Novo chamado · FixLab',
    loadComponent: () =>
      import('./features/chamados/novo-chamado/novo-chamado.component').then(
        (m) => m.NovoChamadoComponent,
      ),
    canActivate: [authGuard],
  },
  {
    // Fila de atendimento: chamados sem técnico e SLA estourado
    path: 'fila',
    title: 'Fila de atendimento · FixLab',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [atendenteGuard],
    data: { modo: 'fila' },
  },
  {
    // Atalho para "os que eu atendo" / "os meus"
    path: 'meus-chamados',
    title: 'Meus chamados · FixLab',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [authGuard],
    data: { modo: 'meus' },
  },
  {
    path: 'chamados/:id/editar',
    title: 'Editar chamado · FixLab',
    loadComponent: () =>
      import('./features/chamados/chamado-edit/chamado-edit.component').then(
        (m) => m.ChamadoEditComponent,
      ),
    canActivate: [atendenteGuard],
  },
  {
    path: 'chamados/:id',
    title: 'Chamado · FixLab',
    loadComponent: () =>
      import('./features/chamados/chamado-detail/chamado-detail.component').then(
        (m) => m.ChamadoDetailComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'relatorios',
    title: 'Relatórios · FixLab',
    loadComponent: () =>
      import('./features/relatorios/relatorios.component').then((m) => m.RelatoriosComponent),
    canActivate: [atendenteGuard],
  },
  {
    path: 'usuarios',
    title: 'Usuários · FixLab',
    loadComponent: () =>
      import('./features/usuarios/usuarios.component').then((m) => m.UsuariosComponent),
    canActivate: [adminGuard],
  },
  {
    path: 'perfil',
    title: 'Meu perfil · FixLab',
    loadComponent: () =>
      import('./features/perfil/perfil.component').then((m) => m.PerfilComponent),
    canActivate: [authGuard],
  },

  // ---------------------------------------------------------------
  // Erros
  // ---------------------------------------------------------------
  {
    path: 'acesso-negado',
    title: 'Acesso negado · FixLab',
    loadComponent: () =>
      import('./features/erros/acesso-negado.component').then((m) => m.AcessoNegadoComponent),
  },
  {
    // Antes qualquer rota desconhecida jogava o usuário logado para o login,
    // fazendo parecer que a sessão havia caído.
    path: '**',
    title: 'Página não encontrada · FixLab',
    loadComponent: () =>
      import('./features/erros/nao-encontrado.component').then((m) => m.NaoEncontradoComponent),
  },
];
