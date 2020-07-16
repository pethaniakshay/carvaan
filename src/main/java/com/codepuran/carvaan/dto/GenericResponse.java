package com.codepuran.carvaan.dto;

import com.codepuran.carvaan.exception.ApiSubError;
import com.codepuran.carvaan.exception.ApiValidationError;
import com.codepuran.carvaan.exception.LowerCaseClassNameResolver;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"status", "message", "data", "subErrors"})
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class GenericResponse<T> {

  @Builder.Default
  private int status = HttpStatus.OK.value();

  private T data;

  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String path;

  private String trackingId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ApiSubError> subErrors;

  public void addSubError(ApiSubError subError) {
    if (subErrors == null) {
      subErrors = new ArrayList<>();
    }
    subErrors.add(subError);
  }

  public void addValidationError(
          String object, String field, Object rejectedValue, String message) {
    addSubError(new ApiValidationError(object, field, rejectedValue, message));
  }

  public void addValidationError(String object, String message) {
    addSubError(new ApiValidationError(object, message));
  }

  public void addValidationError(FieldError fieldError) {
    this.addValidationError(
            fieldError.getObjectName(),
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage());
  }

  public void addValidationErrors(List<FieldError> fieldErrors) {
    fieldErrors.forEach(this::addValidationError);
  }

  public void addValidationError(ObjectError objectError) {
    this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
  }

  public void addValidationError(List<ObjectError> globalErrors) {
    globalErrors.forEach(this::addValidationError);
  }
}