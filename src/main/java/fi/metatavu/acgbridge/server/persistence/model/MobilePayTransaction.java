package fi.metatavu.acgbridge.server.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
  private String merchantId;
  
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
 
  @Enumerated (EnumType.STRING)
  @NotNull
  @Column(nullable = false)
  private MobilePayTransactionType mobilePayTransactionType;
  
  public String getMerchantId() {
    return merchantId;
  }
  
  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

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

  public MobilePayTransactionType getMobilePayTransactionType() {
    return mobilePayTransactionType;
  }
  
  public void setMobilePayTransactionType(MobilePayTransactionType mobilePayTransactionType) {
    this.mobilePayTransactionType = mobilePayTransactionType;
  }
  
}
