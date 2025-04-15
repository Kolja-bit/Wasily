package searchengine.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


@Data
public class IndexingResponse {
    private volatile boolean result;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private volatile String error;
}
