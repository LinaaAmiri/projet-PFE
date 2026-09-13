import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RapportSection {
  titreSection: string;
  typeGraphique: string;
  donnees: any[];
  analyse: string;
  recommandation: string;
}

export interface RapportResponse {
  titre: string;
  sousTitre: string;
  periode: string;
  role: string;
  sections: RapportSection[];
  conclusion: string;
  erreur?: string;
}

@Injectable({ providedIn: 'root' })
export class RapportIaService {

  private baseUrl = 'http://localhost:8099/api/rapport';

  constructor(private http: HttpClient) {}

  private getHeaders() {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      })
    };
  }

  genererRapport(
    typeRapport: string,
    annee: number
  ): Observable<RapportResponse> {
    const body = { typeRapport, annee };
    return this.http.post<RapportResponse>(
      `${this.baseUrl}/generer`,
      body,
      this.getHeaders()
    );
  }
}