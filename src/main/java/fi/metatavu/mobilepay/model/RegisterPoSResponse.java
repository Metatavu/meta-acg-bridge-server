package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterPoSResponse {

  private String posId;
  
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