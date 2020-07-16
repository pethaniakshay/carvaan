package com.codepuran.carvaan.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomBusinessException extends RuntimeException {

  @Getter
  @Setter
  private int status;

  @Getter
  @Setter
  @Builder.Default
  private boolean triggerSentryEvent = false;

  public CustomBusinessException(String message) {
    super(message);
  }

  public CustomBusinessException(String message, Throwable cause) {
    super(message, cause);
  }

  public CustomBusinessException(String message, HttpStatus status) {
    super(message);
    this.status = status.value();
  }

  public CustomBusinessException(String message, Throwable cause, HttpStatus status) {
    super(message, cause);
    this.status = status.value();
  }


}