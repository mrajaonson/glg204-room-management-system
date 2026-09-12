import type { ReservationStatus } from '@/api/reservations';

export const reservationStatusLabels: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: 'En attente de validation',
  CONFIRMED: 'Confirmée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée',
};
