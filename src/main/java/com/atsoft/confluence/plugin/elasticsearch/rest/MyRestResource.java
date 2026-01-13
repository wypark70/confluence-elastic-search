package com.atsoft.confluence.plugin.elasticsearch.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

// XML에 설정한 path와 일치해야 함
@Path("/feedback")
public class MyRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveFeedback(FeedbackModel data) {

        // 실제 비즈니스 로직 (예: DB 저장, 이메일 발송 등)
        System.out.println("User Feedback Received: " + data.getMessage());

        return Response.ok("{\"status\":\"success\"}").build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("q") String query, @QueryParam("page") @DefaultValue("1") int page, @QueryParam("size") @DefaultValue("10") int size) {
        // Mock search results for demo
        List<SearchResult> allResults = Arrays.asList(
            new SearchResult(1, "page", "A quick look at the editor (step 2 of 9)", "Demonstration Space", "Mar 05, 2024",
                "Let's start with the editor. You'll use the Confluence editor to create and edit pages. You can type in the editor as you would in any document, apply...",
                Arrays.asList("quick"), "/pages/viewpage.action?pageId=123"),
            new SearchResult(2, "home", "Welcome to Confluence", "Demonstration Space", "Mar 05, 2024",
                "welcome.png With Confluence it is easy to create, edit and share content with your team. Choose a topic below to start learning how. What is...",
                Arrays.asList(), "/pages/viewpage.action?pageId=456"),
            new SearchResult(3, "page", "Getting started with Confluence", "Demonstration Space", "Mar 06, 2024",
                "This guide will help you get started with Confluence. Learn how to create pages, add content, and collaborate with your team.",
                Arrays.asList(), "/pages/viewpage.action?pageId=789"),
            new SearchResult(4, "blog", "New features in Confluence", "Demonstration Space", "Mar 07, 2024",
                "Exciting new features have been added to Confluence. Check out the latest updates and improvements.",
                Arrays.asList(), "/pages/viewpage.action?pageId=101"),
            new SearchResult(5, "page", "Best practices for documentation", "Demonstration Space", "Mar 08, 2024",
                "Learn the best practices for creating effective documentation in Confluence. Tips and tricks included.",
                Arrays.asList(), "/pages/viewpage.action?pageId=202"),
            new SearchResult(6, "attachment", "Project Roadmap.pdf", "Demonstration Space", "Mar 09, 2024",
                "Detailed project roadmap document outlining milestones and deliverables.",
                Arrays.asList(), "/download/attachments/303/Project_Roadmap.pdf"),
            new SearchResult(7, "page", "Team collaboration guidelines", "Demonstration Space", "Mar 10, 2024",
                "Guidelines for effective team collaboration using Confluence tools and features.",
                Arrays.asList(), "/pages/viewpage.action?pageId=404"),
            new SearchResult(8, "home", "Documentation Hub", "Documentation Space", "Mar 11, 2024",
                "Central hub for all documentation related to our products and services.",
                Arrays.asList(), "/pages/viewpage.action?pageId=505")
        );

        // Filter by query if provided
        List<SearchResult> filteredResults = query != null && !query.trim().isEmpty()
            ? allResults.stream()
                .filter(result ->
                    result.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    result.getSnippet().toLowerCase().contains(query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList())
            : allResults;

        // Apply pagination
        int totalResults = filteredResults.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalResults);
        List<SearchResult> paginatedResults = startIndex < totalResults
            ? filteredResults.subList(startIndex, endIndex)
            : java.util.Collections.emptyList();

        // Create response with pagination info
        SearchResponse response = new SearchResponse(paginatedResults, totalResults, page, size);

        return Response.ok(response).build();
    }

    // JSON 매핑용 내부 모델 클래스
    @XmlRootElement
    public static class FeedbackModel {
        @XmlElement
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @XmlRootElement
    public static class SearchResult {
        @XmlElement
        private int id;
        @XmlElement
        private String type;
        @XmlElement
        private String title;
        @XmlElement
        private String space;
        @XmlElement
        private String date;
        @XmlElement
        private String snippet;
        @XmlElement
        private List<String> highlightValues;
        @XmlElement
        private String url;

        public SearchResult() {}

        public SearchResult(int id, String type, String title, String space, String date, String snippet, List<String> highlightValues, String url) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.space = space;
            this.date = date;
            this.snippet = snippet;
            this.highlightValues = highlightValues;
            this.url = url;
        }

        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getSpace() { return space; }
        public String getDate() { return date; }
        public String getSnippet() { return snippet; }
        public List<String> getHighlightValues() { return highlightValues; }
        public String getUrl() { return url; }

        // Setters
        public void setId(int id) { this.id = id; }
        public void setType(String type) { this.type = type; }
        public void setTitle(String title) { this.title = title; }
        public void setSpace(String space) { this.space = space; }
        public void setDate(String date) { this.date = date; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public void setHighlightValues(List<String> highlightValues) { this.highlightValues = highlightValues; }
        public void setUrl(String url) { this.url = url; }
    }

    @XmlRootElement
    public static class SearchResponse {
        @XmlElement
        private List<SearchResult> results;
        @XmlElement
        private int totalResults;
        @XmlElement
        private int currentPage;
        @XmlElement
        private int pageSize;
        @XmlElement
        private int totalPages;

        public SearchResponse() {}

        public SearchResponse(List<SearchResult> results, int totalResults, int currentPage, int pageSize) {
            this.results = results;
            this.totalResults = totalResults;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) totalResults / pageSize);
        }

        // Getters
        public List<SearchResult> getResults() { return results; }
        public int getTotalResults() { return totalResults; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }

        // Setters
        public void setResults(List<SearchResult> results) { this.results = results; }
        public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}