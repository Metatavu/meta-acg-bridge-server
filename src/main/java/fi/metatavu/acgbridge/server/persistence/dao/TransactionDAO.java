package fi.metatavu.acgbridge.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;

/**
 * DAO class for Transaction entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TransactionDAO extends AbstractDAO<Transaction> {

  public Transaction updateStatus(Transaction transaction, TransactionStatus status) {
    transaction.setStatus(status);
    return persist(transaction);
  }
  
}
