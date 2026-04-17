import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  mainNavItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'grid' },
    { label: 'Chamados', route: '/chamados', icon: 'list' },
    { label: 'Detalhe do chamado', route: '/chamados/detalhe', icon: 'file-text' },
    { label: '+ Criar chamado', route: '/chamados/novo', icon: 'plus-circle' }
  ];

  contaNavItems: NavItem[] = [
    { label: 'Meu perfil', route: '/perfil', icon: 'user' }
  ];

  gestaoNavItems: NavItem[] = [
    { label: 'Relatórios', route: '/relatorios', icon: 'bar-chart' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
