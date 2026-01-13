export function formatDate(dateString: string): string {
	const date = new Date(dateString);
	return date.toLocaleDateString('en-US', {
		year: 'numeric',
		month: 'short',
		day: 'numeric'
	});
}

export function truncateText(text: string, maxLength: number = 200): string {
	if (text.length <= maxLength) return text;
	return text.substring(0, maxLength).trim() + '...';
}

export function highlightText(text: string, queries: string[]): string {
	let highlighted = text;
	queries.forEach((query) => {
		const regex = new RegExp(`(${query})`, 'gi');
		highlighted = highlighted.replace(regex, '<mark>$1</mark>');
	});
	return highlighted;
}

export function debounce<T extends (...args: any[]) => void>(
	func: T,
	wait: number
): (...args: Parameters<T>) => void {
	let timeout: ReturnType<typeof setTimeout>;
	return (...args: Parameters<T>) => {
		clearTimeout(timeout);
		timeout = setTimeout(() => func(...args), wait);
	};
}
