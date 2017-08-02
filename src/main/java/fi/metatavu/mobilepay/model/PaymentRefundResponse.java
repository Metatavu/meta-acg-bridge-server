package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRefundResponse {

  private Double remainder;
  private String transactionId;
  
  @JsonProperty ("Remainder")
  public Double getRemainder() {
    return remainder;
  }
  
  public void setRemainder(Double remainder) {
    this.remainder = remainder;
  }
  
  @JsonProperty ("TransactionId")
  public String getTransactionId() {
    return transactionId;
  }
  
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
  
}