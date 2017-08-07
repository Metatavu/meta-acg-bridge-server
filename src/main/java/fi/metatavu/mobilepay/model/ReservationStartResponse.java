package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationStartResponse {
  
  @JsonProperty("CustomerToken")
  private String customerToken;

  public ReservationStartResponse() {
  }
  
  public ReservationStartResponse(String customerToken) {
    super();
    this.customerToken = customerToken;
  }
  
  /**
   * Contains loyalty card number, if user is part of a loyalty program with the current Merchant, and has enrolled a loyalty card with MobilePay, the loyalty card
   * 
   * Null if not part of loyalty program.
   * 
   * @return customerToken
   */
  public String getCustomerToken() {
    return customerToken;
  }
  
  public void setCustomerToken(String customerToken) {
    this.customerToken = customerToken;
  }
  
}
