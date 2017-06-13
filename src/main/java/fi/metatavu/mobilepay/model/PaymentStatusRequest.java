package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentStatusRequest {

  private String merchantId;

  private String locationId;

  private String orderId;

  private String posId;

  public PaymentStatusRequest() {
  }

  public PaymentStatusRequest(String merchantId, String locationId, String posId, String orderId) {
    super();
    this.merchantId = merchantId;
    this.locationId = locationId;
    this.posId = posId;
    this.orderId = orderId;
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

}
