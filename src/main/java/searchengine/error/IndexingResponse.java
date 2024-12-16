package searchengine.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Setter
//@Getter
//@AllArgsConstructor
@Data
public class IndexingResponse {
    private volatile boolean result;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private volatile String error;
}
