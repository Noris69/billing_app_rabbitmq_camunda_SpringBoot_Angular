import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
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
import { Invoice, InvoiceStatus } from 'src/app/core/models/invoice.model';
import { InvoiceService } from 'src/app/core/services/invoice.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

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

  readonly statusOptions: InvoiceStatus[] = ['EN_ATTENTE', 'PAYEE', 'PAID', 'REJECTED', 'EN_RETARD', 'ANNULEE'];

  readonly searchForm = this.fb.group({
    reference: [''],
    status: [null as InvoiceStatus | null]
  });

  invoices: Invoice[] = [];
  loading = false;
  errorMessage = '';
  totalElements = 0;

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.loading = true;
    this.errorMessage = '';
    const rawValue = this.searchForm.getRawValue();

    this.invoiceService
      .searchInvoices({
        page: 0,
        size: 50,
        reference: rawValue.reference?.trim() || undefined,
        status: rawValue.status ?? undefined
      })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.invoices = page.content ?? [];
          this.totalElements = page.totalElements ?? this.invoices.length;
        },
        error: (error: unknown) => {
          this.invoices = [];
          this.totalElements = 0;
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les factures.');
        }
      });
  }

  resetFilters(): void {
    this.searchForm.reset({
      reference: '',
      status: null
    });
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
}
