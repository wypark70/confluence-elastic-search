<script lang="ts">
	interface Props {
		size?: 'sm' | 'md' | 'lg';
		color?: string;
	}

	let { size = 'md', color = 'border-blue-500' }: Props = $props();

	const sizeClasses = {
		sm: 'h-4 w-4 border-2',
		md: 'h-8 w-8 border-4',
		lg: 'h-12 w-12 border-8'
	};
</script>

<div 
	class="{sizeClasses[size]} animate-spin rounded-full {color} border-t-transparent"
	aria-label="Loading..."
></div>