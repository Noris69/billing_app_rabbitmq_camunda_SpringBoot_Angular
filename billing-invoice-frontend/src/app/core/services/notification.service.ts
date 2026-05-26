import { Injectable, NgZone, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Invoice } from '../models/invoice.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly ngZone = inject(NgZone);
  private readonly invoiceUpdateSubject = new Subject<Invoice>();

  private eventSource: EventSource | null = null;

  constructor() {
    this.connect();
  }

  /**
   * Se connecte au flux SSE du serveur
   */
  private connect(): void {
    if (this.eventSource) {
      return;
    }

    // Connexion relative utilisant le même domaine (géré par le proxy de dev Angular en local)
    this.eventSource = new EventSource('/api/invoices/stream');

    this.eventSource.addEventListener('invoice-update', (event: MessageEvent) => {
      this.ngZone.run(() => {
        try {
          const invoice: Invoice = JSON.parse(event.data);
          this.invoiceUpdateSubject.next(invoice);
        } catch (e) {
          console.error('Erreur lors du parsing des données de facture SSE :', e);
        }
      });
    });

    this.eventSource.addEventListener('init', (event: MessageEvent) => {
      console.log('SSE initialisé :', event.data);
    });

    this.eventSource.onerror = (error) => {
      console.warn('Erreur sur le canal SSE, tentative de reconnexion automatique...', error);
      // EventSource se reconnecte automatiquement par défaut,
      // mais s'il est fermé, nous le réinitialisons
      if (this.eventSource?.readyState === EventSource.CLOSED) {
        this.disconnect();
        setTimeout(() => this.connect(), 5000); // Tente de se reconnecter après 5 secondes
      }
    };
  }

  /**
   * Permet aux composants de s'abonner aux changements de factures
   */
  getInvoiceUpdates(): Observable<Invoice> {
    return this.invoiceUpdateSubject.asObservable();
  }

  /**
   * Ferme la connexion SSE proprement
   */
  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
