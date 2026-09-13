import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AgentIaComponent } from '../agent-ai/agent-ai.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentIaComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {

  username = localStorage.getItem('username');
  activeTab = 'demandes';
  demandes: any[] = [];
  utilisateurs: any[] = [];
  newUser = { username: '', email: '', password: '', role: 'RESP_VENTE' };
  private baseUrl = 'http://localhost:8099/api';

  constructor(
    private http: HttpClient,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  private getHeaders() {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${localStorage.getItem('token')}`
      })
    };
  }

  ngOnInit() {
    this.loadDemandes();
    this.loadUtilisateurs();
  }

  loadDemandes() {
    this.http.get<any[]>(`${this.baseUrl}/demandes/toutes`, this.getHeaders()).subscribe({
      next: (data) => this.demandes = data,
      error: (err) => console.error('Erreur chargement demandes:', err)
    });
  }

  accepter(id: number) {
    this.http.put(`${this.baseUrl}/demandes/${id}/accepter`, {}, {
      ...this.getHeaders(), responseType: 'text'
    }).subscribe({ next: () => this.loadDemandes() });
  }

  bloquer(id: number) {
    this.http.put(`${this.baseUrl}/demandes/${id}/bloquer`, {}, {
      ...this.getHeaders(), responseType: 'text'
    }).subscribe({ next: () => this.loadDemandes() });
  }

  arreter(id: number) {
    this.http.put(`${this.baseUrl}/demandes/${id}/arreter`, {}, {
      ...this.getHeaders(), responseType: 'text'
    }).subscribe({ next: () => this.loadDemandes() });
  }

  loadUtilisateurs() {
    this.http.get<any[]>(`${this.baseUrl}/auth/admin/utilisateurs`, this.getHeaders()).subscribe({
      next: (data) => this.utilisateurs = data,
      error: (err) => console.error('Erreur chargement utilisateurs:', err)
    });
  }

  ajouterUtilisateur() {
    this.http.post(`${this.baseUrl}/auth/admin/ajouter`, this.newUser, {
      ...this.getHeaders(), responseType: 'text'
    }).subscribe({
      next: () => {
        alert('Utilisateur ajouté !');
        this.newUser = { username: '', email: '', password: '', role: 'RESP_VENTE' };
        this.loadUtilisateurs();
      }
    });
  }

  supprimerUtilisateur(id: number) {
    if (confirm('Supprimer cet utilisateur ?')) {
      this.http.delete(`${this.baseUrl}/auth/admin/supprimer/${id}`, {
        ...this.getHeaders(), responseType: 'text'
      }).subscribe({ next: () => this.loadUtilisateurs() });
    }
  }

  getPFEUrl(): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(
      'https://app.powerbi.com/reportEmbed?reportId=e97c8a88-5f53-497a-bd20-e1507020ab78&autoAuth=true&ctid=1ecd776d-d57f-4de0-a67a-eca9809e8d8d'
    );
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}