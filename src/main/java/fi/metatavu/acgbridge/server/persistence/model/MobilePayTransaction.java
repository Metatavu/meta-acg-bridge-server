package fi.metatavu.acgbridge.server.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity for storing transactions
 * 
 * @author Antti Leppä
 */
@Entity
@PrimaryKeyJoinColumn(name = "id")
public class MobilePayTransaction extends Transaction {

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String posId;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String locationId;

  private String bulkRef;
  
  private Integer reCalc;

  private String customerToken;

  private String customerReceiptToken;

  public String getPosId() {
    return posId;
  }

  public void setPosId(String posId) {
    this.posId = posId;
  }

  public String getLocationId() {
    return locationId;
  }

  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }
  
  public String getBulkRef() {
    return bulkRef;
  }
  
  public void setBulkRef(String bulkRef) {
    this.bulkRef = bulkRef;
  }

  public Integer getReCalc() {
    return reCalc;
  }

  public void setReCalc(Integer reCalc) {
    this.reCalc = reCalc;
  }

  public String getCustomerToken() {
    return customerToken;
  }

  public void setCustomerToken(String customerToken) {
    this.customerToken = customerToken;
  }

  public String getCustomerReceiptToken() {
    return customerReceiptToken;
  }

  public void setCustomerReceiptToken(String customerReceiptToken) {
    this.customerReceiptToken = customerReceiptToken;
  }

}
