package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentStartResponse {
  
  @JsonProperty("ReCalc")
  private Integer reCalc;

  @JsonProperty("CustomerToken")
  private String customerToken;

  @JsonProperty("CustomerReceiptToken")
  private String customerReceiptToken;
  
  public PaymentStartResponse() {
  }
  
  public PaymentStartResponse(Integer reCalc, String customerToken, String customerReceiptToken) {
    super();
    this.reCalc = reCalc;
    this.customerToken = customerToken;
    this.customerReceiptToken = customerReceiptToken;
  }

  /**
   * 0 – normal usage.
   * 1 – recalculation must be executed and payment updated
   * 
   * @return reCalc
   */
  public Integer getReCalc() {
    return reCalc;
  }
  
  public void setReCalc(Integer reCalc) {
    this.reCalc = reCalc;
  }
  
  /**
   * null if no recalculation is needed yet. 
   * 
   * CustomerToken from merchant's Loyalty program to be used to recalculate Payment Amount.
   * 
   * @return customerToken
   */
  public String getCustomerToken() {
    return customerToken;
  }
  
  public void setCustomerToken(String customerToken) {
    this.customerToken = customerToken;
  }
  
  /**
   * Used for customer receipt token (In DK: Service agreement with Storebox implies that Storebox user Id is provided).
   * 
   * May be null.
   * Max 32 characters.
   * 
   * @return customerReceiptToken
   */
  public String getCustomerReceiptToken() {
    return customerReceiptToken;
  }
  
  public void setCustomerReceiptToken(String customerReceiptToken) {
    this.customerReceiptToken = customerReceiptToken;
  }
  
}
