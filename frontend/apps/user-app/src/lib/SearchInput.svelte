<script lang="ts">
	import { Search, X } from 'lucide-svelte';
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
		value = '', 
		placeholder = 'Search...',
		loading = false,
		onSearch,
		onInput 
	}: Props = $props();

	let inputValue: string = value;
	let inputElement: HTMLInputElement;

	// Sync with props changes
	$effect(() => {
		inputValue = value;
	});

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
		inputValue = target.value;
		onInput?.(inputValue);
		debouncedSearch(inputValue);
	}

	function handleKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			const validation = validateSearchQuery(inputValue);
			if (validation.isValid) {
				const sanitizedQuery = sanitizeSearchQuery(inputValue);
				onSearch?.(sanitizedQuery);
			}
		}
	}

	function handleClear() {
		inputValue = '';
		onInput?.('');
		onSearch?.('');
		inputElement?.focus();
	}

	// Expose methods
	export function focus() {
		inputElement?.focus();
	}

	export function clear() {
		handleClear();
	}
</script>

<div class="relative">
	<input
		bind:this={inputElement}
		type="text"
		value={inputValue}
		{placeholder}
		class="w-full border-none bg-transparent text-2xl text-gray-900 placeholder-gray-400 focus:ring-0 dark:text-[#c9d1d9] dark:placeholder-[#484f58]"
		oninput={handleInput}
		onkeydown={handleKeydown}
	/>
	
	{#if inputValue}
		<button
			onclick={handleClear}
			class="absolute top-1/2 right-0 -translate-y-1/2 text-gray-500 dark:text-[#c9d1d9] p-1 hover:text-gray-700 dark:hover:text-[#c9d1d9]"
			aria-label="Clear search"
		>
			{#if loading}
				<div class="h-6 w-6 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600"></div>
			{:else}
				<X size={24} />
			{/if}
		</button>
	{:else}
		<Search size={24} class="absolute top-1/2 right-0 -translate-y-1/2 text-gray-500 dark:text-[#c9d1d9]" />
	{/if}
	
	<div class="absolute bottom-0 left-0 h-[2px] w-full bg-blue-500 dark:bg-[#58a6ff]"></div>
</div>