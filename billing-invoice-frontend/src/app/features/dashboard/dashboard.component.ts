import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Invoice } from 'src/app/core/models/invoice.model';
import { Payment } from 'src/app/core/models/payment.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { InvoiceService } from 'src/app/core/services/invoice.service';
import { PaymentTestService } from 'src/app/core/services/payment-test.service';
import { PointDeVenteService } from 'src/app/core/services/point-de-vente.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzSpinModule,
    NzTagModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private readonly invoiceService = inject(InvoiceService);
  private readonly paymentService = inject(PaymentTestService);
  private readonly creancierService = inject(CreancierService);
  private readonly pointDeVenteService = inject(PointDeVenteService);

  loading = true;
  errorMessage = '';

  totalInvoices = 0;
  paidInvoices = 0;
  pendingInvoices = 0;
  totalPayments = 0;
  successfulPayments = 0;
  totalAmount = 0;
  totalCreanciers = 0;
  totalPointsDeVente = 0;
  recentInvoices: Invoice[] = [];
  recentPayments: Payment[] = [];

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      invoices: this.invoiceService.searchInvoices({ page: 0, size: 20 }).pipe(catchError(() => of(null))),
      paidInvoices: this.invoiceService.searchInvoices({ page: 0, size: 1, status: 'PAYEE' }).pipe(catchError(() => of(null))),
      pendingInvoices: this.invoiceService.searchInvoices({ page: 0, size: 1, status: 'EN_ATTENTE' }).pipe(catchError(() => of(null))),
      payments: this.paymentService.searchPayments().pipe(catchError(() => of(null))),
      creanciers: this.creancierService.searchCreanciers({ page: 0, size: 1 }).pipe(catchError(() => of(null))),
      pointsDeVente: this.pointDeVenteService.searchPointDeVentes({ page: 0, size: 1 }).pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ invoices, paidInvoices, pendingInvoices, payments, creanciers, pointsDeVente }) => {
        const invoiceItems = invoices?.content ?? [];
        const paymentItems = payments?.content ?? [];

        this.totalInvoices = invoices?.totalElements ?? invoiceItems.length;
        this.paidInvoices = paidInvoices?.totalElements ?? invoiceItems.filter((invoice) => invoice.status === 'PAYEE').length;
        this.pendingInvoices =
          pendingInvoices?.totalElements ?? invoiceItems.filter((invoice) => invoice.status === 'EN_ATTENTE').length;
        this.totalPayments = payments?.totalElements ?? paymentItems.length;
        this.successfulPayments = paymentItems.filter((payment) => payment.status === 'SUCCESS').length;
        this.totalAmount = paymentItems.reduce((sum, payment) => sum + Number(payment.amount ?? 0), 0);
        this.totalCreanciers = creanciers?.totalElements ?? 0;
        this.totalPointsDeVente = pointsDeVente?.totalElements ?? 0;
        this.recentInvoices = invoiceItems.slice(0, 5);
        this.recentPayments = paymentItems.slice(0, 5);

        if (!invoices || !payments || !creanciers || !pointsDeVente) {
          this.errorMessage = 'Certaines donnees du dashboard n ont pas pu etre chargees.';
        }
      },
      error: () => {
        this.errorMessage = 'Impossible de charger le dashboard.';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  invoiceStatusColor(status: string | null | undefined): string {
    if (status === 'PAYEE' || status === 'PAID') {
      return 'green';
    }
    if (status === 'REJECTED' || status === 'ANNULEE') {
      return 'red';
    }
    return 'gold';
  }

  paymentStatusColor(status: string | null | undefined): string {
    if (status === 'SUCCESS') {
      return 'green';
    }
    if (status === 'FAILED' || status === 'CANCELLED') {
      return 'red';
    }
    return 'gold';
  }
}
