package com.hms.validation;
import java.nio.charset.StandardCharsets;
import java.util.Map;
public class AccountValidation {
 public static void username(String value){if(value==null||!value.matches("[A-Za-z0-9][A-Za-z0-9._-]{2,49}"))throw new FormValidationException(Map.of("username","Use 3–50 letters, numbers, dots, underscores or hyphens; start with a letter or number."));}
 public static void password(String value,String field){if(value==null||value.length()<12||value.length()>72||value.getBytes(StandardCharsets.UTF_8).length>72)throw new FormValidationException(Map.of(field,"Use 12–72 characters (at most 72 UTF-8 bytes)."));if(!value.matches("(?s).*[A-Za-z].*")||!value.matches("(?s).*[0-9].*"))throw new FormValidationException(Map.of(field,"Include at least one letter and one number in the password."));}
}
