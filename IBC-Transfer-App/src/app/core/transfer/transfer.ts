import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AccountService, UserAccount } from '../account/account';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIf, CurrencyPipe],
  templateUrl: './transfer.html',
  styleUrl: './transfer.scss',
})
export class Transfer implements OnInit {
  account: UserAccount | null = null;

  // Dynamically populated properties
  userName: string;
  userIdentifier: string;

  transferForm!: FormGroup;

  transferMessage: string | null = null;
  isError: boolean = false;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private authService: AuthService,
    private router: Router
  ) {
    // FIX: Dynamic assignment in the constructor using JWT data
    const username = this.authService.getUsernameFromToken();

    if (username) {
      this.userIdentifier = username;
      const capitalizedUsername = username.charAt(0).toUpperCase() + username.slice(1);
      this.userName = `Welcome, ${capitalizedUsername}`;
    } else {
      this.userIdentifier = 'Guest';
      this.userName = 'Please Login';
    }
  }

  ngOnInit(): void {
    this.loadAccount();
    this.initForm();
  }

  initForm(): void {
    this.transferForm = this.fb.group({
      recipientAccountNumber: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
    });
  }

  loadAccount(): void {
    this.accountService.getAccount().subscribe({
      next: (data) => {
        this.account = data;
        this.isError = false;
      },
      error: (err) => {
        console.error('Failed to load account:', err);
        this.transferMessage = 'Error loading account data.';
        this.isError = true;

        if (err.status === 401) {
          this.authService.removeToken();
          this.router.navigate(['/login']);
        }
      },
    });
  }

  onTransferSubmit(): void {
    this.transferMessage = null;
    this.isError = false;

    if (this.transferForm.invalid || !this.account) {
      this.transferForm.markAllAsTouched();
      return;
    }

    const { recipientAccountNumber, amount } = this.transferForm.value;

    this.accountService.executeTransfer(recipientAccountNumber, amount).subscribe({
      next: (response) => {
        this.transferMessage = response.message || 'Transfer successful!';
        this.isError = false;
        this.loadAccount();
        this.transferForm.reset({ recipientAccountNumber: '', amount: null });
      },
      error: (err) => {
        console.error('Transfer failed:', err);
        this.transferMessage = err.error?.message || 'Transfer failed due to an unknown error.';
        this.isError = true;
      },
    });
  }

  logout(): void {
    this.authService.removeToken();
    this.router.navigate(['/login']);
  }
}
