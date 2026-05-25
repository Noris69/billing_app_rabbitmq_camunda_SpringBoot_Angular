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
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import {
  POINT_DE_VENTE_TYPES,
  PointDeVente,
  PointDeVenteSearchParams,
  PointDeVenteType
} from '../../core/models/point-de-vente.model';
import { PointDeVenteService } from '../../core/services/point-de-vente.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';

@Component({
  selector: 'app-points-de-vente-list',
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
    NzPopconfirmModule,
    NzSelectModule,
    NzSpinModule,
    NzTableModule,
    NzTagModule
  ],
  templateUrl: './points-de-vente-list.component.html',
  styleUrl: './points-de-vente-list.component.css'
})
export class PointsDeVenteListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly pointDeVenteService = inject(PointDeVenteService);

  readonly pointDeVenteTypes = POINT_DE_VENTE_TYPES;

  readonly searchForm = this.fb.group({
    nom: [''],
    type_point_de_vente: [null as PointDeVenteType | null],
    adresse: [''],
    telephone: ['']
  });

  pointsDeVente: PointDeVente[] = [];
  loading = false;
  exportingPdf = false;
  deletingId: number | null = null;
  errorMessage = '';
  successMessage = '';
  totalElements = 0;

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.pointDeVenteService
      .searchPointDeVentes(this.buildSearchParams())
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.pointsDeVente = page.content ?? [];
          this.totalElements = page.totalElements ?? this.pointsDeVente.length;
        },
        error: (error: unknown) => {
          this.pointsDeVente = [];
          this.totalElements = 0;
          this.errorMessage = extractApiErrorMessage(
            error,
            'Impossible de charger les points de vente.'
          );
        }
      });
  }

  resetFilters(): void {
    this.searchForm.reset({
      nom: '',
      type_point_de_vente: null,
      adresse: '',
      telephone: ''
    });
    this.search();
  }

  deletePointDeVente(pointDeVente: PointDeVente): void {
    this.deletingId = pointDeVente.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.pointDeVenteService
      .deletePointDeVente(pointDeVente.id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.deletingId = null;
        })
      )
      .subscribe({
        next: () => {
          this.successMessage = `Le point de vente ${pointDeVente.nom} a ete supprime.`;
          this.search();
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(
            error,
            'La suppression du point de vente a echoue.'
          );
        }
      });
  }

  exportPdf(): void {
    this.exportingPdf = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.pointDeVenteService
      .exportPdf(this.buildSearchParams())
      .pipe(
        timeout(15000),
        finalize(() => {
          this.exportingPdf = false;
        })
      )
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = 'points-de-vente.pdf';
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(
            error,
            "L'export PDF des points de vente a echoue."
          );
        }
      });
  }

  getSecondaryCode(pointDeVente: PointDeVente): string {
    return pointDeVente.type === 'AGENCE'
      ? pointDeVente.codeAgence ?? '-'
      : pointDeVente.codeDistributeur ?? '-';
  }

  getResponsibleLabel(pointDeVente: PointDeVente): string {
    return pointDeVente.type === 'AGENCE'
      ? pointDeVente.responsable ?? '-'
      : pointDeVente.nomCommercial ?? '-';
  }

  private buildSearchParams(): PointDeVenteSearchParams {
    const rawValue = this.searchForm.getRawValue();

    return {
      page: 0,
      nom: rawValue.nom?.trim() || undefined,
      type_point_de_vente: rawValue.type_point_de_vente ?? undefined,
      adresse: rawValue.adresse?.trim() || undefined,
      telephone: rawValue.telephone?.trim() || undefined
    };
  }
}
