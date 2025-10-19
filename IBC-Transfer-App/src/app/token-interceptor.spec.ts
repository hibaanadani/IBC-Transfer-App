import { TestBed } from '@angular/core/testing';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TokenInterceptor } from './token-interceptor';
import { AuthService } from './auth/auth.service';
import { of } from 'rxjs';

describe('TokenInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authService: jasmine.SpyObj<AuthService>; // Use SpyObj for the service

  const mockToken = 'mock-jwt-token-12345';
  const testUrl = '/api/protected';

  // Set up the testing module before each test
  beforeEach(() => {
    // Create a spy object for AuthService methods needed by the interceptor
    authService = jasmine.createSpyObj('AuthService', ['getToken']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        // Provide the real HttpClient for testing purposes
        HttpClient,
        // Provide the mock AuthService
        { provide: AuthService, useValue: authService },
        // Register the TokenInterceptor using the standard HTTP_INTERCEPTORS token
        { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  // After each test, verify that there are no outstanding requests
  afterEach(() => {
    httpMock.verify();
  });

  // --- TEST CASES ---

  it('should be created and correctly set up', () => {
    const interceptor = TestBed.inject(HTTP_INTERCEPTORS).find(
      (i) => i instanceof TokenInterceptor
    );
    expect(interceptor).toBeTruthy();
  });

  it('should add an Authorization header when a token is present', () => {
    // 1. Arrange: Tell the spy that getToken() will return a mock token
    authService.getToken.and.returnValue(mockToken);

    // 2. Act: Make an HTTP request
    httpClient.get(testUrl).subscribe();

    // 3. Assert: Expect a request to be caught by the mock controller
    const req = httpMock.expectOne(testUrl);

    // 4. Final Assert: Check the request headers
    expect(req.request.headers.has('Authorization')).toBeTruthy();
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

    // Flush the request to complete the cycle
    req.flush({});
  });

  it('should NOT add an Authorization header when no token is present', () => {
    // 1. Arrange: Tell the spy that getToken() will return null (no token)
    authService.getToken.and.returnValue(null);

    // 2. Act: Make an HTTP request
    httpClient.get(testUrl).subscribe();

    // 3. Assert: Expect a request to be caught by the mock controller
    const req = httpMock.expectOne(testUrl);

    // 4. Final Assert: Check the request headers
    expect(req.request.headers.has('Authorization')).toBeFalsy();

    // Flush the request
    req.flush({});
  });
});
