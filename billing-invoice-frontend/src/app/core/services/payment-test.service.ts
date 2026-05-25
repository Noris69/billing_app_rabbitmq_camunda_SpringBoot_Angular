import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  InvoiceWorkflowRequest,
  InvoiceWorkflowResponse,
  PaymentPage
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
    const params = new HttpParams().set('customerId', customerId).set('size', 20);
    return this.http.get<PaymentPage>('/payment-api/payments/search', { params });
  }

  searchPayments(): Observable<PaymentPage> {
    const params = new HttpParams().set('size', 50);
    return this.http.get<PaymentPage>('/payment-api/payments/search', { params });
  }
}
