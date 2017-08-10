package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetCurrentReservationRequest {

  private String merchantId;
  private String locationId;
  private String posId;

  public GetCurrentReservationRequest() {
  }
  
  public GetCurrentReservationRequest(String merchantId, String locationId, String posId) {
    super();
    this.merchantId = merchantId;
    this.locationId = locationId;
    this.posId = posId;
  }

  /**
   * Merchant ID.
   * 
   * @return
   */
  @JsonProperty("MerchantId")
  public String getMerchantId() {
    return merchantId;
  }
  
  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }
  
  /**
   * Location ID.
   * 
   * @return
   */
  @JsonProperty("LocationId")
  public String getLocationId() {
    return locationId;
  }
  
  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }
  
  /**
   * PoSId.
   * 
   * @return
   */
  @JsonProperty("PoSId")
  public String getPosId() {
    return posId;
  }
  
  public void setPosId(String posId) {
    this.posId = posId;
  }

}
