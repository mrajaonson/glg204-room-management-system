import { http } from '@/api/http';

export type ReservationStatus = 'PENDING_APPROVAL' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';

export interface ReservationResponse {
  id: number;
  roomId: number;
  requesterId: number;
  startAt: string;
  endAt: string;
  purpose: string;
  status: ReservationStatus;
  createdAt: string;
}

export interface ReservationCreationRequest {
  roomId: number;
  startAt: string;
  endAt: string;
  purpose: string;
}

export async function fetchReservations(
  roomId?: number,
  currentUserOnly?: boolean,
): Promise<ReservationResponse[]> {
  const { data } = await http.get<ReservationResponse[]>('/reservations', {
    params: { roomId, currentUserOnly },
  });
  return data;
}

export async function createReservation(request: ReservationCreationRequest): Promise<void> {
  await http.post('/reservations', request);
}
