import { HttpErrorResponse } from '@angular/common/http';

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if ((error as { name?: string } | null)?.name === 'TimeoutError') {
    return 'Le serveur met trop de temps a repondre. Verifie le backend puis reessaie.';
  }

  if (error instanceof HttpErrorResponse) {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.error?.details?.length) {
      return `${error.error.message ?? fallback} ${error.error.details.join(' ')}`;
    }

    if (error.error?.message) {
      return error.error.message;
    }
  }

  return fallback;
}
