package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.SiteStatusModel;


import java.time.LocalDateTime;
@Getter
@Setter
public class SitesDto {
    private Integer id;
    private SiteStatusModel status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;
}
