import { SpringPage } from './point-de-vente.model';

export type TypeCreancier = 'IAM' | 'BANQUE' | 'ONEE' | 'CLINIQUE' | 'AUTRE';

export interface Creancier {
  id: number;
  nom: string;
  typeCreancier: TypeCreancier;
  ice?: string | null;
  rc?: string | null;
  rib?: string | null;
  banque?: string | null;
  email?: string | null;
  telephone?: string | null;
  adresse?: string | null;
  createdDate?: string | number | Date | null;
  updatedDate?: string | number | Date | null;
}

export type CreateCreancierPayload = Omit<Creancier, 'id' | 'createdDate' | 'updatedDate'>;

export type UpdateCreancierPayload = CreateCreancierPayload;

export interface CreancierSearchParams {
  page?: number;
  size?: number;
  nom?: string;
  typeCreancier?: TypeCreancier;
  ice?: string;
  rc?: string;
  rib?: string;
  banque?: string;
  email?: string;
  telephone?: string;
  adresse?: string;
}

export type CreancierPage = SpringPage<Creancier>;

export const TYPE_CREANCIER_OPTIONS: Array<{ label: string; value: TypeCreancier }> = [
  { label: 'IAM', value: 'IAM' },
  { label: 'Banque', value: 'BANQUE' },
  { label: 'ONEE', value: 'ONEE' },
  { label: 'Clinique', value: 'CLINIQUE' },
  { label: 'Autre', value: 'AUTRE' }
];
