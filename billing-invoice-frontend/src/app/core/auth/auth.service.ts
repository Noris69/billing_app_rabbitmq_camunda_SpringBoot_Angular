import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly keycloak = new Keycloak({
    url: 'http://localhost:8081',
    realm: 'billing',
    clientId: 'billing-frontend'
  });

  init(): Promise<boolean> {
    return this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false
    });
  }

  get username(): string {
    const token = this.keycloak.tokenParsed as { preferred_username?: string; name?: string } | undefined;
    return token?.name ?? token?.preferred_username ?? 'Utilisateur';
  }

  async getToken(): Promise<string | undefined> {
    if (!this.keycloak.authenticated) {
      return undefined;
    }

    await this.keycloak.updateToken(30);
    return this.keycloak.token;
  }

  logout(): Promise<void> {
    return this.keycloak.logout({
      redirectUri: window.location.origin
    });
  }
}
