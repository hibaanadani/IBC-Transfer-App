import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule, NgIf, NgClass } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive, NgIf, NgClass],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit {
  registerForm!: FormGroup;
  registerError: string | null = null;
  registrationSuccess: boolean = false;

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      initialDeposit: [0.01, [Validators.required, Validators.min(0.01)]],
    });
  }

  onRegisterSubmit(): void {
    this.registerError = null;
    this.registrationSuccess = false;

    if (this.registerForm.valid) {
      const formValue = this.registerForm.value;

      const registerPayload = {
        username: formValue.username,
        email: formValue.email,
        password: formValue.password,
        initialDeposit: formValue.initialDeposit,
      };

      this.authService.register(registerPayload).subscribe({
        next: () => {
          this.registrationSuccess = true;
          this.registerForm.reset({ initialDeposit: 0.01 });
          setTimeout(() => {
            this.router.navigate(['/login'], { queryParams: { registration: 'success' } });
          }, 1500);
        },
        error: (err) => {
          console.error('Registration failed:', err);

          let errorMessage = 'Registration failed. Please try again.';
          if (err.error?.message) {
            errorMessage = err.error.message;
          } else if (typeof err.error === 'string') {
            errorMessage = err.error;
          }
          this.registerError = errorMessage;
        },
      });
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
}
