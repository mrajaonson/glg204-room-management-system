import type { RoomCreationRequest, RoomResponse, RoomType } from '@/api/rooms';

export interface RoomFormValue {
  name: string;
  location: string;
  capacity: number | null;
  type: RoomType | null;
  description: string;
  reservationRequiresApproval: boolean;
}

export const roomTypeLabels: Record<RoomType, string> = {
  CLASSROOM: 'Salle de classe',
  LAB: 'Laboratoire',
  MEETING: 'Salle de réunion',
  AUDITORIUM: 'Amphithéâtre',
};

export function emptyRoomForm(): RoomFormValue {
  return {
    name: '',
    location: '',
    capacity: null,
    type: null,
    description: '',
    reservationRequiresApproval: false,
  };
}

export function roomToForm(room: RoomResponse): RoomFormValue {
  return {
    name: room.name,
    location: room.location,
    capacity: room.capacity,
    type: room.type,
    description: room.description ?? '',
    reservationRequiresApproval: room.reservationRequiresApproval,
  };
}

export function formToRequest(form: RoomFormValue): RoomCreationRequest | null {
  if (form.capacity === null || form.type === null) {
    return null;
  }
  return {
    name: form.name.trim(),
    location: form.location.trim(),
    capacity: form.capacity,
    type: form.type,
    description: form.description.trim() || null,
    reservationRequiresApproval: form.reservationRequiresApproval,
  };
}
