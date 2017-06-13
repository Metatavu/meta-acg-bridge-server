package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
  
  @JsonProperty("StatusCode")
  private int statusCode;
  
  @JsonProperty("StatusText")
  private String statusText;

  public int getStatusCode() {
    return statusCode;
  }
  
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
  
  public String getStatusText() {
    return statusText;
  }
  
  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }
  
}
