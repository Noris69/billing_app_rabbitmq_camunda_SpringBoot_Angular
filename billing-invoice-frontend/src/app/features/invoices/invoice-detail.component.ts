import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, map, of, switchMap, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Creancier } from 'src/app/core/models/creancier.model';
import { Customer } from 'src/app/core/models/customer.model';
import { Invoice, InvoiceStatus } from 'src/app/core/models/invoice.model';
import { PointDeVente } from 'src/app/core/models/point-de-vente.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { CustomerService } from 'src/app/core/services/customer.service';
import { InvoiceService } from 'src/app/core/services/invoice.service';
import { PointDeVenteService } from 'src/app/core/services/point-de-vente.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzSpinModule,
    NzTagModule
  ],
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly invoiceService = inject(InvoiceService);
  private readonly customerService = inject(CustomerService);
  private readonly creancierService = inject(CreancierService);
  private readonly pointDeVenteService = inject(PointDeVenteService);

  invoice: Invoice | null = null;
  customer: Customer | null = null;
  creancier: Creancier | null = null;
  pointDeVente: PointDeVente | null = null;
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Facture introuvable.';
      return;
    }

    this.loadInvoice(id);
  }

  loadInvoice(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.invoiceService
      .getInvoiceById(id)
      .pipe(
        switchMap((invoice) =>
          forkJoin({
            invoice: of(invoice),
            customer: this.findCustomerById(invoice.customerId).pipe(catchError(() => of(null))),
            creancier: invoice.creancierId
              ? this.creancierService.getCreancierById(invoice.creancierId).pipe(catchError(() => of(null)))
              : of(null),
            pointDeVente: invoice.pointDeVenteId
              ? this.pointDeVenteService.getPointDeVenteById(invoice.pointDeVenteId).pipe(catchError(() => of(null)))
              : of(null)
          })
        ),
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: ({ invoice, customer, creancier, pointDeVente }) => {
          this.invoice = invoice;
          this.customer = customer;
          this.creancier = creancier;
          this.pointDeVente = pointDeVente;
        },
        error: (error: unknown) => {
          this.invoice = null;
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger le detail de la facture.');
        }
      });
  }

  statusColor(status: InvoiceStatus | null | undefined): string {
    if (status === 'PAYEE' || status === 'PAID') {
      return 'green';
    }
    if (status === 'ANNULEE' || status === 'REJECTED') {
      return 'red';
    }
    if (status === 'EN_RETARD') {
      return 'orange';
    }
    return 'gold';
  }

  isPaid(status: InvoiceStatus | null | undefined): boolean {
    return status === 'PAYEE' || status === 'PAID';
  }

  customerLabel(): string {
    return this.customer ? `${this.customer.nom} ${this.customer.prenom}` : this.fallbackId(this.invoice?.customerId);
  }

  creancierLabel(): string {
    return this.creancier?.nom ?? this.fallbackId(this.invoice?.creancierId);
  }

  pointDeVenteLabel(): string {
    return this.pointDeVente?.nom ?? this.fallbackId(this.invoice?.pointDeVenteId);
  }

  private findCustomerById(customerId: number | null | undefined) {
    if (!customerId) {
      return of(null);
    }

    return this.customerService.searchCustomers({ page: 0, size: 500 }).pipe(
      map((page) => (page.content ?? []).find((customer) => customer.id === customerId) ?? null)
    );
  }

  private fallbackId(id: number | null | undefined): string {
    return id ? `#${id}` : '-';
  }
}
