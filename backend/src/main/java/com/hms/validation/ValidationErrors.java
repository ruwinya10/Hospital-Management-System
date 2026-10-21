package com.hms.validation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@RestControllerAdvice public class ValidationErrors {
 @ExceptionHandler(FormValidationException.class) public ResponseEntity<?> form(FormValidationException ex){return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage(),"errors",ex.errors));}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> bean(MethodArgumentNotValidException ex){Map<String,String> errors=new LinkedHashMap<>();ex.getBindingResult().getFieldErrors().forEach(e->errors.putIfAbsent(e.getField(),e.getField()+": "+e.getDefaultMessage()));return ResponseEntity.badRequest().body(Map.of("message","Please check the highlighted input values.","errors",errors));}
 @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<?> malformed(HttpMessageNotReadableException ex){return ResponseEntity.badRequest().body(Map.of("message","Invalid request. Check that numbers, dates and text have the correct format."));}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(DataIntegrityViolationException ex){return ResponseEntity.status(409).body(Map.of("message","This record conflicts with existing data. Check for duplicate identifiers and try again."));}
}
