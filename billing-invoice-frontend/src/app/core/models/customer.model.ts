import { SpringPage } from './point-de-vente.model';

export interface Customer {
  id: number;
  nom: string;
  prenom: string;
  cin?: string | null;
  paymentType?: string | null;
  email?: string | null;
  telephone?: string | null;
  adresse?: string | null;
  ville?: string | null;
  createdDate?: string | number | Date | null;
  updatedDate?: string | number | Date | null;
}

export interface CustomerSearchParams {
  page?: number;
  size?: number;
  query?: string;
  nom?: string;
  prenom?: string;
  cin?: string;
  email?: string;
  telephone?: string;
  ville?: string;
}

export type CustomerPage = SpringPage<Customer>;
