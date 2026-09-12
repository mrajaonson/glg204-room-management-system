import { http } from '@/api/http';

export type ReservationStatus = 'PENDING_APPROVAL' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED';

export interface ReservationResponse {
  id: number;
  roomId: number;
  requesterId: number;
  requesterLogin: string;
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

export async function fetchReservations(roomId?: number): Promise<ReservationResponse[]> {
  const { data } = await http.get<ReservationResponse[]>('/reservations', { params: { roomId } });
  return data;
}

export async function createReservation(request: ReservationCreationRequest): Promise<void> {
  await http.post('/reservations', request);
}

export async function cancelReservation(id: number): Promise<void> {
  await http.put(`/reservations/${id}/cancel`);
}

export async function fetchPendingReservations(): Promise<ReservationResponse[]> {
  const { data } = await http.get<ReservationResponse[]>('/reservations', {
    params: { status: 'PENDING_APPROVAL' },
  });
  return data;
}

export async function approveReservation(id: number): Promise<void> {
  await http.put(`/reservations/${id}/approve`);
}

export async function rejectReservation(id: number, reason: string): Promise<void> {
  await http.put(`/reservations/${id}/reject`, { reason });
}
