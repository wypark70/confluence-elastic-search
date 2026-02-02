import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [tailwindcss(), sveltekit()],
	build: {
		outDir: '../../dist/demo-app',
		emptyOutDir: true,
		minify: 'esbuild',
		target: 'es2020',
		rollupOptions: {
			output: {
				manualChunks: {
					vendor: ['svelte'],
					flowbite: ['flowbite-svelte', 'flowbite-svelte-icons']
				}
			}
		},
		chunkSizeWarningLimit: 1000,
		sourcemap: false
	},
	optimizeDeps: {
		include: ['flowbite-svelte', 'lucide-svelte']
	}
});
