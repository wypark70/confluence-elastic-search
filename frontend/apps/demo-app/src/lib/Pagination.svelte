<script lang="ts">
	interface Props {
		currentPage: number;
		totalPages: number;
		onPageChange: (page: number) => void;
	}

	let { currentPage, totalPages, onPageChange }: Props = $props();
</script>

{#if totalPages > 1}
	<div class="mt-8 flex justify-center">
		<div class="flex space-x-2">
			<button
				class="px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed dark:bg-gray-700 dark:border-[#2c333a] dark:hover:bg-[#21262d]"
				disabled={currentPage === 1}
				onclick={() => onPageChange(currentPage - 1)}
			>
				Previous
			</button>

			{#each Array(totalPages) as _, i}
				{#if i + 1 === currentPage || i + 1 === currentPage - 1 || i + 1 === currentPage + 1 || i + 1 === 1 || i + 1 === totalPages}
					<button
						class="px-3 py-2 text-sm border rounded-md disabled:opacity-50 disabled:cursor-not-allowed {i + 1 === currentPage ? 'bg-blue-600 text-white border-blue-600' : 'border-gray-300 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-700 dark:border-[#2c333a] dark:hover:bg-[#21262d]'}"
						disabled={i + 1 === currentPage}
						onclick={() => onPageChange(i + 1)}
					>
						{i + 1}
					</button>
				{:else if i + 1 === currentPage - 2 || i + 1 === currentPage + 2}
					<span class="px-2 py-2 text-sm text-gray-500">...</span>
				{/if}
			{/each}

			<button
				class="px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed dark:border-[#2c333a] dark:hover:bg-[#21262d]"
				disabled={currentPage === totalPages}
				onclick={() => onPageChange(currentPage + 1)}
			>
				Next
			</button>
		</div>
	</div>
{/if}