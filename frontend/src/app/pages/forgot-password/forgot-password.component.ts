import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router'; // ✅ Indispensable pour routerLink
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot',
  standalone: true,
  imports: [FormsModule, RouterModule], // ✅ Ajoutez RouterModule ici
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css'] // ✅ VÉRIFIEZ BIEN CETTE LIGNE
})
export class ForgotPasswordComponent {

  email = '';

  constructor(private auth: AuthService) {}

  send() {
    if (!this.email) {
      alert("❌ Veuillez entrer votre adresse e-mail");
      return;
    }

    this.auth.forgotPassword(this.email).subscribe({
      next: (res: any) => {
        const result = res?.trim();
        if (result === "EMAIL_SENT") {
          alert("📧 Lien envoyé ! Vérifiez votre boîte de réception.");
        } 
        else if (result === "EMAIL_NOT_FOUND") {
          alert("❌ Cet e-mail n'existe pas dans notre base.");
        } 
        else {
          alert(result);
        }
      },
      error: () => {
        alert("❌ Erreur serveur");
      }
    });
  }
}