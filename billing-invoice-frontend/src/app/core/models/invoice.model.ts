import { ModeReglement } from './payment.model';
import { SpringPage } from './point-de-vente.model';

export type InvoiceStatus = 'EN_ATTENTE' | 'PAYEE' | 'PAID' | 'REJECTED' | 'EN_RETARD' | 'ANNULEE';

export interface Invoice {
  id: number;
  reference: string;
  dateInvoice?: string | null;
  dateDue?: string | null;
  montantHt?: number | null;
  montantTva?: number | null;
  montantTtc?: number | null;
  status?: InvoiceStatus | null;
  modeReglement?: ModeReglement | null;
  description?: string | null;
  customerId?: number | null;
  creancierId?: number | null;
  pointDeVenteId?: number | null;
  createdDate?: string | number | Date | null;
  updatedDate?: string | number | Date | null;
}

export interface InvoiceSearchParams {
  page?: number;
  size?: number;
  reference?: string;
  status?: InvoiceStatus;
  customerId?: number;
  creancierId?: number;
  pointDeVenteId?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export type InvoicePage = SpringPage<Invoice>;
