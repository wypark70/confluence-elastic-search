// Theme management utilities

class ThemeStore {
	mode: 'light' | 'dark' = 'light';
	private isBrowser: boolean;

	constructor() {
		this.isBrowser = typeof window !== 'undefined';
		this.init();
	}

	init() {
		if (this.isBrowser) {
			// Load from localStorage or system preference
			const saved = localStorage.getItem('theme') as 'light' | 'dark' | null;
			if (saved) {
				this.mode = saved;
			} else {
				this.mode = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
			}
			this.apply();
		}
	}

	toggle() {
		this.mode = this.mode === 'light' ? 'dark' : 'light';
		this.save();
		this.apply();
	}

	set(mode: 'light' | 'dark') {
		this.mode = mode;
		this.save();
		this.apply();
	}

	private save() {
		if (this.isBrowser) {
			localStorage.setItem('theme', this.mode);
		}
	}

	private apply() {
		if (this.isBrowser) {
			if (this.mode === 'dark') {
				document.documentElement.classList.add('dark');
			} else {
				document.documentElement.classList.remove('dark');
			}
		}
	}
}

export const theme = new ThemeStore();