import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'chamados',
    loadComponent: () =>
      import('./features/chamados/chamados-list/chamados-list.component').then(m => m.ChamadosListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'chamados/novo',
    loadComponent: () =>
      import('./features/chamados/novo-chamado/novo-chamado.component').then(m => m.NovoChamadoComponent),
    canActivate: [authGuard]
  },
  {
    path: 'chamados/:id',
    loadComponent: () =>
      import('./features/chamados/chamado-detail/chamado-detail.component').then(m => m.ChamadoDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'usuarios',
    loadComponent: () =>
      import('./features/usuarios/usuarios.component').then(m => m.UsuariosComponent),
    canActivate: [adminGuard]
  },
  {
    path: 'perfil',
    loadComponent: () =>
      import('./features/perfil/perfil.component').then(m => m.PerfilComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
