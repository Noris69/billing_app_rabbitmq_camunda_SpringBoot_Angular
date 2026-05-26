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
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import {
  Creancier,
  CreancierSearchParams,
  TYPE_CREANCIER_OPTIONS,
  TypeCreancier
} from 'src/app/core/models/creancier.model';
import { CreancierService } from 'src/app/core/services/creancier.service';
import { extractApiErrorMessage } from 'src/app/core/utils/api-error.util';

@Component({
  selector: 'app-creancier-list',
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
  templateUrl: './creancier-list.component.html',
  styleUrls: ['./creancier-list.component.css']
})
export class CreancierListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly creancierService = inject(CreancierService);
  private readonly message = inject(NzMessageService);

  readonly typeOptions = TYPE_CREANCIER_OPTIONS;

  readonly searchForm = this.fb.group({
    nom: [''],
    typeCreancier: [null as TypeCreancier | null],
    ice: [''],
    banque: [''],
    telephone: ['']
  });

  creanciers: Creancier[] = [];
  loading = false;
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

    this.creancierService
      .searchCreanciers(this.buildSearchParams())
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (page) => {
          this.creanciers = page.content ?? [];
          this.totalElements = page.totalElements ?? this.creanciers.length;
        },
        error: (error: unknown) => {
          this.creanciers = [];
          this.totalElements = 0;
          this.errorMessage = extractApiErrorMessage(error, 'Impossible de charger les creanciers.');
        }
      });
  }

  resetFilters(): void {
    this.searchForm.reset({
      nom: '',
      typeCreancier: null,
      ice: '',
      banque: '',
      telephone: ''
    });
    this.search();
  }

  deleteCreancier(creancier: Creancier): void {
    this.deletingId = creancier.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.creancierService
      .deleteCreancier(creancier.id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.deletingId = null;
        })
      )
      .subscribe({
        next: () => {
          this.successMessage = `Le creancier ${creancier.nom} a ete supprime.`;
          this.message.success(this.successMessage);
          this.search();
        },
        error: (error: unknown) => {
          this.errorMessage = extractApiErrorMessage(error, 'La suppression du creancier a echoue.');
          this.message.error(this.errorMessage);
        }
      });
  }

  private buildSearchParams(): CreancierSearchParams {
    const rawValue = this.searchForm.getRawValue();

    return {
      page: 0,
      nom: rawValue.nom?.trim() || undefined,
      typeCreancier: rawValue.typeCreancier ?? undefined,
      ice: rawValue.ice?.trim() || undefined,
      banque: rawValue.banque?.trim() || undefined,
      telephone: rawValue.telephone?.trim() || undefined
    };
  }
}
