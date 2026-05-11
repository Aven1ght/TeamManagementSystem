package ua.rud.teammanagementsystem.Exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
public ResponseEntity<String> handle(NotFoundException e){
    return ResponseEntity.status(404).body(e.getMessage());
}
@ExceptionHandler(BadRequest.class)
    public ResponseEntity<String> handle(BadRequest e){
        return ResponseEntity.status(400).body(e.getMessage());
}
@ExceptionHandler(ConflictRequest.class)
    public ResponseEntity<String> handle(ConflictRequest e){
        return ResponseEntity.status(409).body(e.getMessage());
}

}
