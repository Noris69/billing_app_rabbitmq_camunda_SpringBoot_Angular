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
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import {
  CreateCreancierPayload,
  TYPE_CREANCIER_OPTIONS,
  TypeCreancier
} from 'src/app/core/models/creancier.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

@Component({
  selector: 'app-creancier-form',
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
    NzSelectModule,
    NzSpinModule
  ],
  templateUrl: './creancier-form.component.html',
  styleUrl: './creancier-form.component.css'
})
export class CreancierFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly creancierService = inject(CreancierService);

  readonly typeOptions = TYPE_CREANCIER_OPTIONS;

  readonly creancierForm = this.fb.group({
    nom: ['', Validators.required],
    typeCreancier: ['AUTRE' as TypeCreancier, Validators.required],
    ice: [''],
    rc: [''],
    rib: [''],
    banque: [''],
    email: ['', Validators.email],
    telephone: [''],
    adresse: ['']
  });

  loading = false;
  submitting = false;
  errorMessage = '';
  creancierId: number | null = null;

  get isEditMode(): boolean {
    return this.creancierId !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.creancierId = idParam ? Number(idParam) : null;

    if (this.creancierId !== null) {
      this.loadCreancier(this.creancierId);
    }
  }

  submit(): void {
    this.errorMessage = '';

    if (this.creancierForm.invalid) {
      this.creancierForm.markAllAsTouched();
      return;
    }

    const payload = this.buildPayload();
    this.submitting = true;

    const request =
      this.creancierId === null
        ? this.creancierService.createCreancier(payload)
        : this.creancierService.updateCreancier(this.creancierId, payload);

    request
      .pipe(
        timeout(15000),
        finalize(() => {
          this.submitting = false;
        })
      )
      .subscribe({
        next: () => {
          void this.router.navigate(['/creanciers']);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, "L'enregistrement du creancier a echoue.");
        }
      });
  }

  private loadCreancier(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.creancierService
      .getCreancierById(id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (creancier) => {
          this.creancierForm.patchValue(creancier);
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger ce creancier.');
        }
      });
  }

  private buildPayload(): CreateCreancierPayload {
    const rawValue = this.creancierForm.getRawValue();

    return {
      nom: rawValue.nom?.trim() ?? '',
      typeCreancier: rawValue.typeCreancier ?? 'AUTRE',
      ice: rawValue.ice?.trim() || null,
      rc: rawValue.rc?.trim() || null,
      rib: rawValue.rib?.trim() || null,
      banque: rawValue.banque?.trim() || null,
      email: rawValue.email?.trim() || null,
      telephone: rawValue.telephone?.trim() || null,
      adresse: rawValue.adresse?.trim() || null
    };
  }
}
