<script lang="ts">
	import { Sun, Moon } from 'lucide-svelte';
	import { theme } from '$lib/stores/theme.svelte';
</script>

<button
	onclick={() => theme.toggle()}
	class="flex items-center justify-center rounded-full border border-gray-200 p-2 transition-colors hover:bg-gray-200 dark:border-gray-800 dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700"
	aria-label="Toggle theme"
>
	{#if theme.mode === 'light'}
		<Sun size={20} class="text-orange-500 dark:text-orange-400" />
	{:else}
		<Moon size={20} class="text-blue-300 dark:text-blue-400" />
	{/if}
</button>
