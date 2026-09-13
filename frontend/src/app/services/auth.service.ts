import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8099/api/auth';

  constructor(private http: HttpClient) {}

  // 🔐 LOGIN
login(username: string, password: string) {
  return this.http.post(
    `${this.baseUrl}/login`,
    null,
    { params: { username, password } }
  );
}

  // 🆕 REGISTER (CORRIGÉ → BODY JSON)
  register(username: string, email: string, password: string) {
    return this.http.post(
      `${this.baseUrl}/register`,
      {
        username,
        email,
        password
      },
      { responseType: 'text' as 'json' }
    );
  }

  // 📧 FORGOT PASSWORD (CORRIGÉ BACK COMPATIBLE)
  forgotPassword(email: string) {
    return this.http.post(
      `${this.baseUrl}/forgot-password`,
      null,
      {
        params: { email },
        responseType: 'text' as 'json'
      }
    );
  }

  // 🔑 RESET PASSWORD
  resetPassword(token: string, password: string) {
    return this.http.post(
      `${this.baseUrl}/reset-password`,
      null,
      {
        params: { token, password },
        responseType: 'text' as 'json'
      }
    );
  }
}