import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, map, of, switchMap, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Creancier } from 'src/app/core/models/creancier.model';
import { Customer } from 'src/app/core/models/customer.model';
import { Invoice, InvoiceStatus } from 'src/app/core/models/invoice.model';
import { Payment } from 'src/app/core/models/payment.model';
import { PointDeVente } from 'src/app/core/models/point-de-vente.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { CustomerService } from 'src/app/core/services/customer.service';
import { InvoiceService } from 'src/app/core/services/invoice.service';
import { PaymentTestService } from 'src/app/core/services/payment-test.service';
import { PointDeVenteService } from 'src/app/core/services/point-de-vente.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

interface TimelineItem {
  label: string;
  value: string;
  state: 'done' | 'pending' | 'failed';
}

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzPopconfirmModule,
    NzSpinModule,
    NzTableModule,
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
  private readonly paymentService = inject(PaymentTestService);
  private readonly message = inject(NzMessageService);

  invoice: Invoice | null = null;
  customer: Customer | null = null;
  creancier: Creancier | null = null;
  pointDeVente: PointDeVente | null = null;
  payments: Payment[] = [];
  timelineItems: TimelineItem[] = [];
  loading = false;
  requestingPayment = false;
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
              : of(null),
            payments: this.paymentService
              .searchPayments({ page: 0, size: 20, invoiceId: invoice.id, sortBy: 'createdDate', sortDir: 'desc' })
              .pipe(
                map((page) => page.content ?? []),
                catchError(() => of([]))
              )
          })
        ),
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: ({ invoice, customer, creancier, pointDeVente, payments }) => {
          this.invoice = invoice;
          this.customer = customer;
          this.creancier = creancier;
          this.pointDeVente = pointDeVente;
          this.payments = payments;
          this.timelineItems = this.buildTimeline(invoice);
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

  isRejected(status: InvoiceStatus | null | undefined): boolean {
    return status === 'REJECTED' || status === 'ANNULEE';
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

  paymentStatusColor(status: string | null | undefined): string {
    if (status === 'SUCCESS') {
      return 'green';
    }
    if (status === 'FAILED' || status === 'CANCELLED') {
      return 'red';
    }
    return 'gold';
  }

  canRequestPayment(): boolean {
    return (
      this.invoice?.status === 'EN_ATTENTE' &&
      !this.payments.some((payment) => payment.status === 'PENDING')
    );
  }

  requestPendingPayment(): void {
    if (!this.invoice || !this.canRequestPayment()) {
      return;
    }

    this.requestingPayment = true;
    this.errorMessage = '';

    this.paymentService
      .createPayment({
        invoiceId: this.invoice.id,
        invoiceReference: this.invoice.reference,
        customerId: this.invoice.customerId,
        creancierId: this.invoice.creancierId,
        pointDeVenteId: this.invoice.pointDeVenteId,
        amount: Number(this.invoice.montantTtc ?? 0),
        currency: 'MAD',
        modeReglement: this.invoice.modeReglement,
        description: `Paiement demande pour la facture ${this.invoice.reference}`,
        status: 'PENDING'
      })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.requestingPayment = false;
        })
      )
      .subscribe({
        next: (payment) => {
          this.payments = [payment, ...this.payments];
          this.timelineItems = this.buildTimeline(this.invoice!);
          this.message.success('Paiement en attente cree.');
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de demander le paiement.');
          this.message.error(this.errorMessage);
        }
      });
  }

  private findCustomerById(customerId: number | null | undefined) {
    if (!customerId) {
      return of(null);
    }

    return this.customerService.searchCustomers({ page: 0, size: 500 }).pipe(
      map((page) => (page.content ?? []).find((customer) => customer.id === customerId) ?? null)
    );
  }

  private buildTimeline(invoice: Invoice): TimelineItem[] {
    const finalStatus = invoice.status ?? 'EN_ATTENTE';
    const paymentClosed = this.isPaid(finalStatus) || this.isRejected(finalStatus);
    const pendingPayment = this.payments.some((payment) => payment.status === 'PENDING');
    const statusDate = this.formatDateTime(invoice.updatedDate || invoice.createdDate);

    return [
      {
        label: 'Facture creee',
        value: this.formatDateTime(invoice.createdDate),
        state: 'done'
      },
      {
        label: 'Facture validee',
        value: finalStatus === 'EN_ATTENTE' ? 'En attente' : statusDate,
        state: finalStatus === 'EN_ATTENTE' ? 'pending' : 'done'
      },
      {
        label: 'Paiement demande',
        value: paymentClosed || pendingPayment ? invoice.modeReglement || '-' : 'Non demande',
        state: paymentClosed || pendingPayment ? 'done' : 'pending'
      },
      {
        label: this.isRejected(finalStatus) ? 'Paiement echoue' : 'Paiement reussi',
        value: paymentClosed ? finalStatus : 'En attente',
        state: this.isRejected(finalStatus) ? 'failed' : this.isPaid(finalStatus) ? 'done' : 'pending'
      }
    ];
  }

  private formatDateTime(value: string | number | Date | null | undefined): string {
    if (!value) {
      return '-';
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('fr-FR');
  }

  private fallbackId(id: number | null | undefined): string {
    return id ? `#${id}` : '-';
  }
}
