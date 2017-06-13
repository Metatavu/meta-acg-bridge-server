package fi.metatavu.acgbridge.server.transactions;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import fi.metatavu.acgbridge.server.cluster.ClusterController;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.transactions.TransactionController;

public class TransactionResponsibleNodeUpdateTask implements Runnable {

  @Inject
  private Logger logger;

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
      
      redistributeOrphanedTransactions();
      
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

  private void redistributeOrphanedTransactions() {
    List<Transaction> orphanedTransactions = transactionController.listOrphanedTransactions(clusterController.getNodeNames());
    
    String localNodeName = clusterController.getLocalNodeName();
    List<String> nodeNames = clusterController.getNodeNames();
    int myIndex = nodeNames.indexOf(localNodeName);
    int nodeCount = nodeNames.size();
    
    for (int i = 0; i < orphanedTransactions.size(); i++) {
      if ((i % nodeCount) == myIndex) {
        transactionController.updateTransactionResponsibleNode(orphanedTransactions.get(i), localNodeName);
      }
    }
  }

  
}
