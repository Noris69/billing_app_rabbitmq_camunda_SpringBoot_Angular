import { SpringPage } from './point-de-vente.model';

export type ModeReglement = 'ESPECES' | 'CARTE';

export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';

export interface InvoiceWorkflowRequest {
  reference: string;
  dateInvoice?: string | null;
  dateDue?: string | null;
  montantHt?: number | null;
  montantTva?: number | null;
  montantTtc: number;
  modeReglement?: ModeReglement | null;
  description?: string | null;
  customerId: number;
  creancierId: number;
  pointDeVenteId: number;
  paymentSuccess?: boolean;
}

export interface InvoiceWorkflowResponse {
  processInstanceId: string;
  invoiceId: number;
  businessKey: string;
}

export interface Payment {
  id: number;
  invoiceId?: number | null;
  invoiceReference?: string | null;
  customerId?: number | null;
  creancierId?: number | null;
  pointDeVenteId?: number | null;
  amount: number;
  currency: string;
  modeReglement?: ModeReglement | null;
  transactionReference?: string | null;
  status: PaymentStatus;
  failureReason?: string | null;
  createdDate?: string | null;
  updatedDate?: string | null;
}

export interface PaymentSearchParams {
  page?: number;
  size?: number;
  customerId?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export type PaymentPage = SpringPage<Payment>;
