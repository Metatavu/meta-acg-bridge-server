package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetCurrentPaymentResponse {

  private String posId;
  
  private String posUnitId;
  
  private Integer paymentStatus;

  private String orderId;
  
  private String transactionId;
  
  private Double amount;
  
  private String customerId;
  
  private String customerToken;
  
  private String customerReceiptToken;
  
  private String lastestUpdate;

  /**
   * Unique ID that identifies the PoS that has initiated current payment request.
   * 
   * @return point of sale id
   */
  @JsonProperty("PoSId")
  public String getPoSId() {
    return posId;
  }

  public void setPoSId(String posId) {
    this.posId = posId;
  }
  
  /**
   * White Box/Terminal Id
   * 
   * @return posUnitId
   */
  @JsonProperty ("PoSUnitId")
  public String getPosUnitId() {
    return posUnitId;
  }
  
  public void setPosUnitId(String posUnitId) {
    this.posUnitId = posUnitId;
  }
  
  /**
   * See status values below
   * 
   * 10 ('Idle') - No payment request in the queue
   * 20 ('Issued') - Payment request is sent to customer.
   * 30 ('AwaitCheckIn') - Await customer check-in.
   * 40 ('Cancel') - Customer has cancelled/rejected payment request.
   * 50 ('Error') - MobilePay is not able to handle the payment – the PoS should cancel the MobilePay payment request.
   * 60 (‘AwaitTokenRecalc’) - Await for PoS system to update payment after recalculation.
   * 80 ('PaymentAccepted') - The payment request is accepted by the customer - await payment confirmation from the payment transaction system.
   * 100 ('Done') - "Payment Confirmed" and TransactionId, PaymentSignature, Amount, CustomerId (optional) will contain a value.
   * 
   * @return payment statuss
   */
  @JsonProperty ("PaymentStatus")
  public Integer getPaymentStatus() {
    return paymentStatus;
  }
  
  public void setPaymentStatus(Integer paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
  
  /**
   * The OrderId assigned to current payment
   * 
   * @return orderId
   */
  @JsonProperty("OrderId")
  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }
  
  /**
   * Unique ID that identifies the payment (transaction ID). ID is generated by Danske Bank and is shown on the receipt inside the MobilePay app.
   * @return transaction id
   */
  @JsonProperty ("TransactionId")
  public String getTransactionId() {
    return transactionId;
  }
  
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
  
  /**
   * The amount for the payment.
   * 
   * @return amount
   */
  @JsonProperty ("Amount")
  public Double getAmount() {
    return amount;
  }
  
  public void setAmount(Double amount) {
    this.amount = amount;
  }
  
  /**
   * Unique ID of the customer. The ID is generated by Danske Bank.
   * 
   * @return customer id
   */
  @JsonProperty ("CustomerId")
  public String getCustomerId() {
    return customerId;
  }
  
  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  /**
   * Contains customer token if customer has checked-In with a merchant token ID related to this merchant’s loyalty program
   * 
   * @return customerToken
   */
  @JsonProperty ("CustomerToken")
  public String getCustomerToken() {
    return customerToken;
  }
  
  public void setCustomerToken(String customerToken) {
    this.customerToken = customerToken;
  }
  
  /**
   * Used for customer receipt token (In DK: Service agreement with Storebox implies that Storebox user Id is provided) 
   * 
   * Max 32 char.
   * 
   * @return customerReceiptToken
   */
  @JsonProperty ("CustomerReceiptToken")
  public String getCustomerReceiptToken() {
    return customerReceiptToken;
  }
  
  public void setCustomerReceiptToken(String customerReceiptToken) {
    this.customerReceiptToken = customerReceiptToken;
  }

  /**
   * Datetime where the Payment were last updated / changed / accessed
   * 
   * @return latest update
   */
  @JsonProperty ("LastestUpdate")
  public String getLastestUpdate() {
    return lastestUpdate;
  }
  
  public void setLastestUpdate(String lastestUpdate) {
    this.lastestUpdate = lastestUpdate;
  }
  
}