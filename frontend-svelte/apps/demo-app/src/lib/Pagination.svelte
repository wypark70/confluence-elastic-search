<script lang="ts">
	import { PaginationNav } from 'flowbite-svelte';
	import { ChevronLeftOutline, ChevronRightOutline } from 'flowbite-svelte-icons';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';

	interface Props {
		currentPage: number;
		totalPages: number;
	}

	let { currentPage, totalPages }: Props = $props();

	// Generate href for a page
	function getHref(pageNum: number) {
		const url = new URL(page.url);
		url.searchParams.set('page', pageNum.toString());
		return url.toString();
	}

	function handlePageChange(pageNum: number) {
		goto(getHref(pageNum), { keepFocus: true });
	}
</script>

{#if totalPages > 1}
	<PaginationNav {currentPage} {totalPages} onPageChange={handlePageChange}>
		{#snippet prevContent()}
			<span class="sr-only">Previous</span>
			<ChevronLeftOutline class="h-10 w-10" />
		{/snippet}
		{#snippet nextContent()}
			<span class="sr-only">Next</span>
			<ChevronRightOutline class="h-10 w-10" />
		{/snippet}
	</PaginationNav>
{/if}
