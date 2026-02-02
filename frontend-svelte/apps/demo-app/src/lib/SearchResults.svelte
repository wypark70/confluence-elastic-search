<script lang="ts">
	import { Home, FileText } from 'lucide-svelte';

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

	interface Props {
		results: SearchResult[];
	}

	let { results }: Props = $props();

	function getIconClass(type: SearchResult['type']) {
		switch (type) {
			case 'home':
				return 'bg-yellow-600 dark:bg-[#d29922]';
			default:
				return 'bg-blue-600 dark:bg-[#0969da]';
		}
	}

	function getIcon(type: SearchResult['type']) {
		return type === 'home' ? Home : FileText;
	}

	function highlightText(text: string, queries: string[]): string {
		let highlighted = text;
		queries.forEach((query) => {
			const regex = new RegExp(`(${query})`, 'gi');
			highlighted = highlighted.replace(regex, '<mark>$1</mark>');
		});
		return highlighted;
	}
</script>

<div class="space-y-4">
	{#each results as result}
		<a href={result.url || '#'} class="group block">
			<div
				class="flex items-start gap-5 rounded-lg border border-gray-200 bg-white p-6 shadow-sm transition-colors duration-200 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:hover:bg-gray-700"
			>
				<!-- Icon -->
				<div class="mt-1 flex-shrink-0">
					<div
						class="flex h-12 w-12 items-center justify-center rounded-lg {getIconClass(
							result.type
						)} text-white shadow-sm"
					>
						{#if result.type === 'home'}
							<Home size={24} />
						{:else}
							<FileText size={24} />
						{/if}
					</div>
				</div>

				<!-- Content -->
				<div class="min-w-0 flex-1">
					<div class="mb-2 flex items-baseline justify-between">
						<h3
							class="truncate pr-4 text-xl font-semibold text-blue-600 group-hover:underline dark:text-blue-400"
						>
							{result.title}
						</h3>
						<span class="flex-shrink-0 text-sm whitespace-nowrap text-gray-500 dark:text-gray-400">
							{result.date}
						</span>
					</div>

					<div class="mb-3 flex items-center text-sm text-gray-500 dark:text-gray-400">
						<span class="font-medium text-gray-700 dark:text-gray-300">{result.space}</span>
						{#if result.type !== 'home'}
							<span class="mx-2 text-gray-300 dark:text-gray-600">•</span>
							<span class="capitalize">{result.type}</span>
						{/if}
					</div>

					<p class="line-clamp-2 text-base leading-relaxed text-gray-600 dark:text-gray-300">
						{@html highlightText(result.snippet, result.highlightValues)}
					</p>
				</div>
			</div>
		</a>
	{/each}
</div>
