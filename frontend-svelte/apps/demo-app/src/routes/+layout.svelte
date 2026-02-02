<script lang="ts">
	import { page } from '$app/state';
	import { onMount } from 'svelte';
	import { locales, localizeHref } from '@elastic-search/shared/i18n/runtime';
	import favicon from '$lib/assets/favicon.svg';
	import { theme } from '$lib/stores/theme.svelte';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { Sidebar, SidebarGroup, SidebarItem, SidebarWrapper } from 'flowbite-svelte';
	import { Folder, User, Image, Calendar, Tag, LayoutGrid, ChevronDown } from 'lucide-svelte';
	import './+layout.css';

	let { children } = $props();

	onMount(() => {
		theme.init();
	});

	const filters = [
		{ label: 'Space', icon: Folder },
		{ label: 'Contributor', icon: User },
		{ label: 'Type', icon: Image },
		{ label: 'Date', icon: Calendar },
		{ label: 'Label', icon: Tag },
		{ label: 'Space category', icon: LayoutGrid }
	];
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<div class="flex h-screen overflow-hidden bg-gray-50 dark:bg-gray-900">
	<!-- Sidebar -->
	<Sidebar class="border-r border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-800">
		<SidebarWrapper class="flex h-full flex-col">
			<SidebarGroup>
				<div
					class="mb-4 px-2 text-xs font-semibold tracking-wider text-gray-500 uppercase dark:text-gray-400"
				>
					Filter By
				</div>
				{#each filters as filter}
					<SidebarItem label={filter.label} class="hover:bg-gray-100 dark:hover:bg-gray-700">
						{#snippet icon()}
							<div
								class="text-gray-500 transition duration-75 group-hover:text-gray-900 dark:text-gray-400 dark:group-hover:text-white"
							>
								<filter.icon size={20} />
							</div>
						{/snippet}
						{#snippet subtext()}
							<ChevronDown size={14} class="text-gray-400" />
						{/snippet}
					</SidebarItem>
				{/each}
			</SidebarGroup>

			<SidebarGroup class="mt-auto border-t border-gray-200 pt-4 dark:border-gray-700">
				<SidebarItem
					label="Advanced search"
					href="#"
					class="text-blue-600 hover:text-blue-800 dark:text-blue-500 dark:hover:text-blue-400"
				></SidebarItem>

				<!-- Language Switcher (Hidden but kept for logic) -->
				<div style="display:none">
					{#each locales as locale}
						<a href={localizeHref(page.url.pathname, { locale })}>
							{locale}
						</a>
					{/each}
				</div>
			</SidebarGroup>

			<div class="mt-4 px-2">
				<ThemeToggle />
			</div>
		</SidebarWrapper>
	</Sidebar>

	<!-- Main Content Area -->
	<div class="relative flex min-w-0 flex-1 flex-col overflow-hidden" id="demo-app-root">
		{@render children()}
	</div>
</div>
