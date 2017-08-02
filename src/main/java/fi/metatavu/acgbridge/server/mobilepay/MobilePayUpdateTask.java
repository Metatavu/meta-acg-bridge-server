package fi.metatavu.acgbridge.server.mobilepay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.acgbridge.server.cluster.ClusterController;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayTransaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;
import fi.metatavu.acgbridge.server.rest.model.Transaction;
import fi.metatavu.acgbridge.server.rest.model.TransactionProperty;
import fi.metatavu.acgbridge.server.security.HmacSignatureBuilder;
import fi.metatavu.acgbridge.server.security.HmacSignatureException;
import fi.metatavu.acgbridge.server.transactions.TransactionController;
import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.MobilePayApiException;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.model.PaymentStatusResponse;

@ApplicationScoped
public class MobilePayUpdateTask implements Runnable {

  private static final String FAILED_NOTIFY_CALLER = "Failed notify caller";

  @Inject
  private Logger logger;
  
  @Inject
  private MobilePayApi mobilePayApi;
  
  @Inject
  private MobilePaySettingsController mobilePaySettingsController;
  
  @Inject
  private TransactionController transactionController;

  @Inject
  private ClusterController clusterController;
  
  @Resource
  private EJBContext ejbContext;

  @Override
  @SuppressWarnings ("squid:S2583")
  public void run() {
    UserTransaction userTransaction = ejbContext.getUserTransaction();
    try {
      userTransaction.begin();
      
      checkPendingTransactions();
      
      userTransaction.commit();
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Timer throw an exception", ex);
      try {
        if (userTransaction != null) {
          userTransaction.rollback();
        }
      } catch (SystemException e1) {
        logger.log(Level.SEVERE, "Failed to rollback transaction", e1);
      }
    }
  }

  private void checkPendingTransactions() {
    List<MobilePayTransaction> transactions = transactionController.listPendingMobilePayTransactions(clusterController.getLocalNodeName());
    for (MobilePayTransaction transaction : transactions) {
      try {
        String merchantId = transaction.getMerchantId();
        String apiKey = mobilePaySettingsController.getApiKey(merchantId);
        MobilePayResponse<PaymentStatusResponse> response = mobilePayApi.paymentStatus(apiKey, merchantId, transaction.getLocationId(), transaction.getPosId(), transaction.getOrderId());
        if (response.isOk()) {
          PaymentStatusResponse paymentStatus = response.getResponse();
          switch (paymentStatus.getPaymentStatus()) {
            case 40:
              handleTransactionCancel(transaction);
            break;
            case 50:
              handleTransactionError(transaction);
            break;
            case 100:
              handleTransactionDone(transaction);
            break;  
            default:
              logger.log(Level.SEVERE, () -> String.format("MobilePay responded with %d for transaction %d", paymentStatus.getPaymentStatus(), transaction.getId()));
            break;
          }
        } else {
          logger.log(Level.SEVERE, () -> String.format("Received [%d]: %s when checking transaction status from MobilePay server", response.getStatus(), response.getStatus()));
        }
      } catch (MobilePayApiException e) {
        logger.log(Level.SEVERE, "Failed to check transaction status from MobilePay server", e);
      }
    }
  }

  private void handleTransactionDone(MobilePayTransaction transaction) {
    handleWebhook(transaction, TransactionStatus.SUCCESS);
  }
  
  private void handleTransactionError(MobilePayTransaction transaction) {
    handleWebhook(transaction, TransactionStatus.ERRORED);
  }
  
  private void handleTransactionCancel(MobilePayTransaction transaction) {
    handleWebhook(transaction, TransactionStatus.CANCELLED);
  }

  private void handleWebhook(MobilePayTransaction transaction, TransactionStatus transactionStatus) {
    String signatureKey = transaction.getClient().getSecretKey();
    
    try {
      int status = fireWebhook(transactionStatus == TransactionStatus.SUCCESS ? transaction.getSuccessUrl() : transaction.getFailureUrl(), signatureKey, transaction);
      if (status == 200) {
        transactionController.updateTransactionStatus(transaction, transactionStatus);
      } else {
        logger.log(Level.WARNING, () -> String.format("Webhook notification failed on %d", status)); 
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, FAILED_NOTIFY_CALLER, e);
    } catch (HmacSignatureException e) {
      logger.log(Level.SEVERE, "Failed to create HMAC signature", e);
    }
  }

  private TransactionProperty createProperty(String key, String value) {
    TransactionProperty result = new TransactionProperty();
    result.setKey(key);
    result.setValue(value);
    return result;
  }
  
  private int fireWebhook(String url, String signatureKey, MobilePayTransaction transaction) throws IOException, HmacSignatureException {
    List<TransactionProperty> properties = new  ArrayList<>(3);
    properties.add(createProperty("locationId", transaction.getLocationId()));
    properties.add(createProperty("bulkRef", transaction.getBulkRef()));
    properties.add(createProperty("posId", transaction.getPosId()));
    
    fi.metatavu.acgbridge.server.rest.model.Transaction payloadModel = new Transaction();
    payloadModel.setAmount(transaction.getAmount());
    payloadModel.setFailureUrl(transaction.getFailureUrl());
    payloadModel.setId(transaction.getId().toString());
    payloadModel.setMachineId(transaction.getMachineId());
    payloadModel.setOrderId(transaction.getOrderId());
    payloadModel.setPaymentStrategy(transaction.getPaymentStrategy());
    payloadModel.setProperties(properties);
    payloadModel.setServerId(transaction.getServerId());
    payloadModel.setSuccessUrl(transaction.getSuccessUrl());
    
    String payload = (new ObjectMapper()).writeValueAsString(payloadModel);
    
    return sendWebhook(url, payload, signatureKey);
  }
  
  private int sendWebhook(String url, String payload, String signatureKey) throws IOException, HmacSignatureException {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      String signature = new HmacSignatureBuilder(signatureKey)
        .append(url)
        .append(payload)
        .build();
      
      return executePostRequest(httpClient, url, payload, signature);
    } finally {
      closeClient(httpClient);
    }
  }
  
  private int executePostRequest(CloseableHttpClient httpClient, String url, String data, String signature) throws IOException {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(new StringEntity(data, "UTF-8"));
    
    return executeRequest(httpClient, httpPost, signature);
  }
  
  private int executeRequest(CloseableHttpClient httpClient, HttpPost request, String signature) throws IOException {
    request.addHeader("X-META-ACG-BRIDGE-AUTH", signature);
    request.addHeader("Content-Type", "application/json");
    
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      StatusLine statusLine = response.getStatusLine();
      return statusLine.getStatusCode();
    }
  }

  private void closeClient(CloseableHttpClient httpClient) {
    try {
      httpClient.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to close http client", e);
    }
  }
  
}
