package searchengine.services;

import searchengine.error.IndexingResponse;

public interface IndexingService {
    IndexingResponse getStartIndexingSites();
    IndexingResponse getStopIndexingSites();
    IndexingResponse addUpdatePage(String url);

}
