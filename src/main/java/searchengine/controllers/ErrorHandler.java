package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import searchengine.error.ModelAndView;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    //public ResponseEntity<IndexingResponse> handleIllegalArgumentException(Exception ex) {
        public ResponseEntity<ModelAndView> handleIllegalArgumentException(Exception ex) {
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        modelAndView.setMessage(ex.getMessage());
        //IndexingResponse indexingResponse=new IndexingResponse();
        //indexingResponse.setResult(false);
        //indexingResponse.setError(ex.getMessage());
        //return ResponseEntity.badRequest().body(indexingResponse);
        //return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ex.getMessage());
        //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(indexingResponse);
        //return new ResponseEntity<>(new ModelAndView(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.badRequest().body(modelAndView);
    }
}
