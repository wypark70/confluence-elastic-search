<script lang="ts">
	import { Spinner } from 'flowbite-svelte';

	type SpinnerColor =
		| 'primary'
		| 'secondary'
		| 'gray'
		| 'red'
		| 'orange'
		| 'amber'
		| 'yellow'
		| 'lime'
		| 'green'
		| 'emerald'
		| 'teal'
		| 'cyan'
		| 'sky'
		| 'blue'
		| 'indigo'
		| 'violet'
		| 'purple'
		| 'fuchsia'
		| 'pink'
		| 'rose';

	interface Props {
		size?: 'sm' | 'md' | 'lg';
		color?: SpinnerColor;
	}

	let { size = 'md', color = 'blue' }: Props = $props();

	// Map sizes to Flowbite Spinner sizes (which are strings "1" to "12" or specific tailwind classes)
	// Flowbite Spinner size usage: size="8" (w-8 h-8)
	const spinnerSize = $derived(size === 'sm' ? '4' : size === 'md' ? '8' : '12');
</script>

<Spinner size={spinnerSize} {color} />
