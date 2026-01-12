<script lang="ts">
	import {
		X,
		Folder,
		User,
		Image,
		Calendar,
		Tag,
		LayoutGrid,
		FileText,
		Home,
		ChevronDown
	} from 'lucide-svelte';

	// Mock data to match the screenshot exactly
	const results = [
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
		},
		{
			id: 3,
			type: 'page',
			title: 'What is Confluence? (step 1 of 9)',
			space: 'Demonstration Space',
			date: 'Mar 05, 2024',
			snippet:
				'Welcome to Confluence Welcome to Confluence A quick look at the editor (step 2 of 9)',
			highlightValues: ['quick']
		},
		{
			id: 4,
			type: 'page',
			title: "Let's edit this page (step 3 of 9)",
			space: 'Demonstration Space',
			date: 'Mar 05, 2024',
			snippet:
				'to the space home. A quick look at the editor (step 2 of 9) Welcome to Confluence Prettify the page with an image',
			highlightValues: ['quick']
		}
	];

	const filters = [
		{ label: 'Space', icon: Folder },
		{ label: 'Contributor', icon: User },
		{ label: 'Type', icon: Image }, // Using Image as a proxy for "Type" or generic media
		{ label: 'Date', icon: Calendar },
		{ label: 'Label', icon: Tag },
		{ label: 'Space category', icon: LayoutGrid }
	];

	function highlightText(text: string, queries: string[]) {
		// Basic highlight replacement for demo purposes.
		// In a real app, this would be more robust.
		let highlighted = text;
		queries.forEach((q) => {
			const regex = new RegExp(`(${q})`, 'gi');
			highlighted = highlighted.replace(
				regex,
				'<span class="font-bold text-blue-600 dark:text-white">$1</span>'
			);
		});
		return highlighted;
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
			<!-- search Bar -->
			<div class="flex items-center justify-between border-b border-gray-200 dark:border-[#2c333a]">
				<div class="relative flex-1">
					<input
						type="text"
						value="quick"
						class="w-full border-none bg-transparent text-2xl text-gray-900 placeholder-gray-400 focus:ring-0 dark:text-[#c9d1d9] dark:placeholder-[#484f58]"
						placeholder="Search"
					/>
					<button
						class="absolute top-1/2 right-0 -translate-y-1/2 text-gray-500 dark:bg-gray-800 dark:text-[#c9d1d9] dark:hover:bg-gray-700"
					>
						<X size={24} />
					</button>
					<div class="absolute bottom-0 left-0 h-[2px] w-full bg-blue-500 dark:bg-[#58a6ff]"></div>
				</div>
			</div>

			<div class="mb-6 flex items-end justify-between">
				<h2 class="text-lg text-gray-600 dark:text-[#768390]">4 search results</h2>
				<button
					class="text-sm text-blue-600 hover:underline dark:bg-gray-800 dark:text-[#58a6ff] dark:hover:bg-gray-700"
					>Search tips</button
				>
			</div>

			<div class="space-y-6">
				{#each results as result}
					<div class="group flex gap-4">
						<!-- Icon -->
						<div class="mt-1 flex-shrink-0">
							{#if result.type === 'home'}
								<div
									class="flex h-8 w-8 items-center justify-center rounded bg-yellow-600 dark:bg-[#d29922]"
								>
									<Home size={18} class="text-white" />
								</div>
							{:else}
								<div
									class="flex h-8 w-8 items-center justify-center rounded bg-blue-600 dark:bg-[#0969da]"
								>
									<FileText size={18} class="text-white" />
								</div>
							{/if}
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
		</div>
	</div>
</div>
