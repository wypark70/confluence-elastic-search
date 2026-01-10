import type { SearchResult, SearchRequest, SearchResponse } from '../types/search';

export class SearchApiClient {
	private baseUrl: string = '/rest/myplugin/1.0';

	async search(request: SearchRequest): Promise<SearchResponse> {
		const response = await fetch(`${this.baseUrl}/search`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-Atlassian-Token': 'no-check'
			},
			body: JSON.stringify(request)
		});

		if (!response.ok) {
			throw new Error(`Search failed: ${response.statusText}`);
		}

		return response.json();
	}

	async getSuggestions(query: string): Promise<string[]> {
		const response = await fetch(`${this.baseUrl}/suggestions?q=${encodeURIComponent(query)}`);
		if (!response.ok) {
			throw new Error(`Suggestions failed: ${response.statusText}`);
		}
		return response.json();
	}

	async getRecentSearches(userId?: string): Promise<SearchResult[]> {
		const url = userId 
			? `${this.baseUrl}/recent/${userId}`
			: `${this.baseUrl}/recent`;
		
		const response = await fetch(url);
		if (!response.ok) {
			throw new Error(`Recent searches failed: ${response.statusText}`);
		}
		return response.json();
	}
}

export const searchApi = new SearchApiClient();