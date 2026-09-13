import { HttpErrorResponse } from '@angular/common/http';

import { ApiError } from './models/auth.model';

/**
 * Pulls something a person can act on out of a failed call.
 *
 * The backend answers with {status, message} for every failure it handled, and that message is
 * already written for the end user, so it wins whenever there is one. What is left are the
 * failures that never reached the backend or came back bare, and those get plain wording here:
 * no status codes, no port numbers, nothing about the server's own setup.
 */
export function toErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }
  if (error.status === 0) {
    return 'Cannot reach the service right now. Check your connection and try again.';
  }

  const message = serverMessage(error.error as ApiError | string | null);
  // The backend collapses every rejected body to a bare "Invalid request", which names no field
  // and gives nobody anything to fix. The caller's fallback at least says which action failed.
  if (message && message !== 'Invalid request') {
    return message;
  }
  return statusMessage(error.status, fallback);
}

function serverMessage(body: ApiError | string | null): string | null {
  if (typeof body === 'string') {
    return body.trim() || null;
  }
  return body?.message ?? null;
}

/** Only reached when the response carried no message of its own. */
function statusMessage(status: number, fallback: string): string {
  if (status === 401) {
    return 'Your session has ended. Please sign in again.';
  }
  if (status === 403) {
    return 'You do not have permission to do that.';
  }
  if (status === 404) {
    return 'We could not find what you were looking for.';
  }
  if (status === 408 || status === 504) {
    return 'That took too long to respond. Please try again.';
  }
  if (status >= 500) {
    return 'Something went wrong on our side. Please try again in a moment.';
  }
  return fallback;
}
