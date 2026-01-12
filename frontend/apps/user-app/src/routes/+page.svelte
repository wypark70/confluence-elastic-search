<script lang="ts">
	import SearchInput from '../lib/SearchInput.svelte';
	import SearchResults from '../lib/SearchResults.svelte';
	import LoadingSpinner from '../lib/LoadingSpinner.svelte';
	interface SearchResult {
		id: number;
		type: 'page' | 'home' | 'blog' | 'attachment';
		title: string;
		space: string;
		date: string;
		snippet: string;
		highlightValues: string[];
		url?: string;
	}

	let searchQuery = '';
	let searchResults: SearchResult[] = [
		{
			id: 1,
			type: 'page',
			title: 'A quick look at the editor (step 2 of 9)',
			space: 'Demonstration Space',
			date: 'Mar 05, 2024',
			snippet:
				"Let's start with the editor. You'll use the Confluence editor to create and edit pages. You can type in the editor as you would in any document, apply...",
			highlightValues: ['quick']
		},
		{
			id: 2,
			type: 'home',
			title: 'Welcome to Confluence',
			space: 'Demonstration Space',
			date: 'Mar 05, 2024',
			snippet:
				'welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...',
			highlightValues: []
		}
	];
	let loading = false;
	let searchPerformed = false;

	async function handleSearch(query: string) {
		searchQuery = query;

		if (!query.trim()) {
			searchResults = [];
			searchPerformed = false;
			return;
		}

		loading = true;
		searchPerformed = true;

		try {
			// Simulate API delay
			await new Promise((resolve) => setTimeout(resolve, 500));

			// Mock filter results
			const mockResults = [
				{
					id: 1,
					type: 'page' as const,
					title: 'A quick look at the editor (step 2 of 9)',
					space: 'Demonstration Space',
					date: 'Mar 05, 2024',
					snippet:
						"Let's start with the editor. You'll use the Confluence editor to create and edit pages. You can type in the editor as you would in any document, apply...",
					highlightValues: ['quick']
				},
				{
					id: 2,
					type: 'home' as const,
					title: 'Welcome to Confluence',
					space: 'Demonstration Space',
					date: 'Mar 05, 2024',
					snippet:
						'welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...',
					highlightValues: []
				}
			];

			const filtered = mockResults.filter(
				(result) =>
					result.title.toLowerCase().includes(query.toLowerCase()) ||
					result.snippet.toLowerCase().includes(query.toLowerCase())
			);

			searchResults = filtered;
		} catch (error) {
			console.error('Search failed:', error);
			searchResults = [];
		} finally {
			loading = false;
		}
	}
</script>

<div
	class="flex min-h-screen flex-col bg-white font-sans text-gray-900 dark:bg-[#1c2128] dark:text-[#9fadbc]"
>
	<div class="flex flex-1 overflow-hidden">
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
						? `${searchResults.length} search result${searchResults.length !== 1 ? 's' : ''}`
						: '4 search results'}
				</h2>
				<button class="text-sm text-blue-600 hover:underline dark:text-[#58a6ff]"
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
			{:else if searchPerformed}
				<div class="py-8 text-center text-gray-500 dark:text-[#768390]">
					<p>No results found for "{searchQuery}"</p>
					<p class="mt-2 text-sm">Try different keywords or check your spelling</p>
				</div>
			{:else}
				<SearchResults results={searchResults} />
			{/if}
		</div>
	</div>
</div>
