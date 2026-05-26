import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Invoice, InvoicePage, InvoiceSearchParams } from '../models/invoice.model';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/invoices';

  searchInvoices(params: InvoiceSearchParams = {}): Observable<InvoicePage> {
    return this.http.get<InvoicePage>(`${this.apiUrl}/search`, {
      params: this.toHttpParams(params)
    });
  }

  getInvoiceById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`);
  }

  private toHttpParams(params: InvoiceSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === undefined || value === null || value === '') {
        return httpParams;
      }

      return httpParams.set(key, String(value));
    }, new HttpParams());
  }
}
