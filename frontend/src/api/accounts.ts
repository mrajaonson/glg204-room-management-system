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
