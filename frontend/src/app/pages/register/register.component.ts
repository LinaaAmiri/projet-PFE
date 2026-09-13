import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';



@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'] // <-- AJOUTE CETTE LIGNE
})


export class RegisterComponent {

  username = '';
  email = '';
  password = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

register() {

  if (!this.username || !this.email || !this.password) {
    alert("❌ Please fill all fields");
    return;
  }

  this.auth.register(this.username, this.email, this.password)
    .subscribe({
      next: (res) => {

        console.log("SUCCESS:", res);

        if (res === "User created successfully") {
          alert("✅ Account created successfully");
          this.router.navigate(['/login']);
        } else {
          alert(res);
        }
      },

      error: (err) => {
        console.log("ERROR:", err);
        alert("❌ Registration failed");
      }
    });
}
}