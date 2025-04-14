package searchengine.error;

import lombok.Data;

@Data
//@AllArgsConstructor
public class ErrorModel {
    private int StatusCode;
    private String message;
}
