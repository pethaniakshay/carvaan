package com.codepuran.carvaan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"isError", "status", "message", "details"})
public class ExceptionResponse {

  @JsonProperty("isError")
  private boolean error;

  private String message;

  private int status;

  private String details;

  public ExceptionResponse(boolean isError, String message) {
    super();
    this.error = isError;
    this.message = message;
  }
}