package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.error.IndexingResponse;
import searchengine.services.SearchService;
import searchengine.services.SitesIndexingService;
import searchengine.services.StatisticsService;
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SitesIndexingService indexingService;
    private final SearchService searchService;


    public ApiController(StatisticsService statisticsService,
                         SitesIndexingService indexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService=indexingService;
        this.searchService=searchService;

    }


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing(){
        return ResponseEntity.ok(indexingService.getStartIndexingSites());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        return ResponseEntity.ok(indexingService.getStopIndexingSites());
    }
    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url){
        return ResponseEntity.ok(indexingService.addUpdatePage(url));
    }
    //Делать метод контроллера для поискового класса SearchServiceImpl
    @GetMapping("/search")
    public ResponseEntity<IndexingResponse> search(@RequestParam(required = false) String query,
                                                   @RequestParam(required = false) String site,
                                                   @RequestParam(defaultValue = "0") int offset,
                                                   @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.getSearch(query,site,offset,limit));
    }
    /*@GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        SearchResults searchResults = searchService.search(query, site, offset, limit);
        return ResponseEntity.ok(searchResults);
    }*/

}
