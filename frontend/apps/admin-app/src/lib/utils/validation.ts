export function validateEmail(email: string): boolean {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	return emailRegex.test(email);
}

export function validateSearchQuery(query: string): {
	isValid: boolean;
	error?: string;
} {
	if (!query || query.trim().length === 0) {
		return { isValid: false, error: 'Search query is required' };
	}

	if (query.length < 2) {
		return { isValid: false, error: 'Search query must be at least 2 characters' };
	}

	if (query.length > 500) {
		return { isValid: false, error: 'Search query is too long (max 500 characters)' };
	}

	return { isValid: true };
}

export function sanitizeSearchQuery(query: string): string {
	return query
		.trim()
		.replace(/[<>]/g, '') // Remove potential HTML tags
		.replace(/['"]/g, '') // Remove quotes that could cause issues
		.substring(0, 500);
}