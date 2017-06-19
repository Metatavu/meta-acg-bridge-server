package fi.metatavu.acgbridge.server.persistence.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity for storing transactions
 * 
 * @author Antti Leppä
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @NotNull
  @Column(nullable = false)
  private Date created;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String orderId;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String machineId;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String serverId;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String successUrl;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String failureUrl;

  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String paymentStrategy;

  @NotNull
  @Column(nullable = false)
  private Double amount;
  
  @NotNull
  @Column(nullable = false)
  @Enumerated (EnumType.STRING)
  private TransactionStatus status;
  
  @ManyToOne (optional = false)
  private Client client;

  private String responsibleNode;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public Date getCreated() {
    return created;
  }
  
  public void setCreated(Date created) {
    this.created = created;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getMachineId() {
    return machineId;
  }

  public void setMachineId(String machineId) {
    this.machineId = machineId;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public String getSuccessUrl() {
    return successUrl;
  }

  public void setSuccessUrl(String successUrl) {
    this.successUrl = successUrl;
  }

  public String getFailureUrl() {
    return failureUrl;
  }

  public void setFailureUrl(String failureUrl) {
    this.failureUrl = failureUrl;
  }

  public String getPaymentStrategy() {
    return paymentStrategy;
  }

  public void setPaymentStrategy(String paymentStrategy) {
    this.paymentStrategy = paymentStrategy;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }
  
  public void setStatus(TransactionStatus status) {
    this.status = status;
  }
  
  public TransactionStatus getStatus() {
    return status;
  }
  
  public Client getClient() {
    return client;
  }
  
  public void setClient(Client client) {
    this.client = client;
  }
  
  public String getResponsibleNode() {
    return responsibleNode;
  }
  
  public void setResponsibleNode(String responsibleNode) {
    this.responsibleNode = responsibleNode;
  }

}
