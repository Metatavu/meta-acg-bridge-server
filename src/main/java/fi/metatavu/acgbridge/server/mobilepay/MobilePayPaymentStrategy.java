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

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.acgbridge.server.cluster.ClusterController;
import fi.metatavu.acgbridge.server.payment.PaymentStrategy;
import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransactionType;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;
import fi.metatavu.acgbridge.server.rest.model.TransactionProperty;
import fi.metatavu.acgbridge.server.transactions.TransactionController;
import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.MobilePayApiException;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.model.GetCurrentReservationResponse;
import fi.metatavu.mobilepay.model.PaymentCancelResponse;
import fi.metatavu.mobilepay.model.PaymentStartResponse;
import fi.metatavu.mobilepay.model.ReservationCancelResponse;
import fi.metatavu.mobilepay.model.ReservationStartResponse;

@ApplicationScoped
public class MobilePayPaymentStrategy implements PaymentStrategy {

  private static final String ERROR_OCCURRED_WHILE_INITIATING_MOBILE_PAY_PAYMENT = "Error occurred while initiating mobile pay payment";
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
    String transactionTypeParam = properties.get("transactionType");
    MobilePayTransactionType transactionType = transactionTypeParam != null ? EnumUtils.getEnum(MobilePayTransactionType.class, transactionTypeParam) : MobilePayTransactionType.RESERVE_CAPTURE;
    
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
    
