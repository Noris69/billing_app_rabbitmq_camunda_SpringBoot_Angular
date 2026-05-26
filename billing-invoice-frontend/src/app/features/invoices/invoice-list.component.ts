import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
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

type SortOrder = 'ascend' | 'descend' | null;

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzEmptyModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzSelectModule,
    NzSpinModule,
    NzTableModule,
    NzTagModule
  ],
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.css']
})
export class InvoiceListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly invoiceService = inject(InvoiceService);
  private readonly customerService = inject(CustomerService);
  private readonly creancierService = inject(CreancierService);
  private readonly pointDeVenteService = inject(PointDeVenteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly statusOptions: InvoiceStatus[] = ['EN_ATTENTE', 'PAYEE', 'PAID', 'REJECTED', 'EN_RETARD', 'ANNULEE'];

  readonly searchForm = this.fb.group({
    reference: [''],
    status: [null as InvoiceStatus | null]
  });

  invoices: Invoice[] = [];
  loading = false;
  errorMessage = '';
  totalElements = 0;
  pageIndex = 1;
  pageSize = 10;
  sortBy = 'id';
  sortOrder: SortOrder = 'descend';
  customersById = new Map<number, Customer>();
  creanciersById = new Map<number, Creancier>();
  pointsDeVenteById = new Map<number, PointDeVente>();

  ngOnInit(): void {
    this.readTableStateFromUrl();
    this.search();
  }

  search(): void {
    this.syncTableStateToUrl();
    this.loading = true;
    this.errorMessage = '';
    const rawValue = this.searchForm.getRawValue();

    forkJoin({
      invoicesPage: this.invoiceService.searchInvoices({
        page: this.pageIndex - 1,
        size: this.pageSize,
        reference: rawValue.reference?.trim() || undefined,
        status: rawValue.status ?? undefined,
        sortBy: this.sortBy,
        sortDir: this.sortDir()
      }),
      customersPage: this.customerService.searchCustomers({ page: 0, size: 500 }).pipe(
        catchError(() => of({ content: [] as Customer[] }))
      ),
      creanciersPage: this.creancierService.searchCreanciers({ page: 0, size: 500 }).pipe(
        catchError(() => of({ content: [] as Creancier[] }))
      ),
      pointsDeVentePage: this.pointDeVenteService.searchPointDeVentes({ page: 0, size: 500 }).pipe(
        catchError(() => of({ content: [] as PointDeVente[] }))
      )
    })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: ({ invoicesPage, customersPage, creanciersPage, pointsDeVentePage }) => {
          this.invoices = invoicesPage.content ?? [];
          this.totalElements = invoicesPage.totalElements ?? this.invoices.length;
          this.customersById = this.toMap(customersPage.content ?? []);
          this.creanciersById = this.toMap(creanciersPage.content ?? []);
          this.pointsDeVenteById = this.toMap(pointsDeVentePage.content ?? []);
        },
        error: (error: unknown) => {
          this.invoices = [];
          this.totalElements = 0;
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les factures.');
        }
      });
  }

  submitSearch(): void {
    this.pageIndex = 1;
    this.search();
  }

  resetFilters(): void {
    this.searchForm.reset({
      reference: '',
      status: null
    });
    this.pageIndex = 1;
    this.search();
  }

  onPageIndexChange(pageIndex: number): void {
    this.pageIndex = pageIndex;
    this.search();
  }

  onPageSizeChange(pageSize: number): void {
    this.pageSize = pageSize;
    this.pageIndex = 1;
    this.search();
  }

  onSortChange(sortBy: string, sortOrder: string | null): void {
    this.applySort(sortBy, sortOrder);
    this.pageIndex = 1;
    this.search();
  }

  statusColor(status: InvoiceStatus | null | undefined): string {
    if (status === 'PAYEE' || status === 'PAID') {
      return 'green';
    }
    if (status === 'ANNULEE') {
      return 'red';
    }
    if (status === 'REJECTED') {
      return 'red';
    }
    if (status === 'EN_RETARD') {
      return 'orange';
    }
    return 'gold';
  }

  customerLabel(customerId: number | null | undefined): string {
    const customer = customerId ? this.customersById.get(customerId) : null;
    return customer ? `${customer.nom} ${customer.prenom}` : this.fallbackId(customerId);
  }

  creancierLabel(creancierId: number | null | undefined): string {
    const creancier = creancierId ? this.creanciersById.get(creancierId) : null;
    return creancier ? creancier.nom : this.fallbackId(creancierId);
  }

  pointDeVenteLabel(pointDeVenteId: number | null | undefined): string {
    const pointDeVente = pointDeVenteId ? this.pointsDeVenteById.get(pointDeVenteId) : null;
    return pointDeVente ? pointDeVente.nom : this.fallbackId(pointDeVenteId);
  }

  private toMap<T extends { id: number }>(items: T[]): Map<number, T> {
    return new Map(items.map((item) => [item.id, item]));
  }

  private fallbackId(id: number | null | undefined): string {
    return id ? `#${id}` : '-';
  }

  private sortDir(): 'asc' | 'desc' {
    return this.sortOrder === 'ascend' ? 'asc' : 'desc';
  }

  private toSortOrder(sortOrder: string | null): SortOrder {
    return sortOrder === 'ascend' || sortOrder === 'descend' ? sortOrder : null;
  }

  private applySort(sortBy: string, sortOrder: string | null): void {
    const nextSortOrder = this.toSortOrder(sortOrder);
    this.sortBy = nextSortOrder ? sortBy : 'id';
    this.sortOrder = nextSortOrder ?? 'descend';
  }

  private readTableStateFromUrl(): void {
    const params = this.route.snapshot.queryParamMap;
    this.pageIndex = this.positiveNumber(params.get('page'), this.pageIndex);
    this.pageSize = this.positiveNumber(params.get('size'), this.pageSize);
    this.sortBy = params.get('sortBy') || this.sortBy;
    this.sortOrder = this.toSortOrder(params.get('sortOrder')) ?? this.sortOrder;
  }

  private syncTableStateToUrl(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        page: this.pageIndex,
        size: this.pageSize,
        sortBy: this.sortBy,
        sortOrder: this.sortOrder
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
