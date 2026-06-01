import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of, switchMap, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Payment } from 'src/app/core/models/payment.model';
import { PaymentTestService } from 'src/app/core/services/payment-test.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

@Component({
  selector: 'app-payment-detail',
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
  templateUrl: './payment-detail.component.html',
  styleUrls: ['./payment-detail.component.css']
})
export class PaymentDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly paymentService = inject(PaymentTestService);
  private readonly message = inject(NzMessageService);

  payment: Payment | null = null;
  attempts: Payment[] = [];
  loading = false;
  retrying = false;
  closing = false;
  errorMessage = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadPayment(id);
      } else {
        this.errorMessage = 'Transaction introuvable.';
      }
    });
  }

  loadPayment(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.paymentService
      .getPaymentById(id)
      .pipe(
        switchMap((payment) =>
          forkJoin({
            payment: of(payment),
            attempts: this.paymentService.getPaymentAttempts(payment.id).pipe(catchError(() => of([])))
          })
        ),
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: ({ payment, attempts }) => {
          this.payment = payment;
          this.attempts = attempts;
        },
        error: (error: unknown) => {
          this.payment = null;
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger la transaction payment.');
        }
      });
  }

  retryPayment(): void {
    if (!this.payment || !this.canRetry(this.payment)) {
      return;
    }

    this.retrying = true;
    this.errorMessage = '';

    this.paymentService
      .retryPayment(this.payment.id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.retrying = false;
        })
      )
      .subscribe({
        next: (payment) => {
          this.message.success('Nouvelle tentative de paiement en attente.');
          this.router.navigate(['/paiements', payment.id]);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de relancer ce paiement.');
          this.message.error(this.errorMessage);
        }
      });
  }

  markSuccess(): void {
    this.closePayment('success');
  }

  markFailed(): void {
    this.closePayment('failed');
  }

  canRetry(payment: Payment | null): boolean {
    return payment?.status === 'FAILED' || payment?.status === 'CANCELLED';
  }

  canClose(payment: Payment | null): boolean {
    return payment?.status === 'PENDING';
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

  private closePayment(result: 'success' | 'failed'): void {
    if (!this.payment || !this.canClose(this.payment)) {
      return;
    }

    this.closing = true;
    this.errorMessage = '';
    const request =
      result === 'success'
        ? this.paymentService.markPaymentSuccess(this.payment.id)
        : this.paymentService.markPaymentFailed(this.payment.id);

    request
      .pipe(
        timeout(15000),
        finalize(() => {
          this.closing = false;
        })
      )
      .subscribe({
        next: (payment) => {
          this.payment = payment;
          this.loadPayment(payment.id);
          this.message.success(result === 'success' ? 'Paiement marque reussi.' : 'Paiement marque echoue.');
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de cloturer ce paiement.');
          this.message.error(this.errorMessage);
        }
      });
  }
}
