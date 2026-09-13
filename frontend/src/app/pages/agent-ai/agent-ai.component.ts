import { Component, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RapportIaService, RapportResponse } from '../../services/rapport-ia.service';

@Component({
  selector: 'app-agent-ai',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-ai.component.html',
  styleUrl: './agent-ai.component.css'
})
export class AgentIaComponent implements AfterViewChecked {

  typeRapport = 'VENTES';
  annee = 2024;
  rapport: RapportResponse | null = null;
  loading = false;
  erreur = '';
  private chartsRendered = false;

  rapports = [
    { value: 'VENTES',      label: '📈 Rapport de Performance des Ventes' },
    { value: 'COMMANDES',   label: '📦 Rapport de Suivi Commande' },
    { value: 'SYNTHESE',    label: '🔄 Rapport de Synthèse Commerciale' },
    { value: 'CLASSEMENTS', label: '🏆 Rapport des Performances Clés' },
  ];

  COLORS = ['#1e3a5f','#2563eb','#3b82f6','#60a5fa',
            '#10b981','#f59e0b','#ef4444','#8b5cf6'];

  constructor(private rapportService: RapportIaService) {}

  generer() {
    this.loading = true;
    this.rapport = null;
    this.erreur = '';
    this.chartsRendered = false;
    this.rapportService.genererRapport(
      this.typeRapport, this.annee
    ).subscribe({
      next: (data: RapportResponse) => {
        this.rapport = data;
        this.loading = false;
        this.chartsRendered = false;
        if (data.erreur) this.erreur = data.erreur;
      },
      error: () => {
        this.erreur = 'Erreur de connexion au serveur.';
        this.loading = false;
      }
    });
  }

  ngAfterViewChecked() {
    if (this.rapport && !this.chartsRendered) {
      this.chartsRendered = true;
      setTimeout(() => this.renderCharts(), 100);
    }
  }

  async renderCharts() {
    const Chart = (await import('chart.js/auto')).default;
    if (!this.rapport?.sections) return;

    this.rapport.sections.forEach((section, index) => {
      const canvasId = `chart-${index}`;
      const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
      if (!canvas || !section.donnees?.length) return;

      const existing = (Chart as any).getChart(canvas);
      if (existing) existing.destroy();

      const keys = Object.keys(section.donnees[0]);
      const labelKey = keys.find(k =>
        typeof section.donnees[0][k] === 'string') || keys[0];
      const valueKey = keys.find(k =>
        typeof section.donnees[0][k] === 'number') || keys[1];

      const labels = section.donnees.map(r => String(r[labelKey] ?? ''));
      const values = section.donnees.map(r => Number(r[valueKey] ?? 0));
      const type = section.typeGraphique;

      if (type === 'pie' || type === 'donut') {
        new Chart(canvas, {
          type: 'doughnut',
          data: {
            labels,
            datasets: [{
              data: values,
              backgroundColor: this.COLORS,
              borderWidth: 2,
              borderColor: '#fff'
            }]
          },
          options: {
            responsive: true,
            cutout: type === 'donut' ? '60%' : '0%',
            plugins: { legend: { position: 'right' } }
          }
        });
      } else if (type === 'bar' || type === 'stacked_bar') {
        new Chart(canvas, {
          type: 'bar',
          data: {
            labels,
            datasets: [{
              label: valueKey,
              data: values,
              backgroundColor: this.COLORS[1],
              borderRadius: 4
            }]
          },
          options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
          }
        });
      } else if (type === 'horizontal_bar') {
        new Chart(canvas, {
          type: 'bar',
          data: {
            labels,
            datasets: [{
              label: valueKey,
              data: values,
              backgroundColor: this.COLORS,
              borderRadius: 4
            }]
          },
          options: {
            indexAxis: 'y' as const,
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { x: { beginAtZero: true } }
          }
        });
      } else if (type === 'area' || type === 'line') {
        new Chart(canvas, {
          type: 'line',
          data: {
            labels,
            datasets: [{
              label: valueKey,
              data: values,
              borderColor: this.COLORS[1],
              backgroundColor: type === 'area'
                ? 'rgba(37,99,235,0.15)' : 'transparent',
              fill: type === 'area',
              tension: 0.4,
              pointRadius: 4
            }]
          },
          options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
          }
        });
      }
    });
  }

  telechargerPDF() {
    window.print();
  }

  isTableType(type: string): boolean {
    return type === 'table' || type === 'card' || type === 'treemap';
  }

  isChartType(type: string): boolean {
    return ['pie','donut','bar','horizontal_bar',
            'area','line','stacked_bar'].includes(type);
  }

  getKeys(row: any): string[] {
    return row ? Object.keys(row) : [];
  }

  getIcone(type: string): string {
    const icons: Record<string, string> = {
      card: '🔢', bar: '📊', horizontal_bar: '📊',
      pie: '🥧', donut: '🍩', area: '📈',
      line: '📉', table: '📋', treemap: '🗺️',
      stacked_bar: '📊'
    };
    return icons[type] || '📊';
  }
}