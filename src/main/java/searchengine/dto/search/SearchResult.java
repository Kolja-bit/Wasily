package searchengine.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
        private boolean result;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private int count;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private List<SearchResultQuery> data;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private volatile String error;

}
