import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule, NgIf, NgClass } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIf, NgClass, RouterLink, RouterLinkActive],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  loginForm!: FormGroup;
  loginError: string | null = null;
  registrationSuccessMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });

    this.route.queryParams.subscribe((params) => {
      if (params['registration'] === 'success') {
        this.registrationSuccessMessage =
          'Registration successful! Please log in with your new credentials.';
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { registration: null },
          queryParamsHandling: 'merge',
        });
      }
    });
  }

  onLoginSubmit(): void {
    this.loginError = null;
    this.registrationSuccessMessage = null;
    if (this.loginForm.valid) {
      const formValue = this.loginForm.value;

      const loginPayload = {
        username: formValue.username,
        password: formValue.password,
      };

      this.authService.login(loginPayload).subscribe({
        next: (response) => {
          this.authService.saveToken(response.token);
          this.router.navigate(['/transfer']);
        },
        error: (err) => {
          console.error('Login failed:', err);
          this.loginError = err.error?.message || 'Login failed. Please check your credentials.';
        },
      });
    } else {
      this.loginForm.markAllAsTouched();
    }
  }
}
