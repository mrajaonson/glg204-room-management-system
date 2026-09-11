import { http } from '@/api/http';

export interface AccountCreationRequest {
  login: string;
  password: string;
  passwordConfirmation: string;
  email: string;
}

export async function requestAccountCreation(request: AccountCreationRequest): Promise<void> {
  await http.post('/accounts/requests', request);
}

export async function validateEmail(token: string): Promise<void> {
  await http.get('/accounts/requests/validate', { params: { token } });
}

export interface AccountCreationRequestResponse {
  id: number;
  login: string;
  email: string;
  status: 'CREATED' | 'EMAIL_SENT' | 'EMAIL_VALIDATED' | 'VALIDATED' | 'REFUSED';
  createdAt: string;
}

export async function fetchAccountCreationRequests(): Promise<AccountCreationRequestResponse[]> {
  const { data } = await http.get<AccountCreationRequestResponse[]>('/accounts/requests');
  return data;
}

export async function validateAccountCreationRequest(id: number): Promise<void> {
  await http.put(`/accounts/requests/${id}/validate`);
}

export async function refuseAccountCreationRequest(id: number): Promise<void> {
  await http.put(`/accounts/requests/${id}/refuse`);
}
