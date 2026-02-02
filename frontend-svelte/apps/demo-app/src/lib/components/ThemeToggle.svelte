<script lang="ts">
	import { Sun, Moon } from 'lucide-svelte';
	import { theme } from '$lib/stores/theme.svelte';
	import { Button } from 'flowbite-svelte';
</script>

<Button
	color="light"
	onclick={() => theme.toggle()}
	class="!p-2 rounded-full border-gray-200 dark:border-gray-800"
	aria-label="Toggle theme"
>
	{#if theme.mode === 'light'}
		<Sun size={20} class="text-orange-500 dark:text-orange-400" />
	{:else}
		<Moon size={20} class="text-blue-300 dark:text-blue-400" />
	{/if}
</Button>
