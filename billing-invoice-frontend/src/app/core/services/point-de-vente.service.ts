import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreatePointDeVentePayload,
  PointDeVente,
  PointDeVenteSearchParams,
  SpringPage,
  UpdatePointDeVentePayload
} from '../models/point-de-vente.model';

@Injectable({
  providedIn: 'root'
})
export class PointDeVenteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/points-de-vente';

  createPointDeVente(payload: CreatePointDeVentePayload): Observable<PointDeVente> {
    return this.http.post<PointDeVente>(this.apiUrl, payload);
  }

  updatePointDeVente(id: number, payload: UpdatePointDeVentePayload): Observable<PointDeVente> {
    return this.http.put<PointDeVente>(`${this.apiUrl}/${id}`, payload);
  }

  deletePointDeVente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getPointDeVenteById(id: number): Observable<PointDeVente> {
    return this.http.get<PointDeVente>(`${this.apiUrl}/get-by-id/${id}`);
  }

  searchPointDeVentes(
    params: PointDeVenteSearchParams = {}
  ): Observable<SpringPage<PointDeVente>> {
    return this.http.get<SpringPage<PointDeVente>>(`${this.apiUrl}/search`, {
      params: this.toHttpParams(params)
    });
  }

  exportPdf(params: PointDeVenteSearchParams = {}): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/pdf`, {
      params: this.toHttpParams(params),
      responseType: 'blob'
    });
  }

  private toHttpParams(params: PointDeVenteSearchParams): HttpParams {
    return Object.entries(params).reduce((httpParams, [key, value]) => {
      if (value === undefined || value === null || value === '') {
        return httpParams;
      }

      return httpParams.set(key, String(value));
    }, new HttpParams());
  }
}
