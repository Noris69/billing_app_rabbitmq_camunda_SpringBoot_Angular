import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateCreancierPayload,
  Creancier,
  CreancierPage,
  CreancierSearchParams,
  UpdateCreancierPayload
} from '../models/creancier.model';

@Injectable({
  providedIn: 'root'
})
export class CreancierService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/creanciers';

  createCreancier(payload: CreateCreancierPayload): Observable<Creancier> {
    return this.http.post<Creancier>(this.apiUrl, payload);
  }

  updateCreancier(id: number, payload: UpdateCreancierPayload): Observable<Creancier> {
    return this.http.put<Creancier>(`${this.apiUrl}/${id}`, payload);
  }

  deleteCreancier(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCreancierById(id: number): Observable<Creancier> {
    return this.http.get<Creancier>(`${this.apiUrl}/get-by-id/${id}`);
  }

  searchCreanciers(params: CreancierSearchParams = {}): Observable<CreancierPage> {
    return this.http.get<CreancierPage>(`${this.apiUrl}/search`, {
      params: this.toHttpParams(params)
    });
  }

  private toHttpParams(params: CreancierSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === undefined || value === null || value === '') {
        return httpParams;
      }

      return httpParams.set(key, String(value));
    }, new HttpParams());
  }

}
