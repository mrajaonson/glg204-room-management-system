import { http } from '@/api/http';

export type RoomType = 'CLASSROOM' | 'LAB' | 'MEETING' | 'AUDITORIUM';

export interface RoomResponse {
  id: number;
  name: string;
  location: string;
  capacity: number;
  type: RoomType;
  description: string | null;
  reservationRequiresApproval: boolean;
}

export async function fetchRooms(): Promise<RoomResponse[]> {
  const { data } = await http.get<RoomResponse[]>('/rooms');
  return data;
}

export interface RoomCreationRequest {
  name: string;
  location: string;
  capacity: number;
  type: RoomType;
  description: string | null;
  reservationRequiresApproval: boolean;
}

export async function createRoom(request: RoomCreationRequest): Promise<void> {
  await http.post('/rooms', request);
}

// Backend LocalDateTime, no time zone: "2026-09-11T14:00:00"
export interface AvailabilitySlotCreationRequest {
  startAt: string;
  endAt: string;
}

export async function addAvailabilitySlot(
  roomId: number,
  request: AvailabilitySlotCreationRequest,
): Promise<void> {
  await http.post(`/rooms/${roomId}/availabilities`, request);
}

export async function fetchRoom(roomId: number): Promise<RoomResponse> {
  const { data } = await http.get<RoomResponse>(`/rooms/${roomId}`);
  return data;
}

// Same fields as a creation
export async function updateRoom(roomId: number, request: RoomCreationRequest): Promise<void> {
  await http.put(`/rooms/${roomId}`, request);
}

export interface AvailabilitySlotResponse {
  id: number;
  startAt: string;
  endAt: string;
  roomId: number;
}

export async function fetchAvailabilitySlots(roomId: number): Promise<AvailabilitySlotResponse[]> {
  const { data } = await http.get<AvailabilitySlotResponse[]>(`/rooms/${roomId}/availabilities`);
  return data;
}
