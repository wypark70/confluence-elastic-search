import { paraglideVitePlugin } from '@inlang/paraglide-js';
import devtoolsJson from 'vite-plugin-devtools-json';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vitest/config';
import { playwright } from '@vitest/browser-playwright';
import { sveltekit } from '@sveltejs/kit/vite';

export default defineConfig({
	plugins: [
		tailwindcss(),
		sveltekit(),
		devtoolsJson(),
		paraglideVitePlugin({ project: './project.inlang', outdir: './src/lib/paraglide' }),
		{
			name: 'fix-paraglide-sourcemap',
			apply: 'serve', // Only apply in dev
			enforce: 'post', // Run after other plugins
			async configureServer(server) {
				const fs = await import('node:fs');
				const path = await import('node:path');
				const fixMaps = () => {
					const paraglideDir = path.resolve(process.cwd(), 'src/lib/paraglide');
					if (!fs.existsSync(paraglideDir)) return;

					const filesToFix = ['strategy.js.map', 'middleware.js.map'];
					filesToFix.forEach((file) => {
						const filePath = path.join(paraglideDir, file);
						if (!fs.existsSync(filePath)) {
							fs.writeFileSync(
								filePath,
								'{"version":3,"file":"' + file.replace('.map', '') + '","sources":[],"mappings":""}'
							);
							console.log('[fix-paraglide-sourcemap] Created dummy ' + file);
						}
					});
				};

				// Fix on start
				fixMaps();

				// Fix on file change (if paraglide regenerates)
				server.watcher.on('all', (eventName, path) => {
					if (path.includes('paraglide') && (eventName === 'add' || eventName === 'change')) {
						fixMaps();
					}
				});
			}
		}
	],

	test: {
		expect: { requireAssertions: true },

		projects: [
			{
				extends: './vite.config.ts',

				test: {
					name: 'client',

					browser: {
						enabled: true,
						provider: playwright(),
						instances: [{ browser: 'chromium', headless: true }]
					},

					include: ['src/**/*.svelte.{test,spec}.{js,ts}'],
					exclude: ['src/lib/server/**']
				}
			},

			{
				extends: './vite.config.ts',

				test: {
					name: 'server',
					environment: 'node',
					include: ['src/**/*.{test,spec}.{js,ts}'],
					exclude: ['src/**/*.svelte.{test,spec}.{js,ts}']
				}
			}
		]
	}
});
