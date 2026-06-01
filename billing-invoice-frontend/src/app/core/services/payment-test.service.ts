import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  InvoiceWorkflowRequest,
  InvoiceWorkflowResponse,
  CreatePaymentPayload,
  Payment,
  PaymentDashboard,
  PaymentPage,
  PaymentSearchParams
} from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentTestService {
  private readonly http = inject(HttpClient);

  startInvoicePaymentWorkflow(payload: InvoiceWorkflowRequest): Observable<InvoiceWorkflowResponse> {
    return this.http.post<InvoiceWorkflowResponse>('/api/workflows/invoice-payment/start', payload);
  }

  searchPaymentsByCustomer(customerId: number): Observable<PaymentPage> {
    return this.searchPayments({ customerId, page: 0, size: 20 });
  }

  searchPayments(params: PaymentSearchParams = {}): Observable<PaymentPage> {
    return this.http.get<PaymentPage>('/payment-api/payments/search', {
      params: this.toHttpParams(params)
    });
  }

  createPayment(payload: CreatePaymentPayload): Observable<Payment> {
    return this.http.post<Payment>('/payment-api/payments', payload);
  }

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`/payment-api/payments/${id}`);
  }

  retryPayment(id: number): Observable<Payment> {
    return this.http.post<Payment>(`/payment-api/payments/${id}/retry`, {});
  }

  markPaymentSuccess(id: number): Observable<Payment> {
    return this.http.post<Payment>(`/payment-api/payments/${id}/mark-success`, {});
  }

  markPaymentFailed(id: number): Observable<Payment> {
    return this.http.post<Payment>(`/payment-api/payments/${id}/mark-failed`, {});
  }

  getPaymentAttempts(id: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`/payment-api/payments/${id}/attempts`);
  }

  getDashboard(): Observable<PaymentDashboard> {
    return this.http.get<PaymentDashboard>('/payment-api/payments/dashboard');
  }

  private toHttpParams(params: PaymentSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === undefined || value === null || value === '') {
        return httpParams;
      }

      return httpParams.set(key, String(value));
    }, new HttpParams());
  }
}
