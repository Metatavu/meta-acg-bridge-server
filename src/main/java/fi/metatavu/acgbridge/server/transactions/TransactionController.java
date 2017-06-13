package fi.metatavu.acgbridge.server.transactions;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.persistence.dao.MobilePayTransactionDAO;
import fi.metatavu.acgbridge.server.persistence.dao.TransactionDAO;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

/**
 * Controller for system settings.
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TransactionController {
  
  @Inject
  private TransactionDAO transactionDAO;

  @Inject
  private MobilePayTransactionDAO mobilePayTransactionDAO;
  
  public Transaction createMobilePayTransaction(String orderId, String machineId, String serverId, Double amount, String failureUrl, String successUrl, String posId, String locationId, String bulkRef) {
    return mobilePayTransactionDAO.create(TransactionStatus.PENDING, "mobilepay", orderId, machineId, serverId, amount, failureUrl, successUrl, posId, locationId, bulkRef, null, null, null);
  }

  public List<MobilePayTransaction> listPendingMobilePayTransactions() {
    return mobilePayTransactionDAO.listByStatus(TransactionStatus.PENDING);
  }

  public Transaction updateTransactionStatus(Transaction transaction, TransactionStatus status) {
    return transactionDAO.updateStatus(transaction, status);
  }
  
}
