// src/app/app.config.ts

import { ApplicationConfig } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
// FIX: Use modern approach for HttpClient and SSR
import { provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';

// Imports must use the exported class names from their files
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { Transfer } from './core/transfer/transfer';

import { TokenInterceptor } from './token-interceptor';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'transfer', component: Transfer },
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    // FIX 1: Use provideHttpClient and withFetch() for optimal SSR
    provideHttpClient(withFetch()),

    // 2. Provide the interceptor
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true,
    },
  ],
};
