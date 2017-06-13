package fi.metatavu.mobilepay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadPoSAssignPoSUnitIdResponse {

  private String posUnitId;
  
  /**
   * Current Point of Sale ID (cash register/terminal).
   * 
   * @return point of sale id
   */
  @JsonProperty("PoSUnitId")
  public String getPosUnitId() {
    return posUnitId;
  }
  
  public void setPosUnitId(String posUnitId) {
    this.posUnitId = posUnitId;
  }
  
}