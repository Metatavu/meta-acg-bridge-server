package fi.metatavu.acgbridge.server.payment;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

@ApplicationScoped
public class PaymentController {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<PaymentStrategy> paymentStrategies;

  public Transaction updateTransactionStatus(Transaction transaction, TransactionStatus transactionStatus) {
    PaymentStrategy paymentStrategy = findPaymentStrategy(transaction.getPaymentStrategy());
    if (paymentStrategy == null) {
      logger.log(Level.SEVERE, () -> String.format("Invalid payment strategy %s", transaction.getPaymentStrategy()));
      return null;
    }
    
    return paymentStrategy.updateTransactionStatus(transaction, transactionStatus);
  }
  
  public Transaction cancelTransaction(Transaction transaction, TransactionStatus cancelStatus) {
    PaymentStrategy paymentStrategy = findPaymentStrategy(transaction.getPaymentStrategy());
    if (paymentStrategy == null) {
      logger.log(Level.SEVERE, () -> String.format("Invalid payment strategy %s", transaction.getPaymentStrategy()));
      return null;
    }
    
    return paymentStrategy.cancelTransaction(transaction, cancelStatus);
  }
  
  public PaymentStrategy findPaymentStrategy(String name) {
    for (PaymentStrategy paymentStrategy : paymentStrategies) {
      if (StringUtils.equals(paymentStrategy.getName(), name)) {
        return paymentStrategy;
      }
    }
    
    return null;
  }
  
  
}
