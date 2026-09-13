export type Role = 'ADMIN' | 'USER';

/** Answer from POST /login, produced by the backend JSON login filter. */
export interface LoginResponse {
  status: string;
  message: string;
  token: string;
  userId: number;
  email: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  fullName: string;
  password: string;
  confirmPassword: string;
}

/** The uniform error envelope every backend endpoint uses. */
export interface ApiError {
  status: string;
  message: string;
}
