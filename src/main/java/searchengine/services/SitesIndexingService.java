package searchengine.services;

import searchengine.error.IndexingResponse;

public interface SitesIndexingService {
    IndexingResponse getStartIndexingSites();

    IndexingResponse getStopIndexingSites();

    IndexingResponse addUpdatePage(String url);

}
