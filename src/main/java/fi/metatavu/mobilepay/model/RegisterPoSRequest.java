package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterPoSRequest {

  private String merchantId;

  private String locationId;

  private String posId;

  private String name;

  public RegisterPoSRequest() {
  }

  public RegisterPoSRequest(String merchantId, String locationId, String posId, String name) {
    super();
    this.merchantId = merchantId;
    this.locationId = locationId;
    this.posId = posId;
    this.name = name;
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
   * PoS name to be shown in MobilePay App when the customer has checked-in.
   * 
   * Example: “Cash register 1”
   * 
   * @return name
   */  
  @JsonProperty("Name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
