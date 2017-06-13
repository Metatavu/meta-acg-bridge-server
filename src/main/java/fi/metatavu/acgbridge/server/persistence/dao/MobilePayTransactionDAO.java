package fi.metatavu.acgbridge.server.persistence.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction_;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

/**
 * DAO class for MobilePayTransaction entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MobilePayTransactionDAO extends AbstractDAO<MobilePayTransaction> {

  public MobilePayTransaction create(TransactionStatus status, String paymentStrategy, String orderId, String machineId, String serverId, Double amount, String failureUrl, String successUrl, String posId, String locationId, String bulkRef, Integer reCalc, String customerToken, String customerReceiptToken) {
    MobilePayTransaction mobilePayTransaction = new MobilePayTransaction();
    mobilePayTransaction.setStatus(status);
    mobilePayTransaction.setAmount(amount);
    mobilePayTransaction.setFailureUrl(failureUrl);
    mobilePayTransaction.setMachineId(machineId);
    mobilePayTransaction.setOrderId(orderId);
    mobilePayTransaction.setPaymentStrategy(paymentStrategy);
    mobilePayTransaction.setServerId(serverId);
    mobilePayTransaction.setSuccessUrl(successUrl);
    mobilePayTransaction.setPosId(posId);
    mobilePayTransaction.setLocationId(locationId);
    mobilePayTransaction.setBulkRef(bulkRef);
    mobilePayTransaction.setReCalc(reCalc);
    mobilePayTransaction.setCustomerToken(customerToken);
    mobilePayTransaction.setCustomerReceiptToken(customerReceiptToken);
    return persist(mobilePayTransaction);
  }

  public List<MobilePayTransaction> listByStatus(TransactionStatus status) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<MobilePayTransaction> criteria = criteriaBuilder.createQuery(MobilePayTransaction.class);
    Root<MobilePayTransaction> root = criteria.from(MobilePayTransaction.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(MobilePayTransaction_.status), status)
    );

    return entityManager.createQuery(criteria).getResultList();
  }
  
  public MobilePayTransaction updateReCalc(MobilePayTransaction mobilePayTransaction, Integer reCalc) {
    mobilePayTransaction.setReCalc(reCalc);
    return persist(mobilePayTransaction);
  }
  
  public MobilePayTransaction updateCustomerToken(MobilePayTransaction mobilePayTransaction, String customerToken) {
    mobilePayTransaction.setCustomerToken(customerToken);
    return persist(mobilePayTransaction);
  }
  
  public MobilePayTransaction updateCustomerReceiptToken(MobilePayTransaction mobilePayTransaction, String customerReceiptToken) {
    mobilePayTransaction.setCustomerReceiptToken(customerReceiptToken);
    return persist(mobilePayTransaction);
  }
  
  
}
