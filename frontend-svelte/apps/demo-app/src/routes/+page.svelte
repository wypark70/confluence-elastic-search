<script lang="ts">
	import SearchInput from '../lib/SearchInput.svelte';
	import SearchResults from '../lib/SearchResults.svelte';
	import LoadingSpinner from '../lib/LoadingSpinner.svelte';
	import Pagination from '../lib/Pagination.svelte';
	import getSampleResponse from '$lib/assets/SampleResponse';
	import { Folder, User, Image, Calendar, Tag, LayoutGrid, ChevronDown } from 'lucide-svelte';

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

	let searchQuery = '';
	let searchResults: SearchResult[] = [];
	let loading = false;
	let searchPerformed = false;
	let currentPage = 1;
	let totalResults = 0;
	let totalPages = 0;
	let pageSize = 5;

	const filters = [
		{ label: 'Space', icon: Folder },
		{ label: 'Contributor', icon: User },
		{ label: 'Type', icon: Image }, // Using Image as a proxy for "Type" or generic media
		{ label: 'Date', icon: Calendar },
		{ label: 'Label', icon: Tag },
		{ label: 'Space category', icon: LayoutGrid }
	];

	async function handleSearch(query: string, page: number = 1) {
		searchQuery = query;
		currentPage = page;

		if (!query.trim()) {
			searchResults = [];
			searchPerformed = false;
			totalResults = 0;
			totalPages = 0;
			return;
		}

		loading = true;
		searchPerformed = true;

		try {
			/*const response = await fetch(`/rest/myplugin/1/feedback/search?q=${encodeURIComponent(query)}&page=${page}&size=${pageSize}`);
			if (response.ok) {
				const data: SearchResponse = await response.json();
				searchResults = data.results;
				totalResults = data.totalResults;
				totalPages = data.totalPages;
				currentPage = data.currentPage;
			} else {
				console.error('Search failed:', response.statusText);
				searchResults = [];
				totalResults = 0;
				totalPages = 0;
			}*/

			const data: SearchResponse = getSampleResponse(query, currentPage);
			searchResults = data.results;
			totalResults = data.totalResults;
			totalPages = data.totalPages;
			currentPage = data.currentPage;
		} catch (error) {
			console.error('Search failed:', error);
			searchResults = [];
			totalResults = 0;
			totalPages = 0;
		} finally {
			loading = false;
		}
	}

	function handlePageChange(page: number) {
		handleSearch(searchQuery, page);
	}
</script>

<div
	class="flex min-h-screen flex-col bg-white font-sans text-gray-900 dark:bg-[#1c2128] dark:text-[#9fadbc]"
>
	<div class="flex flex-1 overflow-hidden">
		<!-- Sidebar -->
		<div class="flex w-64 flex-col gap-4 border-r border-transparent p-8">
			<!-- Border transparent for now, maybe layout specific -->

			<div
				class="mb-2 text-xs font-bold tracking-wider text-gray-500 uppercase dark:text-[#768390]"
			>
				Filter By
			</div>

			{#each filters as filter}
				<button
					class="group flex w-full items-center justify-between rounded p-2 text-left transition-colors hover:bg-gray-200 dark:border-gray-800 dark:bg-gray-800 dark:hover:bg-[#2d333b]"
				>
					<div class="flex items-center gap-3 text-gray-700 dark:text-[#c9d1d9]">
						<filter.icon
							size={18}
							class="text-gray-500 group-hover:text-gray-900 dark:text-[#768390] dark:group-hover:text-[#c9d1d9]"
						/>
						<span class="text-sm">{filter.label}</span>
					</div>
					<ChevronDown size={14} class="text-gray-500 dark:text-[#768390]" />
				</button>
			{/each}

			<div class="mt-auto pt-4">
				<button
					class="text-sm text-blue-600 hover:underline dark:bg-gray-800 dark:text-[#58a6ff] dark:hover:bg-gray-700"
					>Advanced search</button
				>
			</div>
		</div>

		<!-- Main Content -->
		<div class="flex-1 overflow-y-auto p-8">
			<!-- Search Bar -->
			<div class="flex items-center justify-between border-b border-gray-200 dark:border-[#2c333a]">
				<div class="relative flex-1">
					<SearchInput value={searchQuery} {loading} onSearch={handleSearch} />
				</div>
			</div>

			<!-- Results Section -->
			<div class="mb-6 flex items-end justify-between">
				<h2 class="text-lg text-gray-600 dark:text-[#768390]">
					{searchPerformed
						? `${totalResults} search result${totalResults !== 1 ? 's' : ''} (page ${currentPage} of ${totalPages})`
						: 'Search for content'}
				</h2>
				<button class="text-sm text-blue-600 hover:underline dark:bg-gray-800 dark:text-[#58a6ff]"
					>Search tips</button
				>
			</div>

			<!-- Search Results -->
			{#if loading}
				<div class="flex justify-center p-8">
					<LoadingSpinner size="lg" />
				</div>
			{:else if searchResults.length > 0}
				<SearchResults results={searchResults} />

				<Pagination {currentPage} {totalPages} onPageChange={handlePageChange} />
			{:else if searchPerformed}
				<div class="py-8 text-center text-gray-500 dark:text-[#768390]">
					<p>No results found for "{searchQuery}"</p>
					<p class="mt-2 text-sm">Try different keywords or check your spelling</p>
				</div>
			{:else}
				<div class="py-8 text-center text-gray-500 dark:text-[#768390]">
					<p>Enter a search query to find content</p>
				</div>
			{/if}
		</div>
	</div>
</div>
