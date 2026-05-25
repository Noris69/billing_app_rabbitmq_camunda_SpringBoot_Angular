import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import {
  CreatePointDeVentePayload,
  POINT_DE_VENTE_TYPES,
  PointDeVenteType
} from '../../core/models/point-de-vente.model';
import { PointDeVenteService } from '../../core/services/point-de-vente.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';

@Component({
  selector: 'app-point-de-vente-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzFormModule,
    NzGridModule,
    NzInputModule,
    NzInputNumberModule,
    NzRadioModule,
    NzSpinModule
  ],
  templateUrl: './point-de-vente-form.component.html',
  styleUrl: './point-de-vente-form.component.css'
})
export class PointDeVenteFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly pointDeVenteService = inject(PointDeVenteService);

  readonly pointDeVenteTypes = POINT_DE_VENTE_TYPES;

  readonly pointDeVenteForm = this.fb.group({
    type: ['AGENCE' as PointDeVenteType, Validators.required],
    nom: ['', Validators.required],
    adresse: ['', Validators.required],
    telephone: ['', Validators.required],
    codeAgence: [''],
    responsable: [''],
    region: [''],
    typeAgence: [''],
    codeDistributeur: [''],
    zoneDistribution: [''],
    nomCommercial: [''],
    commission: [null as number | null, [Validators.min(0)]]
  });

  loading = false;
  submitting = false;
  errorMessage = '';
  pointDeVenteId: number | null = null;

  get isEditMode(): boolean {
    return this.pointDeVenteId !== null;
  }

  get selectedType(): PointDeVenteType {
    return this.pointDeVenteForm.controls.type.value ?? 'AGENCE';
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.pointDeVenteId = idParam ? Number(idParam) : null;

    this.applyTypeValidators(this.selectedType);
    this.pointDeVenteForm.controls.type.valueChanges.subscribe((type) => {
      this.applyTypeValidators(type ?? 'AGENCE');
    });

    if (this.pointDeVenteId !== null) {
      this.loadPointDeVente(this.pointDeVenteId);
    }
  }

  submit(): void {
    this.errorMessage = '';
    this.applyTypeValidators(this.selectedType);

    if (this.pointDeVenteForm.invalid) {
      this.pointDeVenteForm.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.submitting = true;

    const request =
      this.pointDeVenteId === null
        ? this.pointDeVenteService.createPointDeVente(payload)
        : this.pointDeVenteService.updatePointDeVente(this.pointDeVenteId, payload);

    request
      .pipe(
        timeout(15000),
        finalize(() => {
          this.submitting = false;
        })
      )
      .subscribe({
        next: () => {
          void this.router.navigate(['/points-de-vente']);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(
            error,
            "L'enregistrement du point de vente a echoue."
          );
        }
      });
  }

  private loadPointDeVente(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.pointDeVenteService
      .getPointDeVenteById(id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (pointDeVente) => {
          this.pointDeVenteForm.patchValue(pointDeVente);
          this.applyTypeValidators(pointDeVente.type);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(
            error,
            'Impossible de charger ce point de vente.'
          );
        }
      });
  }

  private buildPayload(): CreatePointDeVentePayload {
    const rawValue = this.pointDeVenteForm.getRawValue();
    const commonPayload = {
      type: rawValue.type ?? 'AGENCE',
      nom: rawValue.nom?.trim() ?? '',
      adresse: rawValue.adresse?.trim() ?? '',
      telephone: rawValue.telephone?.trim() ?? ''
    };

    if (commonPayload.type === 'AGENCE') {
      return {
        ...commonPayload,
        codeAgence: rawValue.codeAgence?.trim() || null,
        responsable: rawValue.responsable?.trim() || null,
        region: rawValue.region?.trim() || null,
        typeAgence: rawValue.typeAgence?.trim() || null,
        codeDistributeur: null,
        zoneDistribution: null,
        nomCommercial: null,
        commission: null
      };
    }

    return {
      ...commonPayload,
      codeAgence: null,
      responsable: null,
      region: null,
      typeAgence: null,
      codeDistributeur: rawValue.codeDistributeur?.trim() || null,
      zoneDistribution: rawValue.zoneDistribution?.trim() || null,
      nomCommercial: rawValue.nomCommercial?.trim() || null,
      commission: Number(rawValue.commission ?? 0)
    };
  }

  private applyTypeValidators(type: PointDeVenteType): void {
    const agenceFields = ['codeAgence', 'responsable', 'region', 'typeAgence'] as const;
    const distributeurFields = ['codeDistributeur', 'zoneDistribution', 'nomCommercial'] as const;

    for (const field of agenceFields) {
      this.pointDeVenteForm.controls[field].setValidators(
        type === 'AGENCE' ? Validators.required : null
      );
      this.pointDeVenteForm.controls[field].updateValueAndValidity({ emitEvent: false });
    }

    for (const field of distributeurFields) {
      this.pointDeVenteForm.controls[field].setValidators(
        type === 'DISTRIBUTEUR' ? Validators.required : null
      );
      this.pointDeVenteForm.controls[field].updateValueAndValidity({ emitEvent: false });
    }

    this.pointDeVenteForm.controls.commission.setValidators(
      type === 'DISTRIBUTEUR' ? [Validators.required, Validators.min(0)] : [Validators.min(0)]
    );
    this.pointDeVenteForm.controls.commission.updateValueAndValidity({ emitEvent: false });
  }
}
