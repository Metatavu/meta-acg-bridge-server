package fi.metatavu.acgbridge.server.transactions;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import fi.metatavu.acgbridge.server.payment.PaymentController;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

public class TransactionTimeoutUpdateTask implements Runnable {

  @Inject
  private Logger logger;

  @Inject
  private TransactionController transactionController;
  
  @Inject
  private PaymentController paymentController;
  
  @Resource
  private EJBContext ejbContext;

  @Override
  @SuppressWarnings ("squid:S2583")
  public void run() {
    UserTransaction userTransaction = ejbContext.getUserTransaction();
    try {
      userTransaction.begin();
      
      updateTimedOutTransactions();
      
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

  private void updateTimedOutTransactions() {
    Date fiveMinutesAgo = Date.from(OffsetDateTime.now().minusMinutes(5).toInstant());
    List<Transaction> timedOutTransactions = transactionController.listPendingTransactionsBefore(fiveMinutesAgo);
    for (Transaction timedOutTransaction : timedOutTransactions) {
      paymentController.cancelTransaction(timedOutTransaction, TransactionStatus.TIMED_OUT);
    }
  }

  
}
