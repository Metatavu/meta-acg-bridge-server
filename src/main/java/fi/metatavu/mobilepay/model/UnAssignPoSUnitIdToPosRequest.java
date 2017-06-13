package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnAssignPoSUnitIdToPosRequest {

  private String merchantId;

  private String locationId;

  private String posId;

  private String poSUnitId;
  
  public UnAssignPoSUnitIdToPosRequest() {
  }

  public UnAssignPoSUnitIdToPosRequest(String merchantId, String locationId, String posId, String poSUnitId) {
    super();
    this.merchantId = merchantId;
    this.locationId = locationId;
    this.posId = posId;
    this.poSUnitId = poSUnitId;
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
  
  @JsonProperty ("PoSUnitId")
  public String getPoSUnitId() {
    return poSUnitId;
  }
  
  public void setPoSUnitId(String poSUnitId) {
    this.poSUnitId = poSUnitId;
  }

}