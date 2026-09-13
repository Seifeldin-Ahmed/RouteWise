import { Injectable, effect, signal } from '@angular/core';

export type Theme = 'dark' | 'light';

const STORAGE_KEY = 'routewise.theme';

/**
 * Owns the `data-theme` attribute on <html>. The inline script in index.html has already set it
 * from storage or the system preference before first paint, so the starting value is read back
 * from the DOM rather than worked out a second time here.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(document.documentElement.dataset['theme'] === 'light' ? 'light' : 'dark');

  constructor() {
    effect(() => {
      const theme = this.theme();
      document.documentElement.dataset['theme'] = theme;
      localStorage.setItem(STORAGE_KEY, theme);
    });
  }

  toggle(): void {
    this.theme.update((current) => (current === 'dark' ? 'light' : 'dark'));
  }
}
