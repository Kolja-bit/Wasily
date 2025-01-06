package searchengine.services;

import searchengine.error.IndexingResponse;

public interface SitesIndexingServiceImpl {
    IndexingResponse getStartIndexingSites();

    IndexingResponse getStopIndexingSites();

    IndexingResponse addUpdatePage(String url);

}
