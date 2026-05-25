export type PointDeVenteType = 'AGENCE' | 'DISTRIBUTEUR';

export interface PointDeVente {
  id: number;
  type: PointDeVenteType;
  nom: string;
  adresse: string;
  telephone: string;
  codeAgence?: string | null;
  responsable?: string | null;
  region?: string | null;
  typeAgence?: string | null;
  codeDistributeur?: string | null;
  zoneDistribution?: string | null;
  nomCommercial?: string | null;
  commission?: number | null;
}

export type CreatePointDeVentePayload = Omit<PointDeVente, 'id'>;

export type UpdatePointDeVentePayload = CreatePointDeVentePayload;

export interface PointDeVenteSearchParams {
  page?: number;
  size?: number;
  type_point_de_vente?: PointDeVenteType;
  nom?: string;
  adresse?: string;
  telephone?: string;
  codeAgence?: string;
  responsable?: string;
  region?: string;
  typeAgence?: string;
  codeDistributeur?: string;
  zoneDistribution?: string;
  nomCommercial?: string;
  commission?: number;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  numberOfElements: number;
  pageable?: unknown;
  sort?: unknown;
}

export const POINT_DE_VENTE_TYPES: Array<{ label: string; value: PointDeVenteType }> = [
  { label: 'Agence', value: 'AGENCE' },
  { label: 'Distributeur', value: 'DISTRIBUTEUR' }
];
