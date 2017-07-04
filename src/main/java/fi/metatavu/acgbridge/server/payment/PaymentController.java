package fi.metatavu.acgbridge.server.payment;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

@ApplicationScoped
public class PaymentController {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<PaymentStrategy> paymentStrategies;

  public Response createTransaction(Client client, fi.metatavu.acgbridge.server.rest.model.Transaction payload) {
    PaymentStrategy paymentStrategy = findPaymentStrategy(payload.getPaymentStrategy());
    if (paymentStrategy == null) {
      return Response.status(Status.BAD_REQUEST)
        .entity(String.format("Invalid payment strategy %s", payload.getPaymentStrategy()))
        .build();
    }
    
    paymentStrategy.cancelActiveTransactions(payload.getMachineId());
    
    Transaction transaction = paymentStrategy.createTransaction(client, payload);    
    if (transaction == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE)
        .entity("Failed to create transaction")
        .build();
    }
    
    if (!paymentStrategy.initatePayment(transaction)) {
      return Response.status(Status.SERVICE_UNAVAILABLE)
        .entity("Failed to initiate payment")
        .build();
    }
    
    return Response.noContent().build();
  }

  public Transaction cancelTransaction(Transaction transaction, TransactionStatus cancelStatus) {
    PaymentStrategy paymentStrategy = findPaymentStrategy(transaction.getPaymentStrategy());
    if (paymentStrategy == null) {
      logger.log(Level.SEVERE, () -> String.format("Invalid payment strategy %s", transaction.getPaymentStrategy()));
      return null;
    }
    
    return paymentStrategy.cancelTransaction(transaction, cancelStatus);
  }
  
  private PaymentStrategy findPaymentStrategy(String name) {
    for (PaymentStrategy paymentStrategy : paymentStrategies) {
      if (StringUtils.equals(paymentStrategy.getName(), name)) {
        return paymentStrategy;
      }
    }
    
    return null;
  }
  
  
}
