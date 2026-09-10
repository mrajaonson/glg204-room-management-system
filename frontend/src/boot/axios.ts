import { defineBoot } from '#q-app';
import { isAxiosError } from 'axios';
import { http } from '@/api/http';
import { useUserStore } from '@/stores/user-store';

export default defineBoot(({ store, router }) => {
  const userStore = useUserStore(store);

  http.interceptors.request.use((config) => {
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  });

  http.interceptors.response.use(
    (response) => response,
    async (error: unknown) => {
      if (isAxiosError(error) && error.response?.status === 401 && userStore.token) {
        userStore.logout();
        await router.push({
          name: '/login',
          query: { redirect: router.currentRoute.value.fullPath },
        });
      }
      throw error;
    },
  );
});
