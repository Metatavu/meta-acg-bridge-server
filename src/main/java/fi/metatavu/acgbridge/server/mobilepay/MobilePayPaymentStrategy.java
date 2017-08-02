package fi.metatavu.acgbridge.server.mobilepay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.cluster.ClusterController;
import fi.metatavu.acgbridge.server.payment.PaymentStrategy;
import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;
import fi.metatavu.acgbridge.server.rest.model.TransactionProperty;
import fi.metatavu.acgbridge.server.transactions.TransactionController;
import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.MobilePayApiException;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.model.PaymentStartResponse;

@ApplicationScoped
public class MobilePayPaymentStrategy implements PaymentStrategy {

  private static final String STRATEGY_NAME = "mobilepay";

  @Inject
  private Logger logger;
  
  @Inject
  private TransactionController transactionController;
  
  @Inject
  private MobilePayApi mobilePayApi;

  @Inject
  private MobilePayPosIdController mobilePayPosIdController;

  @Inject
  private MobilePaySettingsController mobilePaySettingsController;
  
  @Inject
  private ClusterController clusterController;
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Override
  public String getName() {
    return STRATEGY_NAME;
  }
  
  @Override
  public Transaction createTransaction(Client client, fi.metatavu.acgbridge.server.rest.model.Transaction payload) {
    Map<String, String> properties = getProperties(payload.getProperties());
    String orderId = payload.getOrderId();
    String machineId = payload.getMachineId();
    String serverId = payload.getServerId();
    String successUrl = payload.getSuccessUrl();
    String failureUrl = payload.getFailureUrl();
    Double amount = payload.getAmount();
    String merchantId = properties.get("merchantId");
    String locationId = properties.get("locationId");
    String bulkRef = properties.containsKey("bulkRef") ? properties.get("bulkRef") : "";
    String name = properties.containsKey("name") ? properties.get("name") : "";
    
    String posId;
    try {
      posId = mobilePayPosIdController.getPosId(merchantId, machineId, locationId, name);
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, String.format("Failed to obtain posId by machineId %s", machineId), e);
      return null;
    }
    
    if (posId == null) {
      return null;
    }
    
    return transactionController.createMobilePayTransaction(client, merchantId, orderId, machineId, serverId, amount, failureUrl, successUrl, posId, locationId, bulkRef, clusterController.getLocalNodeName());
  }
  
  @Override
  public boolean initatePayment(Transaction transaction) {
    MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
    
    String orderId = mobilePayTransaction.getOrderId();
    Double amount = mobilePayTransaction.getAmount();
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    
    try {
      MobilePayResponse<PaymentStartResponse> paymentStartResponse = mobilePayApi.paymentStart(apiKey, merchantId, mobilePayTransaction.getLocationId(), mobilePayTransaction.getPosId(), orderId, amount, mobilePayTransaction.getBulkRef(), "Start");
      if (!paymentStartResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to start payment [%d]: %s", paymentStartResponse.getStatus(), paymentStartResponse.getMessage()));
        return false;
      } else {
        return true;
      }
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, "Error occurred while initiating mobile pay payment", e);
    }
    
    return false;
  }
  
  @Override
  public Transaction cancelTransaction(Transaction transaction, TransactionStatus cancelStatus) {
    if (transaction instanceof MobilePayTransaction) {
      MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
      String posId = mobilePayTransaction.getPosId();
      String locationId = mobilePayTransaction.getLocationId();
      String merchantId = mobilePayTransaction.getMerchantId();
      String apiKey = getApiKey(merchantId);
      
      try {
        mobilePayApi.paymentCancel(apiKey, merchantId, locationId, posId);
        transactionController.updateTransactionStatus(mobilePayTransaction, cancelStatus);
        logger.log(Level.INFO, () -> String.format("Cancelled transaction %d with status %s", transaction.getId(), transaction.getStatus()));        
      } catch (MobilePayApiException e) {
        logger.log(Level.SEVERE, String.format("Error occurred while cancelling transaction %d", transaction.getId()), e);
      }
      
    } else {
      logger.log(Level.SEVERE, () -> String.format("Tried to cancelling non-mobilepay transaction %d", transaction.getId()));
    }
    
    return transaction;
  }
  
  @Override
  public void cancelActiveTransactions(String machineId) {
    List<MobilePayTransaction> pendingTransactions = transactionController.listPendingMobilePayTransactionsByMachineId(machineId);
    for (MobilePayTransaction pendingTransaction : pendingTransactions) {
      cancelTransaction(pendingTransaction, TransactionStatus.CANCELLED);
    }
  }
  
  private Map<String, String> getProperties(List<TransactionProperty> transactionProperties) {
    Map<String, String> result = new HashMap<>(transactionProperties.size());
  
    for (TransactionProperty transactionProperty : transactionProperties) {
      result.put(transactionProperty.getKey(), transactionProperty.getValue());
    }
    
    return result;
  }

  @Override
  public boolean cancelActiveTransactionsByOrderId(String orderId) {
    List<MobilePayTransaction> pendingTransactions = transactionController.listPendingMobilePayTransactionsByOrderId(orderId);
    if (pendingTransactions.isEmpty()) {
      return false;
    }
    
    for (MobilePayTransaction pendingTransaction : pendingTransactions) {
      cancelTransaction(pendingTransaction, TransactionStatus.CANCELLED);
    }
    
    return true;
  }
  
  private String getApiKey(String merchantId) {
    return mobilePaySettingsController.getApiKey(merchantId);
  }
  
}