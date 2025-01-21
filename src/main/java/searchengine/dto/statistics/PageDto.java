package searchengine.dto.statistics;

import lombok.Data;


@Data
public class PageDto {
    private Integer id;
    private Integer siteId;
    private Integer code;
    private String content;
    private String path;


}
