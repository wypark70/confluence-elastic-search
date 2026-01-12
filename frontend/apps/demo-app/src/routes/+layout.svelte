<script lang="ts">
	import { page } from '$app/state';
	import { onMount } from 'svelte';
	import { locales, localizeHref } from '@elastic-search/shared/i18n/runtime';
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { theme } from '$lib/stores/theme.svelte';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';

	let { children } = $props();

	onMount(() => {
		theme.init();
	});
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<div class="fixed right-4 bottom-4 z-50">
	<ThemeToggle />
</div>

<div id="demo-app-root">
	{@render children()}
</div>
<div style="display:none">
	{#each locales as locale}
		<a href={localizeHref(page.url.pathname, { locale })}>
			{locale}
		</a>
	{/each}
</div>
