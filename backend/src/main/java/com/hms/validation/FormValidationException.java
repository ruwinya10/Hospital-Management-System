package com.hms.validation;
import java.util.*;
public class FormValidationException extends RuntimeException {
 public final Map<String,String> errors;
 public FormValidationException(Map<String,String> errors){super("Please correct the highlighted fields.");this.errors=new LinkedHashMap<>(errors);}
}
