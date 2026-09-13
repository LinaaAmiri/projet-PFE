import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  password = '';
  token = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParams['token'];
  }

  reset() {
    if (!this.password) {
      alert("Enter new password");
      return;
    }

    this.auth.resetPassword(this.token, this.password)
      .subscribe({
        next: (res) => {
          if (res === "Password updated") {
            alert("Password updated successfully");
            this.router.navigate(['/login']);
          } else {
            alert(res);
          }
        },
        error: () => {
          alert("Invalid or expired token");
        }
      });
  }
}