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
