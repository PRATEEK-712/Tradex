import { User, UserManager, WebStorageStateStore } from 'oidc-client-ts';

const authority = import.meta.env.VITE_OIDC_AUTHORITY;
const clientId = import.meta.env.VITE_OIDC_CLIENT_ID;
const redirectUri = import.meta.env.VITE_OIDC_REDIRECT_URI || window.location.origin;

export const authEnabled = Boolean(authority && clientId);

export const userManager = authEnabled
  ? new UserManager({
      authority,
      client_id: clientId,
      redirect_uri: redirectUri,
      post_logout_redirect_uri: redirectUri,
      response_type: 'code',
      scope: import.meta.env.VITE_OIDC_SCOPE || 'openid profile email trading.read trading.write',
      automaticSilentRenew: true,
      userStore: new WebStorageStateStore({ store: window.localStorage }),
    })
  : null;

export async function getCurrentUser(): Promise<User | null> {
  if (!userManager) {
    return null;
  }
  if (window.location.search.includes('code=') && window.location.search.includes('state=')) {
    const user = await userManager.signinRedirectCallback();
    window.history.replaceState({}, document.title, window.location.pathname);
    return user;
  }
  return userManager.getUser();
}
