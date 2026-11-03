import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard, atendenteGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },


  {
    path: 'login',
    title: 'Entrar · Help Desk',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    title: 'Criar conta · Help Desk',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'esqueci-senha',
    title: 'Recuperar senha · Help Desk',
    loadComponent: () =>
      import('./features/auth/esqueci-senha/esqueci-senha.component').then(
        (m) => m.EsqueciSenhaComponent,
      ),
  },
  {
    path: 'redefinir-senha',
    title: 'Definir nova senha · Help Desk',
    loadComponent: () =>
      import('./features/auth/redefinir-senha/redefinir-senha.component').then(
        (m) => m.RedefinirSenhaComponent,
      ),
  },


  {
    path: 'dashboard',
    title: 'Painel · Help Desk',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'chamados',
    title: 'Chamados · Help Desk',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'chamados/novo',
    title: 'Novo chamado · Help Desk',
    loadComponent: () =>
      import('./features/chamados/novo-chamado/novo-chamado.component').then(
        (m) => m.NovoChamadoComponent,
      ),
    canActivate: [authGuard],
  },
  {

    path: 'fila',
    title: 'Fila de atendimento · Help Desk',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [atendenteGuard],
    data: { modo: 'fila' },
  },
  {

    path: 'meus-chamados',
    title: 'Meus chamados · Help Desk',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(
        (m) => m.ChamadosListComponent,
      ),
    canActivate: [authGuard],
    data: { modo: 'meus' },
  },
  {
    path: 'chamados/:id/editar',
    title: 'Editar chamado · Help Desk',
    loadComponent: () =>
      import('./features/chamados/chamado-edit/chamado-edit.component').then(
        (m) => m.ChamadoEditComponent,
      ),
    canActivate: [atendenteGuard],
  },
  {
    path: 'chamados/:id',
    title: 'Chamado · Help Desk',
    loadComponent: () =>
      import('./features/chamados/chamado-detail/chamado-detail.component').then(
        (m) => m.ChamadoDetailComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'relatorios',
    title: 'Relatórios · Help Desk',
    loadComponent: () =>
      import('./features/relatorios/relatorios.component').then((m) => m.RelatoriosComponent),
    canActivate: [atendenteGuard],
  },
  {
    path: 'usuarios',
    title: 'Usuários · Help Desk',
    loadComponent: () =>
      import('./features/usuarios/usuarios.component').then((m) => m.UsuariosComponent),
    canActivate: [adminGuard],
  },
  {
    path: 'perfil',
    title: 'Meu perfil · Help Desk',
    loadComponent: () =>
      import('./features/perfil/perfil.component').then((m) => m.PerfilComponent),
    canActivate: [authGuard],
  },


  {
    path: 'acesso-negado',
    title: 'Acesso negado · Help Desk',
    loadComponent: () =>
      import('./features/erros/acesso-negado.component').then((m) => m.AcessoNegadoComponent),
  },
  {


    path: '**',
    title: 'Página não encontrada · Help Desk',
    loadComponent: () =>
      import('./features/erros/nao-encontrado.component').then((m) => m.NaoEncontradoComponent),
  },
];
