import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Invoice, InvoiceStatus } from 'src/app/core/models/invoice.model';
import { InvoiceService } from 'src/app/core/services/invoice.service';
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

  invoice: Invoice | null = null;
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
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (invoice) => {
          this.invoice = invoice;
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
}
