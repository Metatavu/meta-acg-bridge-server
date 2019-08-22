package fi.metatavu.acgbridge.server.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity for storing posIds matching posUnitIds
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "clientId" }) })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String clientId;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String secretKey;

  public String getClientId() {
    return clientId;
  }
  
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
  
  public String getSecretKey() {
    return secretKey;
  }
  
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }
  
}
