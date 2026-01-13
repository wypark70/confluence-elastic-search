import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),

	kit: {
		adapter: adapter({
			fallback: 'index.html',
			pages: '../../dist/admin-app',
			assets: '../../dist/admin-app',
			precompress: false,
			strict: true
		}),
		paths: {
			base: '/plugins/servlet/admin-app'
		}
	}
};

export default config;
