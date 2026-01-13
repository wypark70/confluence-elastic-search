function getSampleResponse(query = "") {
    return {
        results: [
            {
                "id": 1,
                "type": "page",
                "title": "A quick look at the editor (step 2 of 9)",
                "space": "Demonstration Space",
                "date": "Mar 05, 2024",
                "snippet": "Let's start with the editor. You'll use the Confluence editor to create and edit pages. You can type in the editor as you would in any document, apply...",
                "highlightValues": [query]
            },
            {
                "id": 2,
                "type": "home",
                "title": "Welcome to Confluence",
                "space": "Demonstration Space",
                "date": "Mar 05, 2024",
                "snippet": "welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...",
                "highlightValues": [query]
            },
            {
                "id": 3,
                "type": "home",
                "title": "Welcome to Confluence",
                "space": "Demonstration Space",
                "date": "Mar 05, 2024",
                "snippet": "welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...",
                "highlightValues": [query]
            },
            {
                "id": 4,
                "type": "home",
                "title": "Welcome to Confluence",
                "space": "Demonstration Space",
                "date": "Mar 05, 2024",
                "snippet": "welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...",
                "highlightValues": [query]
            },
            {
                "id": 5,
                "type": "home",
                "title": "Welcome to Confluence",
                "space": "Demonstration Space",
                "date": "Mar 05, 2024",
                "snippet": "welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...",
                "highlightValues": [query]
            }
        ].filter(result => result.title.includes(query) || result.snippet.includes(query) || result.space.includes(query)),
        totalResults: 15,
        currentPage: 2,
        pageSize: 5,
        totalPages: 3
    };
}

export default getSampleResponse;