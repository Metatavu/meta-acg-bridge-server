package fi.metatavu.acgbridge.server.transactions;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.persistence.dao.MobilePayTransactionDAO;
import fi.metatavu.acgbridge.server.persistence.dao.TransactionDAO;
import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransactionType;
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
  
  public Transaction createMobilePayTransaction(Client client, MobilePayTransactionType mobilePayTransactionType, String merchantId, String orderId, String machineId, String serverId, Double amount, String failureUrl, String successUrl, String posId, String locationId, String bulkRef, String responsibleNode) {
    return mobilePayTransactionDAO.create(client, mobilePayTransactionType, merchantId, TransactionStatus.PENDING, "mobilepay", orderId, machineId, serverId, amount, failureUrl, successUrl, posId, locationId, bulkRef, null, null, null, responsibleNode, new Date());
  }

  public List<MobilePayTransaction> listPendingMobilePayTransactions(String responsibleNode) {
    return mobilePayTransactionDAO.listByStatusAndResponsibleNode(TransactionStatus.PENDING, responsibleNode);
  }

  public List<MobilePayTransaction> listPendingMobilePayTransactionsByMachineId(String machineId) {
    return mobilePayTransactionDAO.listByStatusAndMachineId(TransactionStatus.PENDING, machineId);
  }

  public List<MobilePayTransaction> listPendingMobilePayTransactionsByOrderId(String orderId) {
    return mobilePayTransactionDAO.listByStatusAndOrderId(TransactionStatus.PENDING, orderId);
  }

  public Transaction findTransactionById(Long id) {
    return transactionDAO.findById(id);
  }
  
  public Transaction updateTransactionStatus(Transaction transaction, TransactionStatus status) {
    return transactionDAO.updateStatus(transaction, status);
  }

  public List<Transaction> listOrphanedTransactions(List<String> validNodeNames) {
    return transactionDAO.listByStatusAndResponsibleNodeNotIn(TransactionStatus.PENDING, validNodeNames);
  }

  public List<Transaction> listPendingTransactionsBefore(Date before) {
    return transactionDAO.listByStatusAndCreatedBefore(TransactionStatus.PENDING, before);
  }
  
  public Transaction updateTransactionResponsibleNode(Transaction transaction, String responsibleNode) {
    return transactionDAO.updateResponsibleNode(transaction, responsibleNode);
  }
  
}
