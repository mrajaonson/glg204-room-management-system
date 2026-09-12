import { http } from '@/api/http';

export type RoomType = 'CLASSROOM' | 'LAB' | 'MEETING' | 'AUDITORIUM';

export interface RoomResponse {
  id: number;
  name: string;
  location: string;
  capacity: number;
  type: RoomType;
  description: string;
  reservationRequiresApproval: boolean;
}

export async function fetchRooms(): Promise<RoomResponse[]> {
  const { data } = await http.get<RoomResponse[]>('/rooms');
  return data;
}

export interface RoomSearchFilter {
  name: string;
  location: string;
  capacityMin: number | null;
  capacityMax: number | null;
  type: RoomType | null;
  equipments: string[];
  startAt: string | null;
  endAt: string | null;
}

export async function searchRooms(filter: RoomSearchFilter): Promise<RoomResponse[]> {
  const { data } = await http.post<RoomResponse[]>('/rooms/search', filter);
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

export interface EquipmentResponse {
  id: number;
  name: string;
  description: string | null;
  roomId: number;
}

export interface EquipmentCreationRequest {
  name: string;
  description: string | null;
}

export async function fetchEquipments(roomId: number): Promise<EquipmentResponse[]> {
  const { data } = await http.get<EquipmentResponse[]>(`/rooms/${roomId}/equipments`);
  return data;
}

export async function addEquipment(
  roomId: number,
  request: EquipmentCreationRequest,
): Promise<void> {
  await http.post(`/rooms/${roomId}/equipments`, request);
}
