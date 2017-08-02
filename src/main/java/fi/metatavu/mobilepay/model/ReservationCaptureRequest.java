package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationCaptureRequest {

  private String merchantId;

  private String locationId;

  private String posId;

  private String orderId;

  private String amount;

  private String bulkRef;
  
  public ReservationCaptureRequest() {
    super();
  }
  
  public ReservationCaptureRequest(String merchantId, String locationId, String posId, String orderId, String amount,
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
   * Merchant ID related to current PoS ID.
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
   * The OrderId is a unique id that identifies the payment. The OrderId is issued by the merchant and is attached to the payment inside Danske Bank system.
   * 
   * The order ID must be unique for the merchant/location combination. This means that there should be only one completed payment with any given order ID for the same merchant and location (store) during the lifetime of the merchant/location.
   * 
   * CASE SENTITIVE
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
   * The amount for the payment. Always with 2 decimals and no thousand separators.
   * 
   * For capture of reservations made with CaptureType = Full – amount is to be set to 0.00
   * 
   * Note: Decimal point is "."
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
   * An option for grouping the payments – a text or ID. The field has a maximum length of 18 characters.
   * 
   * If the field remains empty and the merchant does not have a Bulkpost agreement, the merchant will receive all mobile payments from any connected shops as individual postings in the reconciliation file.
   * 
   * If the field remains empty and the merchant does have a Bulkpost agreement, the merchant will receive all mobile payments bulked with a default bulkref of the MP Enterprise Serialnumber value in the reconciliation file
   * 
   * It must be a merchant decision whether they want all individual postings or a bulk posting per store or the entire group as one posting.
   * 
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
