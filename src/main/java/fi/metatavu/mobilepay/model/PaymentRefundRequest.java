package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRefundRequest {

  private String merchantId;

  private String locationId;

  private String posId;

  private String orderId;

  private String amount;

  private String bulkRef;
  
  public PaymentRefundRequest() {
    super();
  }

  public PaymentRefundRequest(String merchantId, String locationId, String posId, String orderId, String amount,
      String bulkRef) {
    super();
    this.merchantId = merchantId;
    this.locationId = locationId;
    this.posId = posId;
    this.orderId = orderId;
    this.amount = amount;
    this.bulkRef = bulkRef;
  }

  /**
   * Id of the merchant.
   * 
   * @return merchant id
   */
  @JsonProperty("MerchantId")
  public String getMerchantId() {
    return merchantId;
  }
  
  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  /**
   * Location ID related to current merchant ID and PoS ID.
   * 
   * @return location id
   */
  @JsonProperty("LocationId")
  public String getLocationId() {
    return locationId;
  }

  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }

  /**
   * Current Point of Sale ID (cash register/terminal).
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
   * Order ID that identifies the payment to refund.
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
   * Amount to refund.
   * 
   * If 0.00 or blank, the whole transaction will be refunded.
   * 
   * Note: Decimal point is “.”
   * 
   * Examples: "Amount": "0.00" - "Amount": "100.00"
   * 
   * @return amount
   */  
  @JsonProperty("Amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  /**
   * Note: this parameter is required but currently only a placeholder for future use. For now, passed values will not be used, but we recommend preparing your solution for this feature. We expect to implement this in an upcoming update of the API.
   * 
   * An option for grouping the refunds - a text or ID. The field has a maximum length of 18 characters.
   * 
   * If the field remains empty and the merchant does not have a Bulkpost agreement, the merchant will receive all mobile refunds from any connected shops as individual postings in the reconciliation file.
   * 
   * If the field remains empty and the merchant does have a Bulkpost agreement, the merchant will receive all mobile refunds bulked with a default BulkRef of the MP Enterprise Serialnumber value in the reconciliation file. 
   * 
   * It must be a merchant decision whether they want all individual postings or an bulk posting per store or the entire group as one posting.
   * @return bulkRef
   */
  @JsonProperty("BulkRef")
  public String getBulkRef() {
    return bulkRef;
  }

  public void setBulkRef(String bulkRef) {
    this.bulkRef = bulkRef;
  }

}
