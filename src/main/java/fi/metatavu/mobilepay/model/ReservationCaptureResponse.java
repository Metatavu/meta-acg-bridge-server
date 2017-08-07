package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationCaptureResponse {

  private String customerReceiptToken;
  
  /**
   * Used for customer receipt token (In DK: Service agreement with Storebox implies that Storebox user Id is provided).
   * 
   * May be null.
   * 
   * Max 32 characters.
   * 
   * @return customer receipt token
   */
  @JsonProperty ("CustomerReceiptToken")
  public String getCustomerReceiptToken() {
    return customerReceiptToken;
  }
  
  public void setCustomerReceiptToken(String customerReceiptToken) {
    this.customerReceiptToken = customerReceiptToken;
  }
  
}
