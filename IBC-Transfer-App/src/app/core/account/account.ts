import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { UserAccount } from './account.model';

export type { UserAccount };

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private accountApiUrl = '/api/account';
  private transferApiUrl = '/api/transfers';

  constructor(private http: HttpClient) {}

  getAccount(): Observable<UserAccount> {
    return this.http.get<UserAccount>(this.accountApiUrl);
  }

  executeTransfer(recipientAccountNumber: string, amount: number): Observable<any> {
    return this.http.post<any>(this.transferApiUrl, { recipientAccountNumber, amount });
  }
}
