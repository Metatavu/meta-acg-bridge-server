package fi.metatavu.mobilepay.client;

import fi.metatavu.mobilepay.model.ErrorResponse;

public class MobilePayResponse<T> {

  private int status;
  private String message;
  private T response;
  private ErrorResponse error;

  public MobilePayResponse(int status, String message, T response, ErrorResponse error) {
    this.status = status;
    this.response = response;
    this.message = message;
    this.error = error;
  }

  public T getResponse() {
    return response;
  }
  
  public ErrorResponse getError() {
    return error;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public boolean isOk() {
    return status >= 200 && status <= 299;
  }
}
