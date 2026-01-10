export class ConfluenceApiClient {
	private baseUrl: string;

	constructor(baseUrl: string = '/rest/myplugin/1.0') {
		this.baseUrl = baseUrl;
	}

	async search(query: string, filters?: any) {
		const response = await fetch(`${this.baseUrl}/search`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-Atlassian-Token': 'no-check'
			},
			body: JSON.stringify({ query, filters })
		});

		if (!response.ok) {
			throw new Error(`Search failed: ${response.statusText}`);
		}

		return response.json();
	}

	async getUserData(userId: string) {
		const response = await fetch(`${this.baseUrl}/user/${userId}`);
		if (!response.ok) {
			throw new Error(`Failed to get user data: ${response.statusText}`);
		}
		return response.json();
	}

	async submitFeedback(message: string) {
		const response = await fetch(`${this.baseUrl}/feedback`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-Atlassian-Token': 'no-check'
			},
			body: JSON.stringify({ message })
		});

		if (!response.ok) {
			throw new Error(`Feedback submission failed: ${response.statusText}`);
		}

		return response.json();
	}
}

export const confluenceApi = new ConfluenceApiClient();