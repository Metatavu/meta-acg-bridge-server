package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetUniquePoSIdRequest {

  private String merchantId;

  public GetUniquePoSIdRequest() {
  }

  public GetUniquePoSIdRequest(String merchantId) {
    super();
    this.merchantId = merchantId;
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

}
