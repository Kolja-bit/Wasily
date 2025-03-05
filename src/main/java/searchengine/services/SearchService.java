package searchengine.services;

import searchengine.dto.search.SearchResult;

public interface SearchService {
    SearchResult getSearch(String query, String site, int offset, int limit);
}