    return transactionController.createMobilePayTransaction(client, transactionType, merchantId, orderId, machineId, serverId, amount, failureUrl, successUrl, posId, locationId, bulkRef, clusterController.getLocalNodeName());
  }
  
  @Override
  public boolean initatePayment(Transaction transaction) {
    MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
    
    switch (mobilePayTransaction.getMobilePayTransactionType()) {
      case DIRECT_PAYMENT:
        return initiateDirectPayment(mobilePayTransaction);
      case RESERVE_CAPTURE:
        return initiateReserveCapture(mobilePayTransaction);
      default:
    }
    
    logger.log(Level.SEVERE, () -> String.format("Don't know how to handle transaction type %s", mobilePayTransaction.getMobilePayTransactionType()));
    
    return false;
  }
  
  @Override
  public Transaction cancelTransaction(Transaction transaction, TransactionStatus cancelStatus) {
    if (transaction instanceof MobilePayTransaction) {
      MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
      try {
        switch (mobilePayTransaction.getMobilePayTransactionType()) {
          case DIRECT_PAYMENT:
            cancelDirectPayment(mobilePayTransaction);
          break;
          case RESERVE_CAPTURE:
            cancelReserveCapture(mobilePayTransaction);
          break;
          default:
            logger.log(Level.SEVERE, () -> String.format("Don't know how to cancel transaction type %s", mobilePayTransaction.getMobilePayTransactionType()));
          break;
        }
        
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
  public Transaction updateTransactionStatus(Transaction transaction, TransactionStatus transactionStatus) {
    if (transaction instanceof MobilePayTransaction) {
      MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
      
      switch (transactionStatus) {
        case CANCELLED:
          return cancelTransaction(transaction, transactionStatus);
        case SUCCESS:
          return updateTransactionStatusSuccess(mobilePayTransaction);
        default:
          logger.log(Level.SEVERE, () -> String.format("Provided invalid status %s when updating transaction %d", transactionStatus, transaction.getId()));
          return null;
        
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Tried to update non-mobilepay transaction %d", transaction.getId()));
    }
    
    return null;
  }

  private Transaction updateTransactionStatusSuccess(MobilePayTransaction mobilePayTransaction) {
    switch (mobilePayTransaction.getMobilePayTransactionType()) {
      case DIRECT_PAYMENT:
        // Payment is already captured when using direct payments
        transactionController.updateTransactionStatus(mobilePayTransaction, TransactionStatus.SUCCESS);
        logger.log(Level.INFO, () -> String.format("Direct payment transaction %d successful", mobilePayTransaction.getId()));
        return mobilePayTransaction;
      case RESERVE_CAPTURE:
        try {
          captureReserveCapture(mobilePayTransaction);
          transactionController.updateTransactionStatus(mobilePayTransaction, TransactionStatus.SUCCESS);
          logger.log(Level.INFO, () -> String.format("Captured payment transaction %d successfully", mobilePayTransaction.getId()));
          return mobilePayTransaction;
        } catch (MobilePayApiException e) {
          logger.log(Level.SEVERE, String.format("Error occurred while capturing transaction %d", mobilePayTransaction.getId()), e);
        }
      break;
      default:
        logger.log(Level.SEVERE, () -> String.format("Don't know how to cancel transaction type %s", mobilePayTransaction.getMobilePayTransactionType()));
      break;
    }
    
    return null;
  }

  @Override
  public void cancelActiveTransactions(String machineId) {
    List<MobilePayTransaction> pendingTransactions = transactionController.listPendingMobilePayTransactionsByMachineId(machineId);
    for (MobilePayTransaction pendingTransaction : pendingTransactions) {
      cancelTransaction(pendingTransaction, TransactionStatus.CANCELLED);
    }
  }

  private boolean initiateDirectPayment(MobilePayTransaction mobilePayTransaction) {
    String orderId = mobilePayTransaction.getOrderId();
    Double amount = mobilePayTransaction.getAmount();
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    
    try {
      MobilePayResponse<PaymentStartResponse> paymentStartResponse = mobilePayApi.paymentStart(apiKey, merchantId, mobilePayTransaction.getLocationId(), mobilePayTransaction.getPosId(), orderId, amount, mobilePayTransaction.getBulkRef(), "Start");
      if (!paymentStartResponse.isOk()) {
        boolean hasExistingPayment = paymentStartResponse.getError().getStatusCode() == 50;
        if (hasExistingPayment && cancelExistingPayment(mobilePayTransaction)) {
          return initiateDirectPayment(mobilePayTransaction);
        }
        
        logger.log(Level.SEVERE, () -> String.format("Failed to start payment [%d]: %s", paymentStartResponse.getStatus(), paymentStartResponse.getMessage()));
      } else {
        return true;
      }
    } catch (MobilePayApiException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, ERROR_OCCURRED_WHILE_INITIATING_MOBILE_PAY_PAYMENT, e);
      }
    }
    
    transactionController.updateTransactionStatus(mobilePayTransaction, TransactionStatus.ERRORED);
    
    return false;
  }

  private boolean initiateReserveCapture(MobilePayTransaction mobilePayTransaction) {
    String orderId = mobilePayTransaction.getOrderId();
    Double amount = mobilePayTransaction.getAmount();
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    String posId = mobilePayTransaction.getPosId();
    String locationId = mobilePayTransaction.getLocationId();
    String bulkRef = mobilePayTransaction.getBulkRef();
    String captureType = "Full";
    
    try {
      MobilePayResponse<ReservationStartResponse> reservationStartResponse = mobilePayApi.reservationStart(apiKey, merchantId, locationId, posId, orderId, amount, bulkRef, captureType);
      if (!reservationStartResponse.isOk()) {
        boolean hasExistingPayment = reservationStartResponse.getError().getStatusCode() == 50;
        if (hasExistingPayment && cancelExistingReservation(mobilePayTransaction)) {
          return initiateReserveCapture(mobilePayTransaction);
        }
        
        logger.log(Level.SEVERE, () -> String.format("Failed to start reservation [%d]: %s", reservationStartResponse.getStatus(), reservationStartResponse.getMessage()));
      } else {
        return true;
      }
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, ERROR_OCCURRED_WHILE_INITIATING_MOBILE_PAY_PAYMENT, e);
    }
   
    transactionController.updateTransactionStatus(mobilePayTransaction, TransactionStatus.ERRORED);
    
    return false;
  }

  private void cancelDirectPayment(MobilePayTransaction mobilePayTransaction) throws MobilePayApiException {
    String posId = mobilePayTransaction.getPosId();
    String locationId = mobilePayTransaction.getLocationId();
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    String orderId = mobilePayTransaction.getOrderId();
    String bulkRef = mobilePayTransaction.getBulkRef();
    
    if (mobilePayTransaction.getStatus() == TransactionStatus.SUCCESS) {
      mobilePayApi.paymentRefund(apiKey, merchantId, locationId, posId, orderId, 0d, bulkRef);
    } else {
      mobilePayApi.paymentCancel(apiKey, merchantId, locationId, posId);
    }
  }

  private void cancelReserveCapture(MobilePayTransaction mobilePayTransaction) throws MobilePayApiException {
    String posId = mobilePayTransaction.getPosId();
    String locationId = mobilePayTransaction.getLocationId();
    String merchantId = mobilePayTransaction.getMerchantId();
    String orderId = mobilePayTransaction.getOrderId();
    String apiKey = getApiKey(merchantId);
    mobilePayApi.reservationCancel(apiKey, merchantId, locationId, posId, orderId);
  }
  
  private void captureReserveCapture(MobilePayTransaction mobilePayTransaction) throws MobilePayApiException {
    String posId = mobilePayTransaction.getPosId();
    String locationId = mobilePayTransaction.getLocationId();
    String merchantId = mobilePayTransaction.getMerchantId();
    String orderId = mobilePayTransaction.getOrderId();
    String apiKey = getApiKey(merchantId);
    String bulkRef = mobilePayTransaction.getBulkRef();
    
    mobilePayApi.reservationCapture(apiKey, merchantId, locationId, posId, orderId, 0d, bulkRef);
  }

  
  private Map<String, String> getProperties(List<TransactionProperty> transactionProperties) {
    Map<String, String> result = new HashMap<>(transactionProperties.size());
  
    for (TransactionProperty transactionProperty : transactionProperties) {
      result.put(transactionProperty.getKey(), transactionProperty.getValue());
    }
    
    return result;
  }
  
  private boolean cancelExistingPayment(MobilePayTransaction mobilePayTransaction) {
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    String locationId = mobilePayTransaction.getLocationId();
    String posId = mobilePayTransaction.getPosId();
    
    try {
      MobilePayResponse<PaymentCancelResponse> directCancelResponse = mobilePayApi.paymentCancel(apiKey, merchantId, locationId, posId);
      if (directCancelResponse.isOk()) {
        return true;
      }
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, ERROR_OCCURRED_WHILE_INITIATING_MOBILE_PAY_PAYMENT, e);
    }
    
    return false;
  }
  
  private boolean cancelExistingReservation(MobilePayTransaction mobilePayTransaction) {
    String merchantId = mobilePayTransaction.getMerchantId();
    String apiKey = getApiKey(merchantId);
    String locationId = mobilePayTransaction.getLocationId();
    String posId = mobilePayTransaction.getPosId();
    
    try {
      MobilePayResponse<GetCurrentReservationResponse> currentReservationResponse = mobilePayApi.getCurrentReservation(apiKey, merchantId, locationId, posId);
      if (!currentReservationResponse.isOk()) {
        logger.log(Level.SEVERE, currentReservationResponse.getError().getStatusText());
        return false;
      }
      
      String orderId = currentReservationResponse.getResponse().getOrderId();
      
      MobilePayResponse<ReservationCancelResponse> reservationCancelResponse = mobilePayApi.reservationCancel(apiKey, merchantId, locationId, posId, orderId);
      if (reservationCancelResponse.isOk()) {
        return true; 
      }
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, ERROR_OCCURRED_WHILE_INITIATING_MOBILE_PAY_PAYMENT, e);
    }
    
    return false;
  }
  
  private String getApiKey(String merchantId) {
    return mobilePaySettingsController.getApiKey(merchantId);
  }
  
}