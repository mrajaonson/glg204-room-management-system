import { defineStore, acceptHMRUpdate } from 'pinia';
import { jwtDecode } from 'jwt-decode';
import { http } from '@/api/http';

export type Role = 'USER' | 'MANAGER' | 'ADMIN';

interface TokenResponse {
  token: string;
  type: string;
  expiresIn: number;
}

interface TokenClaims {
  sub: string;
  roles: Role[];
  exp: number;
}

const TOKEN_STORAGE_KEY = 'auth.token';

function decodeClaims(token: string): TokenClaims | null {
  try {
    return jwtDecode<TokenClaims>(token);
  } catch {
    return null;
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_STORAGE_KEY),
  }),

  getters: {
    claims: (state) => (state.token ? decodeClaims(state.token) : null),
    login(): string | null {
      return this.claims?.sub ?? null;
    },
    roles(): Role[] {
      return this.claims?.roles ?? [];
    },
    isAdmin(): boolean {
      return this.roles.includes('ADMIN');
    },
    isManager(): boolean {
      return this.isAdmin || this.roles.includes('MANAGER');
    },
  },

  actions: {
    async authenticate(login: string, password: string) {
      const { data } = await http.post<TokenResponse>('/auth/login', { login, password });
      this.token = data.token;
      localStorage.setItem(TOKEN_STORAGE_KEY, data.token);
    },

    logout() {
      this.token = null;
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    },

    hasValidSession(): boolean {
      const claims = this.claims;
      if (!claims || claims.exp * 1000 <= Date.now()) {
        if (this.token) {
          this.logout();
        }
        return false;
      }
      return true;
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot));
}
