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
  attemptNumber?: number | null;
  parentPaymentId?: number | null;
  createdDate?: string | null;
  updatedDate?: string | null;
}

export interface CreatePaymentPayload {
  invoiceId: number;
  invoiceReference?: string | null;
  customerId?: number | null;
  creancierId?: number | null;
  pointDeVenteId?: number | null;
  amount: number;
  currency?: string | null;
  modeReglement?: ModeReglement | null;
  description?: string | null;
  paymentSuccess?: boolean | null;
  status?: PaymentStatus | null;
}

export interface PaymentSearchParams {
  page?: number;
  size?: number;
  customerId?: number;
  invoiceId?: number;
  invoiceReference?: string;
  creancierId?: number;
  pointDeVenteId?: number;
  status?: PaymentStatus;
  operationType?: ModeReglement;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface PaymentDashboard {
  totalTransactions: number;
  successfulTransactions: number;
  failedTransactions: number;
  pendingTransactions: number;
  cardTransactions: number;
  cashTransactions: number;
  totalCollected: number;
}

export type PaymentPage = SpringPage<Payment>;
