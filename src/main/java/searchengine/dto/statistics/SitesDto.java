package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.SiteIndexingStatus;


import java.time.LocalDateTime;
@Getter
@Setter
public class SitesDto {
    private Integer id;
    private SiteIndexingStatus status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;
}
