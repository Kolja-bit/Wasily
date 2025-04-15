package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import searchengine.error.ErrorModel;
import searchengine.error.IndexingResponse;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<IndexingResponse> handleIllegalArgumentException(Exception ex) {
        IndexingResponse indexingResponse=new IndexingResponse();
        indexingResponse.setResult(false);
        ErrorModel modelAndView=new ErrorModel();
        modelAndView.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        indexingResponse.setError(modelAndView.getStatusCode()+"  "+modelAndView.getReturnResponse());
        return ResponseEntity.badRequest().body(indexingResponse);
    }
}
