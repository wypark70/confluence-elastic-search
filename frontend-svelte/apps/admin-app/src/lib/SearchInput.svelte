<script lang="ts">
	import { Search, Button } from 'flowbite-svelte';
	import { Search as SearchIcon, X } from 'lucide-svelte';
	import { debounce } from './utils/formatting';
	import { validateSearchQuery, sanitizeSearchQuery } from './utils/validation';

	interface Props {
		value?: string;
		placeholder?: string;
		loading?: boolean;
		onSearch?: (query: string) => void;
		onInput?: (query: string) => void;
	}

	let {
		value = $bindable(''),
		placeholder = 'Search...',
		loading = false,
		onSearch,
		onInput
	}: Props = $props();

	// Debounced search
	const debouncedSearch = debounce((query: string) => {
		const validation = validateSearchQuery(query);
		if (validation.isValid) {
			const sanitizedQuery = sanitizeSearchQuery(query);
			onSearch?.(sanitizedQuery);
		}
	}, 300);

	function handleInput(event: Event) {
		const target = event.target as HTMLInputElement;
		value = target.value;
		onInput?.(value);
		debouncedSearch(value);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			const validation = validateSearchQuery(value);
			if (validation.isValid) {
				const sanitizedQuery = sanitizeSearchQuery(value);
				onSearch?.(sanitizedQuery);
			}
		}
	}

	function handleClear() {
		value = '';
		onInput?.('');
		onSearch?.('');
	}

	export function focus() {
		// Element binding is handled internally by Flowbite's Search,
		// but we can try to use autofocus prop or similar if needed.
		// For now, simpler implementation.
		const input = document.querySelector('input[type="search"]') as HTMLInputElement;
		input?.focus();
	}

	export function clear() {
		handleClear();
	}
</script>

<div class="relative w-full">
	<Search
		size="lg"
		bind:value
		{placeholder}
		class="pl-12 text-lg"
		oninput={handleInput}
		onkeydown={handleKeydown}
	>
		{#snippet left()}
			<div class="pointer-events-none absolute inset-y-0 start-0 flex items-center ps-4">
				<SearchIcon class="text-gray-500 dark:text-gray-400" size={24} />
			</div>
		{/snippet}

		{#if value}
			{#snippet right()}
				<div class="absolute inset-y-0 end-0 flex items-center pe-3">
					{#if loading}
						<div
							class="h-6 w-6 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600"
						></div>
					{:else}
						<Button
							color="light"
							class="p-1 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
							onclick={handleClear}
						>
							<X size={24} />
						</Button>
					{/if}
				</div>
			{/snippet}
		{/if}
	</Search>

	<div class="absolute -bottom-1 left-0 h-[2px] w-full bg-blue-500 dark:bg-blue-600"></div>
</div>
