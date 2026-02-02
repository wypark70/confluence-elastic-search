<script lang="ts">
	import SearchInput from '../lib/SearchInput.svelte';
	import SearchResults from '../lib/SearchResults.svelte';
	import LoadingSpinner from '../lib/LoadingSpinner.svelte';
	import Pagination from '../lib/Pagination.svelte';
	import getSampleResponse from '$lib/assets/SampleResponse';
	import { Tag, LayoutGrid } from 'lucide-svelte';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';

	interface SearchResult {
		id: number;
		type: string;
		title: string;
		space: string;
		date: string;
		snippet: string;
		highlightValues: string[];
		url?: string;
	}

	interface SearchResponse {
		results: SearchResult[];
		totalResults: number;
		currentPage: number;
		pageSize: number;
		totalPages: number;
	}

	let searchQuery = $state('');
	let searchResults: SearchResult[] = $state([]);
	let loading = $state(false);
	let searchPerformed = $state(false);

	// Derive state from URL
	let currentPage = $derived(Number(page.url.searchParams.get('page')) || 1);
	let currentQuery = $derived(page.url.searchParams.get('q') || '');
	let totalResults = $state(0);
	let totalPages = $state(0);

	// React to URL changes
	$effect(() => {
		if (currentQuery) {
			searchQuery = currentQuery; // Sync input with URL
			performSearch(currentQuery, currentPage);
		} else {
			// Reset state if no query
			searchQuery = '';
			searchResults = [];
			searchPerformed = false;
			totalResults = 0;
			totalPages = 0;
		}
	});

	async function performSearch(query: string, pageNum: number) {
		loading = true;
		searchPerformed = true;

		try {
			// Simulate API call
			await new Promise((resolve) => setTimeout(resolve, 300)); // Add slight delay for realism
			const data: SearchResponse = getSampleResponse(query, pageNum);
			searchResults = data.results;
			totalResults = data.totalResults;
			totalPages = data.totalPages;
		} catch (error) {
			console.error('Search failed:', error);
			searchResults = [];
			totalResults = 0;
			totalPages = 0;
		} finally {
			loading = false;
		}
	}

	function handleSearch(query: string) {
		if (!query.trim()) {
			goto('?', { keepFocus: true });
			return;
		}
		// Reset to page 1 for new searches
		const url = new URL(page.url);
		url.searchParams.set('q', query);
		url.searchParams.set('page', '1');
		goto(url.toString(), { keepFocus: true });
	}
</script>

<!-- Main Content Content -->
<div class="flex h-full flex-col">
	<!-- Top Search Bar Area -->
	<div
		class="z-10 border-b border-gray-200 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-800"
	>
		<div class="mx-auto max-w-4xl">
			<div class="relative mb-4">
				<SearchInput value={searchQuery} {loading} onSearch={handleSearch} />
			</div>

			<div class="flex items-center justify-between">
				<h2 class="text-lg font-medium text-gray-700 dark:text-gray-200">
					{searchPerformed
						? `${totalResults} result${totalResults !== 1 ? 's' : ''} found`
						: 'Search content'}
				</h2>
				<button
					class="text-sm font-medium text-blue-600 transition-colors hover:text-blue-800 dark:text-blue-500 dark:hover:text-blue-400"
					>Search tips</button
				>
			</div>
		</div>
	</div>

	<!-- Scrollable Results Area -->
	<div class="flex-1 overflow-y-auto bg-gray-50 p-6 dark:bg-gray-900">
		<div class="mx-auto max-w-4xl pb-10">
			{#if loading}
				<div class="flex justify-center p-12">
					<LoadingSpinner size="lg" />
				</div>
			{:else if searchResults.length > 0}
				<SearchResults results={searchResults} />
				<div class="mt-8 flex justify-center">
					<Pagination {currentPage} {totalPages} />
				</div>
			{:else if searchPerformed}
				<div class="py-16 text-center">
					<div
						class="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800"
					>
						<Tag size={32} class="text-gray-400" />
					</div>
					<p class="text-lg font-medium text-gray-900 dark:text-gray-100">
						No results found for "{searchQuery}"
					</p>
					<p class="mt-2 text-gray-500 dark:text-gray-400">
						Try adjusting your search or filter to find what you're looking for.
					</p>
				</div>
			{:else}
				<div class="py-16 text-center">
					<div
						class="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-blue-50 dark:bg-blue-900/20"
					>
						<LayoutGrid size={32} class="text-blue-500 dark:text-blue-400" />
					</div>
					<p class="text-lg font-medium text-gray-900 dark:text-gray-100">Start your search</p>
					<p class="mt-2 text-gray-500 dark:text-gray-400">
						Type in the search box above to find pages, spaces, and more.
					</p>
				</div>
			{/if}
		</div>
	</div>
</div>
