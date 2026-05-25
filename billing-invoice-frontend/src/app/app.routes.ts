import { Routes } from '@angular/router';
import { CreancierListComponent } from './features/creancier/creancier-list.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'creanciers',
  },
  {
    path: 'creanciers',
    component: CreancierListComponent,
    pathMatch: 'full',
  },
  {
    path: 'creanciers/new',
    loadComponent: () =>
      import('./features/creancier/creancier-form.component').then(
        (module) => module.CreancierFormComponent
      ),
  },
  {
    path: 'creanciers/:id/edit',
    loadComponent: () =>
      import('./features/creancier/creancier-form.component').then(
        (module) => module.CreancierFormComponent
      ),
  },
  {
    path: 'points-de-vente',
    loadComponent: () =>
      import('./features/points-de-vente-list/points-de-vente-list.component').then(
        (module) => module.PointsDeVenteListComponent
      ),
  },
  {
    path: 'points-de-vente/new',
    loadComponent: () =>
      import('./features/point-de-vente-form/point-de-vente-form.component').then(
        (module) => module.PointDeVenteFormComponent
      ),
  },
  {
    path: 'points-de-vente/:id/edit',
    loadComponent: () =>
      import('./features/point-de-vente-form/point-de-vente-form.component').then(
        (module) => module.PointDeVenteFormComponent
      ),
  },
  {
    path: 'factures',
    loadComponent: () =>
      import('./features/invoices/invoice-list.component').then(
        (module) => module.InvoiceListComponent
      ),
  },
  {
    path: 'test-paiement',
    loadComponent: () =>
      import('./features/payment-test/payment-test.component').then(
        (module) => module.PaymentTestComponent
      ),
  },
  {
    path: '**',
    redirectTo: 'creanciers',
  },
];
