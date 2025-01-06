package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.error.IndexingResponse;
import searchengine.services.SitesIndexingServiceImpl;
import searchengine.services.StatisticsService;
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SitesIndexingServiceImpl indexingService ;


    public ApiController(StatisticsService statisticsService, SitesIndexingServiceImpl indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService=indexingService;
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

}
