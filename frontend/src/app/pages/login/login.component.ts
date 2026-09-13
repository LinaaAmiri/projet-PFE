import { Component, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent {

  username = '';
  password = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  login() {
  this.auth.login(this.username, this.password)
    .subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('username', res.username);
        localStorage.setItem('role', res.role);
        
        // CORRECTION : Utilise res.userId car c'est ce que tu as mis dans ta Map Java
        localStorage.setItem('userId', res.userId); 
        
        console.log("Login réussi ! ID stocké :", res.userId); // Petit test pour vérifier

        if (res.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/employe']);
        }
      },
      error: (err) => {
        alert("Erreur de connexion");
      }
    });
}
}