import { defineRouter } from '#q-app';
import { routes, handleHotUpdate, type RouteNamedMap } from 'vue-router/auto-routes';
import {
  createMemoryHistory,
  createRouter,
  createWebHashHistory,
  createWebHistory,
} from 'vue-router';
import { useUserStore } from '@/stores/user-store';

/*
 * If not building with SSR mode, you can
 * directly export the Router instantiation;
 *
 * The function below can be async too; either use
 * async/await or return a Promise which resolves
 * with the Router instance.
 */

export default defineRouter(({ store }) => {
  const createHistory = import.meta.env.QUASAR_SERVER
    ? createMemoryHistory
    : import.meta.env.QUASAR_VUE_ROUTER_MODE === 'history'
      ? createWebHistory
      : createWebHashHistory;

  const Router = createRouter({
    scrollBehavior: () => ({ left: 0, top: 0 }),
    routes,

    // Leave this as is and make changes in quasar.conf.js instead!
    // quasar.conf.js -> build -> vueRouterMode
    // quasar.conf.js -> build -> publicPath
    history: createHistory(import.meta.env.QUASAR_VUE_ROUTER_BASE),
  });

  // Reachable by anyone
  const publicRoutes: Set<keyof RouteNamedMap> = new Set(['/', '/accounts/requests/validate']);
  // Reachable only without a session: a logged-in user is sent home
  const guestOnlyRoutes: Set<keyof RouteNamedMap> = new Set(['/login', '/register']);
  // Reachable only by ADMIN users: others are sent home
  const adminRoutes: Set<keyof RouteNamedMap> = new Set(['/admin/account-requests']);
  // Reachable only by MANAGER users (admins included): others are sent home
  const managerRoutes: Set<keyof RouteNamedMap> = new Set([
    '/rooms/[id]/edit',
    '/manager/reservation-requests',
  ]);

  // Every other page requires a valid session
  Router.beforeEach((to) => {
    const userStore = useUserStore(store);
    const authenticated = userStore.hasValidSession();

    if (guestOnlyRoutes.has(to.name)) {
      return authenticated ? { path: '/' } : true;
    }
    if (publicRoutes.has(to.name)) {
      return true;
    }
    if (!authenticated) {
      return { name: '/login', query: { redirect: to.fullPath } };
    }
    if (adminRoutes.has(to.name) && !userStore.isAdmin) {
      return { path: '/' };
    }
    if (managerRoutes.has(to.name) && !userStore.isManager) {
      return { path: '/' };
    }
    return true;
  });

  // enable HMR for it
  if (import.meta.hot) {
    handleHotUpdate(Router);
  }

  return Router;
});
