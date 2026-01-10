export interface SearchResult {
	id: number;
	type: 'page' | 'home' | 'blog' | 'attachment';
	title: string;
	space: string;
	date: string;
	snippet: string;
	highlightValues: string[];
	url?: string;
}

export interface SearchFilters {
	space?: string;
	contributor?: string;
	type?: string;
	date?: string;
	label?: string;
	spaceCategory?: string;
}

export interface SearchRequest {
	query: string;
	filters?: SearchFilters;
	page?: number;
	pageSize?: number;
}

export interface SearchResponse {
	results: SearchResult[];
	totalCount: number;
	pageSize: number;
	currentPage: number;
	totalPages: number;
	query: string;
	filters?: SearchFilters;
}