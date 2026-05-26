import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, defaultIfEmpty, filter, finalize, map, switchMap, take, tap, timer, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Creancier } from 'src/app/core/models/creancier.model';
import { Customer } from 'src/app/core/models/customer.model';
import { InvoiceWorkflowResponse, ModeReglement, Payment } from 'src/app/core/models/payment.model';
import { PointDeVente } from 'src/app/core/models/point-de-vente.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { CustomerService } from 'src/app/core/services/customer.service';
import { PaymentTestService } from 'src/app/core/services/payment-test.service';
import { PointDeVenteService } from 'src/app/core/services/point-de-vente.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

type SortOrder = 'ascend' | 'descend' | null;

@Component({
  selector: 'app-payment-test',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzInputNumberModule,
    NzSelectModule,
    NzSpinModule,
    NzTableModule,
    NzTagModule
  ],
  templateUrl: './payment-test.component.html',
  styleUrls: ['./payment-test.component.css']
})
export class PaymentTestComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly paymentTestService = inject(PaymentTestService);
  private readonly customerService = inject(CustomerService);
  private readonly creancierService = inject(CreancierService);
  private readonly pointDeVenteService = inject(PointDeVenteService);
  private readonly message = inject(NzMessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly modeOptions: ModeReglement[] = ['ESPECES', 'CARTE'];

  readonly form = this.fb.group({
    reference: [`INV-RMQ-${Date.now()}`, [Validators.required]],
    dateInvoice: [this.formatDate(new Date()), [Validators.required]],
    dateDue: [this.formatDate(this.addDays(new Date(), 30)), [Validators.required]],
    montantHt: [100, [Validators.required, Validators.min(0)]],
    montantTva: [{ value: 20, disabled: true }, [Validators.required, Validators.min(0)]],
    montantTtc: [{ value: 120, disabled: true }, [Validators.required, Validators.min(1)]],
    modeReglement: ['CARTE' as ModeReglement, [Validators.required]],
    description: ['Test RabbitMQ depuis Angular'],
    customerId: [null as number | null, [Validators.required]],
    creancierId: [null as number | null, [Validators.required]],
    pointDeVenteId: [null as number | null, [Validators.required]],
    paymentSuccess: [true]
  });

  customers: Customer[] = [];
  creanciers: Creancier[] = [];
  pointsDeVente: PointDeVente[] = [];
  payments: Payment[] = [];
  workflowResponse: InvoiceWorkflowResponse | null = null;
  loading = false;
  refreshing = false;
  loadingCustomers = false;
  loadingCreanciers = false;
  loadingPointsDeVente = false;
  errorMessage = '';
  successMessage = '';
  lastPaymentIdBeforeTest: number | null = null;
  paymentTotalElements = 0;
  paymentPageIndex = 1;
  paymentPageSize = 10;
  paymentSortBy = 'id';
  paymentSortOrder: SortOrder = 'descend';

  constructor() {
    this.form.controls.montantHt.valueChanges.subscribe((value) => {
      this.updateAmounts(Number(value ?? 0));
    });
  }

  ngOnInit(): void {
    this.readPaymentTableStateFromUrl();
    this.updateAmounts(Number(this.form.controls.montantHt.value ?? 0));
    this.searchCustomers();
    this.searchCreanciers();
    this.searchPointsDeVente();
  }

  startTest(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.workflowResponse = null;
    this.lastPaymentIdBeforeTest = this.latestPaymentId();

    const payload = this.form.getRawValue();
    const customerId = Number(payload.customerId);

    this.paymentTestService
      .startInvoicePaymentWorkflow({
        reference: payload.reference ?? `INV-RMQ-${Date.now()}`,
        dateInvoice: payload.dateInvoice,
        dateDue: payload.dateDue,
        montantHt: Number(payload.montantHt ?? 0),
        montantTva: Number(payload.montantTva ?? 0),
        montantTtc: Number(payload.montantTtc ?? 0),
        modeReglement: payload.modeReglement,
        description: payload.description,
        customerId,
        creancierId: Number(payload.creancierId ?? 0),
        pointDeVenteId: Number(payload.pointDeVenteId ?? 0),
        paymentSuccess: payload.paymentSuccess ?? true
      })
      .pipe(
        timeout(20000),
        switchMap((response) => {
          this.workflowResponse = response;
          return this.waitForPaymentRefresh();
        }),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (payments) => {
          this.payments = payments;
          this.paymentTotalElements = Math.max(this.paymentTotalElements, payments.length);
          if (this.hasNewPaymentAfterTest()) {
            this.successMessage = 'Facture envoyee et nouvelle transaction payment detectee.';
            this.message.success(this.successMessage);
          } else {
            this.errorMessage =
              'Facture envoyee, mais aucune nouvelle transaction payment detectee. Verifie le listener RabbitMQ billing-payment.';
            this.message.warning(this.errorMessage);
          }
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Le test paiement a echoue.');
          this.message.error(this.errorMessage);
        }
      });
  }

  refreshPayments(): void {
    this.syncPaymentTableStateToUrl();
    this.refreshing = true;
    this.errorMessage = '';

    this.paymentTestService
      .searchPayments({
        page: this.paymentPageIndex - 1,
        size: this.paymentPageSize,
        sortBy: this.paymentSortBy,
        sortDir: this.paymentSortDir()
      })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.refreshing = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.payments = page.content ?? [];
          this.paymentTotalElements = page.totalElements ?? this.payments.length;
          this.message.success('Transactions payment rechargees.');
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les paiements.');
          this.message.error(this.errorMessage);
        }
      });
  }

  onPaymentPageIndexChange(pageIndex: number): void {
    this.paymentPageIndex = pageIndex;
    this.refreshPayments();
  }

  onPaymentPageSizeChange(pageSize: number): void {
    this.paymentPageSize = pageSize;
    this.paymentPageIndex = 1;
    this.refreshPayments();
  }

  onPaymentSortChange(sortBy: string, sortOrder: string | null): void {
    this.paymentSortBy = sortBy;
    this.applyPaymentSort(sortBy, sortOrder);
    this.paymentPageIndex = 1;
    this.refreshPayments();
  }

  resetReference(): void {
    this.form.patchValue({ reference: `INV-RMQ-${Date.now()}` });
  }

  searchCustomers(query = ''): void {
    this.loadingCustomers = true;
    this.customerService
      .searchCustomers({ page: 0, size: 100, query: query.trim() || undefined })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loadingCustomers = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.customers = page.content ?? [];
          this.selectFirstValue('customerId', this.customers);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les customers.');
        }
      });
  }

  searchCreanciers(query = ''): void {
    this.loadingCreanciers = true;
    this.creancierService
      .searchCreanciers({ page: 0, size: 100, nom: query.trim() || undefined })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loadingCreanciers = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.creanciers = page.content ?? [];
          this.selectFirstValue('creancierId', this.creanciers);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les creanciers.');
        }
      });
  }

  searchPointsDeVente(query = ''): void {
    this.loadingPointsDeVente = true;
    this.pointDeVenteService
      .searchPointDeVentes({ page: 0, size: 100, nom: query.trim() || undefined })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loadingPointsDeVente = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.pointsDeVente = page.content ?? [];
          this.selectFirstValue('pointDeVenteId', this.pointsDeVente);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les points de vente.');
        }
      });
  }

  customerLabel(customer: Customer): string {
    const paymentType = customer.paymentType ? ` - ${customer.paymentType}` : '';
    return `${customer.nom} ${customer.prenom}${paymentType} (#${customer.id})`;
  }

  creancierLabel(creancier: Creancier): string {
    return `${creancier.nom} - ${creancier.typeCreancier} (#${creancier.id})`;
  }

  pointDeVenteLabel(pointDeVente: PointDeVente): string {
    return `${pointDeVente.nom} - ${pointDeVente.type} (#${pointDeVente.id})`;
  }

  customerName(customerId: number | null | undefined): string {
    const customer = this.customers.find((item) => item.id === customerId);
    return customer ? `${customer.nom} ${customer.prenom}` : this.fallbackId(customerId);
  }

  creancierName(creancierId: number | null | undefined): string {
    const creancier = this.creanciers.find((item) => item.id === creancierId);
    return creancier ? creancier.nom : this.fallbackId(creancierId);
  }

  pointDeVenteName(pointDeVenteId: number | null | undefined): string {
    const pointDeVente = this.pointsDeVente.find((item) => item.id === pointDeVenteId);
    return pointDeVente ? pointDeVente.nom : this.fallbackId(pointDeVenteId);
  }

  statusColor(status: string | null | undefined): string {
    if (status === 'SUCCESS') {
      return 'green';
    }
    if (status === 'FAILED' || status === 'CANCELLED') {
      return 'red';
    }
    return 'gold';
  }

  private updateAmounts(montantHt: number): void {
    const montantTva = this.roundAmount(montantHt * 0.2);
    const montantTtc = this.roundAmount(montantHt + montantTva);

    this.form.patchValue(
      {
        montantTva,
        montantTtc
      },
      { emitEvent: false }
    );
  }

  private roundAmount(value: number): number {
    return Math.round(value * 100) / 100;
  }

  private fallbackId(id: number | null | undefined): string {
    return id ? `#${id}` : '-';
  }

  private latestPaymentId(): number | null {
    if (!this.payments.length) {
      return null;
    }

    return Math.max(...this.payments.map((payment) => payment.id));
  }

  private waitForPaymentRefresh(): Observable<Payment[]> {
    return timer(1000, 1000).pipe(
      take(10),
      switchMap(() =>
        this.paymentTestService.searchPayments({
          page: 0,
          size: Math.max(this.paymentPageSize, 10),
          sortBy: this.paymentSortBy,
          sortDir: this.paymentSortDir()
        })
      ),
      map((page) => page.content ?? []),
      tap((payments) => {
        this.payments = payments;
        this.paymentPageIndex = 1;
        this.paymentTotalElements = Math.max(this.paymentTotalElements, payments.length);
      }),
      filter((payments) => this.containsNewPayment(payments)),
      take(1),
      defaultIfEmpty(this.payments)
    );
  }

  private hasNewPaymentAfterTest(): boolean {
    return this.containsNewPayment(this.payments);
  }

  private containsNewPayment(payments: Payment[]): boolean {
    if (!payments.length) {
      return false;
    }

    const latestId = Math.max(...payments.map((payment) => payment.id));
    if (!latestId) {
      return false;
    }

    return this.lastPaymentIdBeforeTest === null || latestId > this.lastPaymentIdBeforeTest;
  }

  private selectFirstValue(controlName: 'customerId' | 'creancierId' | 'pointDeVenteId', items: Array<{ id: number }>): void {
    const control = this.form.controls[controlName];
    if (!control.value && items.length) {
      control.setValue(items[0].id);
    }
  }

  private addDays(date: Date, days: number): Date {
    const next = new Date(date);
    next.setDate(next.getDate() + days);
    return next;
  }

  private formatDate(date: Date): string {
    return date.toISOString().slice(0, 10);
  }

  private paymentSortDir(): 'asc' | 'desc' {
    return this.paymentSortOrder === 'ascend' ? 'asc' : 'desc';
  }

  private toSortOrder(sortOrder: string | null): SortOrder {
    return sortOrder === 'ascend' || sortOrder === 'descend' ? sortOrder : null;
  }

  private applyPaymentSort(sortBy: string, sortOrder: string | null): void {
    const nextSortOrder = this.toSortOrder(sortOrder);
    this.paymentSortBy = nextSortOrder ? sortBy : 'id';
    this.paymentSortOrder = nextSortOrder ?? 'descend';
  }

  private readPaymentTableStateFromUrl(): void {
    const params = this.route.snapshot.queryParamMap;
    this.paymentPageIndex = this.positiveNumber(params.get('paymentPage'), this.paymentPageIndex);
    this.paymentPageSize = this.positiveNumber(params.get('paymentSize'), this.paymentPageSize);
    this.paymentSortBy = params.get('paymentSortBy') || this.paymentSortBy;
    this.paymentSortOrder = this.toSortOrder(params.get('paymentSortOrder')) ?? this.paymentSortOrder;
  }

  private syncPaymentTableStateToUrl(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        paymentPage: this.paymentPageIndex,
        paymentSize: this.paymentPageSize,
        paymentSortBy: this.paymentSortBy,
        paymentSortOrder: this.paymentSortOrder
      },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  private positiveNumber(value: string | null, fallback: number): number {
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
  }
}
