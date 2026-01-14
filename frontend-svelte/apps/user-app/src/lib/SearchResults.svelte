<script lang="ts">
	import { Home, FileText } from 'lucide-svelte';

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

<div class="space-y-6">
	{#each results as result}
		<div class="group flex gap-4">
			<!-- Icon -->
			<div class="mt-1 flex-shrink-0">
				<div class="flex h-8 w-8 items-center justify-center rounded {getIconClass(result.type)}">
					{#if result.type === 'home'}
						<Home size={18} class="text-white" />
					{:else}
						<FileText size={18} class="text-white" />
					{/if}
				</div>
			</div>

			<!-- Content -->
			<div class="flex-1">
				<h3
					class="cursor-pointer text-lg leading-snug font-medium text-blue-600 hover:underline dark:text-[#58a6ff]"
				>
					{result.title}
				</h3>
				<div class="mt-0.5 mb-1 text-xs text-gray-500 dark:text-[#768390]">
					<span class="text-gray-700 dark:text-[#c9d1d9]">{result.space}</span>
					<span class="mx-2">·</span>
					<span>{result.date}</span>
				</div>
				<p class="text-sm leading-relaxed text-gray-600 dark:text-[#8b949e]">
					{@html highlightText(result.snippet, result.highlightValues)}
				</p>
			</div>
		</div>
	{/each}
</div>
