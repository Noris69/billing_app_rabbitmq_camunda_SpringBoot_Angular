import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CustomerPage, CustomerSearchParams } from '../models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/customer-api/customers';

  searchCustomers(params: CustomerSearchParams = {}): Observable<CustomerPage> {
    return this.http.get<CustomerPage>(`${this.apiUrl}/search`, {
      params: this.toHttpParams(params)
    });
  }

  private toHttpParams(params: CustomerSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === undefined || value === null || value === '') {
        return httpParams;
      }

      return httpParams.set(key, String(value));
    }, new HttpParams());
  }
}
