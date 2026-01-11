import { browser } from '$app/environment';

class ThemeStore {
	mode = $state<'light' | 'dark'>('light');

	constructor() {
		// Initialize is separate to avoid SSR issues if called too early,
		// though likely safe in constructor if guarded by browser check.
	}

	init() {
		if (browser) {
			const stored = localStorage.getItem('theme') as 'light' | 'dark' | null;
			if (stored) {
				this.mode = stored;
			} else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
				this.mode = 'dark';
			}
			this.apply();
		}
	}

	toggle() {
		this.mode = this.mode === 'light' ? 'dark' : 'light';
		this.save();
		this.apply();
	}

	save() {
		if (browser) {
			localStorage.setItem('theme', this.mode);
		}
	}

	apply() {
		if (browser) {
			if (this.mode === 'dark') {
				document.documentElement.classList.add('dark');
				document.documentElement.dataset.colorMode = 'dark';
			} else {
				document.documentElement.classList.remove('dark');
				document.documentElement.dataset.colorMode = 'light';
			}
		}
	}
}

export const theme = new ThemeStore();
