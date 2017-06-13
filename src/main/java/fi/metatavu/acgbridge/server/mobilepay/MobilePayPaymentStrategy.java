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

import fi.metatavu.acgbridge.server.payment.PaymentStrategy;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
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
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Override
  public String getName() {
    return STRATEGY_NAME;
  }
  
  @Override
  public Transaction createTransaction(fi.metatavu.acgbridge.server.rest.model.Transaction payload) {
    Map<String, String> properties = getProperties(payload.getProperties());
    String orderId = payload.getOrderId();
    String machineId = payload.getMachineId();
    String serverId = payload.getServerId();
    String successUrl = payload.getSuccessUrl();
    String failureUrl = payload.getFailureUrl();
    Double amount = payload.getAmount();
    String locationId = properties.get("locationId");
    String bulkRef = properties.containsKey("bulkRef") ? properties.get("bulkRef") : "";
    String name = properties.containsKey("name") ? properties.get("name") : "";
    
    String posId;
    try {
      posId = mobilePayPosIdController.getPosId(machineId, locationId, name);
    } catch (MobilePayApiException e) {
      logger.log(Level.SEVERE, String.format("Failed to obtain posId by machineId %s", machineId), e);
      return null;
    }
    
    if (posId == null) {
      return null;
    }
    
    return transactionController.createMobilePayTransaction(orderId, machineId, serverId, amount, failureUrl, successUrl, posId, locationId, bulkRef);
  }
  
  @Override
  public boolean initatePayment(Transaction transaction) {
    MobilePayTransaction mobilePayTransaction = (MobilePayTransaction) transaction;
    
    String orderId = mobilePayTransaction.getOrderId();
    Double amount = mobilePayTransaction.getAmount();
    
    try {
      MobilePayResponse<PaymentStartResponse> paymentStartResponse = mobilePayApi.paymentStart(mobilePayTransaction.getLocationId(), mobilePayTransaction.getPosId(), orderId, amount, mobilePayTransaction.getBulkRef(), "Start");
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
  
  private Map<String, String> getProperties(List<TransactionProperty> transactionProperties) {
    Map<String, String> result = new HashMap<>(transactionProperties.size());
  
    for (TransactionProperty transactionProperty : transactionProperties) {
      result.put(transactionProperty.getKey(), transactionProperty.getValue());
    }
    
    return result;
  }
  
}