import { Component, OnInit, OnDestroy, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RapportIaService, RapportResponse } from '../../services/rapport-ia.service';
 
@Component({
  selector: 'app-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employe.component.html',
  styleUrl: './employe.component.css'
})
export class EmployeComponent implements OnInit, OnDestroy, AfterViewChecked {
 
  username: string | null = '';
  userId: string | null = '';
  role: string | null = '';
  demandes: any[] = [];
  rapportsAcceptes: any[] = [];
  rapportChoisi = '';
  activeTab = 'demandes';
  private baseUrl = 'http://localhost:8099/api';
  private refreshInterval: any;
  private urlCache: { [key: string]: SafeResourceUrl } = {};
 
  // ── IA ──────────────────────────────────────────
  typeRapport = '';
  annee = 2024;
  rapportIA: RapportResponse | null = null;
  loadingIA = false;
  erreurIA = '';
  private chartsRendered = false;
 
  COLORS = ['#1e3a5f', '#2563eb', '#3b82f6', '#60a5fa',
            '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];
 
  // ── Mapping : tableau de bord → rapport(s) IA correspondant(s) ──
  private readonly MAPPING_DASHBOARD_IA: Record<string, { value: string; label: string }[]> = {
    'RAPPORT_VENTE': [
      { value: 'VENTES', label: '📈 Rapport de Performance des Ventes' }
    ],
    'RAPPORT_COMMANDE': [
      { value: 'COMMANDES', label: '📦 Rapport de Suivi Commande' }
    ],
    'RAPPORT_GLOBAL': [
      { value: 'VENTES',      label: '📈 Rapport de Performance des Ventes' },
      { value: 'COMMANDES',   label: '📦 Rapport de Suivi Commande' },
      { value: 'SYNTHESE',    label: '🔄 Rapport de Synthèse Commerciale' },
      { value: 'CLASSEMENTS', label: '🏆 Rapport des Performances Clés' }
    ]
  };
 
  get peutConsulterRapports(): boolean {
    return this.rapportsAcceptes.length > 0;
  }
 
  /**
   * Rapports IA disponibles — débloqués uniquement si le tableau de bord
   * correspondant a été accepté (logique parallèle).
   */
  get rapportsDisponiblesIA(): { value: string; label: string }[] {
    const result: { value: string; label: string }[] = [];
 
    // Récupérer les noms des dashboards acceptés
    const dashboardsAcceptes = this.rapportsAcceptes.map(d => d.rapportDemande);
 
    // Pour chaque dashboard accepté, débloquer les rapports IA correspondants
    for (const dashboard of dashboardsAcceptes) {
      const iaRapports = this.MAPPING_DASHBOARD_IA[dashboard];
      if (iaRapports) {
        for (const r of iaRapports) {
          if (!result.find(x => x.value === r.value)) {
            result.push(r);
          }
        }
      }
    }
 
    return result;
  }
 
  constructor(
    private http: HttpClient,
    private router: Router,
    private sanitizer: DomSanitizer,
    private rapportService: RapportIaService
  ) {
    const urls: { [key: string]: string } = {
      'RAPPORT_VENTE':    'https://app.powerbi.com/reportEmbed?reportId=9c0f4607-787b-408e-89b0-72ab752c55c4&autoAuth=true&ctid=1ecd776d-d57f-4de0-a67a-eca9809e8d8d',
      'RAPPORT_COMMANDE': 'https://app.powerbi.com/reportEmbed?reportId=f1d1c8b4-b987-4bf9-bf4f-0b949882b119&autoAuth=true&ctid=1ecd776d-d57f-4de0-a67a-eca9809e8d8d',
      'RAPPORT_GLOBAL':   'https://app.powerbi.com/reportEmbed?reportId=e97c8a88-5f53-497a-bd20-e1507020ab78&autoAuth=true&ctid=1ecd776d-d57f-4de0-a67a-eca9809e8d8d'
    };
    for (const key of Object.keys(urls)) {
      this.urlCache[key] = this.sanitizer.bypassSecurityTrustResourceUrl(urls[key]);
    }
  }
 
  private getHeaders() {
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${localStorage.getItem('token')}`
      })
    };
  }
 
  ngOnInit() {
    this.username = localStorage.getItem('username');
    this.userId   = localStorage.getItem('userId');
    this.role     = localStorage.getItem('role');
    this.initRapportChoisi();
 
    if (this.userId && this.userId !== 'undefined' && this.userId !== 'null') {
      this.loadMesDemandes();
      this.refreshInterval = setInterval(() => this.loadMesDemandes(), 15000);
    }
  }
 
  ngOnDestroy() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }
 
  ngAfterViewChecked() {
    if (this.rapportIA && !this.chartsRendered) {
      this.chartsRendered = true;
      setTimeout(() => this.renderCharts(), 100);
    }
  }
 
  initRapportChoisi() {
    if (this.role === 'RESP_VENTE')         this.rapportChoisi = 'RAPPORT_VENTE';
    else if (this.role === 'RESP_COMMANDE') this.rapportChoisi = 'RAPPORT_COMMANDE';
    else if (this.role === 'RESP_GENERAL')  this.rapportChoisi = 'RAPPORT_VENTE';
    else this.rapportChoisi = '';
  }
 
  /**
   * Sélectionne par défaut le premier rapport IA disponible
   * (appelé après chaque mise à jour des demandes acceptées)
   */
  initTypeRapportIA() {
    const disponibles = this.rapportsDisponiblesIA;
    if (disponibles.length > 0) {
      // Garder la sélection actuelle si elle est encore valide
      const selectionValide = disponibles.find(r => r.value === this.typeRapport);
      if (!selectionValide) {
        this.typeRapport = disponibles[0].value;
      }
    } else {
      this.typeRapport = '';
    }
  }
 
  get rapportsDisponibles(): string[] {
    if (this.role === 'RESP_VENTE')    return ['RAPPORT_VENTE'];
    if (this.role === 'RESP_COMMANDE') return ['RAPPORT_COMMANDE'];
    if (this.role === 'RESP_GENERAL')  return ['RAPPORT_VENTE', 'RAPPORT_COMMANDE', 'RAPPORT_GLOBAL'];
    return [];
  }
 
  loadMesDemandes() {
    this.http.get<any[]>(
      `${this.baseUrl}/demandes/mes-demandes/${this.userId}`,
      this.getHeaders()
    ).subscribe({
      next: (data) => {
        this.demandes = data;
        const nouveauxAcceptes = data.filter(d => d.statut === 'ACCEPTE');
        const ancienIds = this.rapportsAcceptes.map(r => r.id).join(',');
        const nouveauIds = nouveauxAcceptes.map(r => r.id).join(',');
        if (ancienIds !== nouveauIds) {
          this.rapportsAcceptes = nouveauxAcceptes;
          this.initTypeRapportIA(); // ← MAJ du rapport IA sélectionné
        }
      },
      error: (err) => console.error('Erreur chargement demandes:', err)
    });
  }
 
  trackByRapport(index: number, item: any): number { return item.id; }
  getSafeUrl(rapport: string): SafeResourceUrl { return this.urlCache[rapport]; }
 
  deposerDemande() {
    if (!this.rapportChoisi) { alert('Veuillez choisir un rapport.'); return; }
    const dejaAccepte = this.demandes.find(
      d => d.rapportDemande === this.rapportChoisi && d.statut === 'ACCEPTE'
    );
    if (dejaAccepte) { alert('Vous avez déjà accès à ce rapport !'); return; }
    const dejaEnAttente = this.demandes.find(
      d => d.rapportDemande === this.rapportChoisi && d.statut === 'EN_ATTENTE'
    );
    if (dejaEnAttente) { alert('Demande déjà envoyée, en attente de validation !'); return; }
 
    this.http.post(
      `${this.baseUrl}/demandes/demander`,
      null,
      {
        params: { userId: this.userId!, rapport: this.rapportChoisi },
        responseType: 'text',
        headers: new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('token')}` })
      }
    ).subscribe({
      next: () => { alert('Demande envoyée avec succès !'); this.loadMesDemandes(); },
      error: (err) => console.error('Erreur dépôt demande:', err)
    });
  }
 
  genererRapportIA() {
    if (!this.typeRapport) {
      this.erreurIA = 'Aucun rapport IA disponible. Demandez d\'abord l\'accès au tableau de bord correspondant.';
      return;
    }
 
    this.loadingIA = true;
    this.rapportIA = null;
    this.erreurIA = '';
    this.chartsRendered = false;
 
    this.rapportService.genererRapport(this.typeRapport, this.annee).subscribe({
      next: (data: RapportResponse) => {
        this.rapportIA = data;
        this.loadingIA = false;
        this.chartsRendered = false;
        if (data.erreur) this.erreurIA = data.erreur;
      },
      error: () => {
        this.erreurIA = 'Erreur de connexion au serveur.';
        this.loadingIA = false;
      }
    });
  }
 
  async renderCharts() {
    const Chart = (await import('chart.js/auto')).default;
    if (!this.rapportIA?.sections) return;
 
    this.rapportIA.sections.forEach((section, index) => {
      const canvas = document.getElementById(`chartE-${index}`) as HTMLCanvasElement;
      if (!canvas || !section.donnees?.length) return;
 
      const existing = (Chart as any).getChart(canvas);
      if (existing) existing.destroy();
 
      const keys = Object.keys(section.donnees[0]);
      const labelKey = keys.find(k => typeof section.donnees[0][k] === 'string') || keys[0];
      const valueKey = keys.find(k => typeof section.donnees[0][k] === 'number') || keys[1];
      const labels = section.donnees.map(r => String(r[labelKey] ?? ''));
      const values = section.donnees.map(r => Number(r[valueKey] ?? 0));
      const type = section.typeGraphique;
 
      if (type === 'pie' || type === 'donut') {
        new Chart(canvas, {
          type: 'doughnut',
          data: { labels, datasets: [{ data: values, backgroundColor: this.COLORS, borderWidth: 2, borderColor: '#fff' }] },
          options: { responsive: true, cutout: type === 'donut' ? '60%' : '0%', plugins: { legend: { position: 'right' } } }
        });
      } else if (type === 'bar' || type === 'stacked_bar') {
        new Chart(canvas, {
          type: 'bar',
          data: { labels, datasets: [{ label: valueKey, data: values, backgroundColor: this.COLORS[1], borderRadius: 4 }] },
          options: { responsive: true, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
        });
      } else if (type === 'horizontal_bar') {
        new Chart(canvas, {
          type: 'bar',
          data: { labels, datasets: [{ label: valueKey, data: values, backgroundColor: this.COLORS, borderRadius: 4 }] },
          options: { indexAxis: 'y' as const, responsive: true, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true } } }
        });
      } else if (type === 'area' || type === 'line') {
        new Chart(canvas, {
          type: 'line',
          data: { labels, datasets: [{ label: valueKey, data: values, borderColor: this.COLORS[1], backgroundColor: type === 'area' ? 'rgba(37,99,235,0.15)' : 'transparent', fill: type === 'area', tension: 0.4, pointRadius: 4 }] },
          options: { responsive: true, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
        });
      }
    });
  }
 
  telechargerPDF() { window.print(); }
 
  isTableType(type: string): boolean { return type === 'table' || type === 'card' || type === 'treemap'; }
  isChartType(type: string): boolean { return ['pie', 'donut', 'bar', 'horizontal_bar', 'area', 'line', 'stacked_bar'].includes(type); }
  getKeys(row: any): string[] { return row ? Object.keys(row) : []; }
  getIcone(type: string): string {
    const icons: Record<string, string> = {
      card: '🔢', bar: '📊', horizontal_bar: '📊',
      pie: '🥧', donut: '🍩', area: '📈',
      line: '📉', table: '📋', treemap: '🗺️', stacked_bar: '📊'
    };
    return icons[type] || '📊';
  }
 
  logout() {
    clearInterval(this.refreshInterval);
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}