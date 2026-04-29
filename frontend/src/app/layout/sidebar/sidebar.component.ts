import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  userName     = '';
  userRole     = '';
  userInitials = '?';
  isAdmin      = false;
  isTecnico    = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    const user = this.authService.getUsuarioAtual();
    if (user) {
      this.userName     = user.nome || user.email;
      this.userRole     = this.authService.getPerfilLabel(user.perfis);
      this.userInitials = this.authService.getIniciais(user.nome || user.email);
      this.isAdmin      = this.authService.isAdmin();
      this.isTecnico    = this.authService.isTecnico();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
