package fi.metatavu.acgbridge.server.persistence.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.acgbridge.server.persistence.model.Transaction;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;
import fi.metatavu.acgbridge.server.persistence.model.Transaction_;

/**
 * DAO class for Transaction entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TransactionDAO extends AbstractDAO<Transaction> {

  public List<Transaction> listByStatusAndResponsibleNodeNotIn(TransactionStatus status, List<String> responsibleNodes) {
    if (responsibleNodes == null || responsibleNodes.isEmpty()) {
      return Collections.emptyList();
    }
    
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Transaction> criteria = criteriaBuilder.createQuery(Transaction.class);
    Root<Transaction> root = criteria.from(Transaction.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Transaction_.status), status),
        criteriaBuilder.not(root.get(Transaction_.responsibleNode).in(responsibleNodes))
      )
    );

    return entityManager.createQuery(criteria).getResultList();
  }

  public List<Transaction> listByStatusAndCreatedBefore(TransactionStatus status, Date before) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Transaction> criteria = criteriaBuilder.createQuery(Transaction.class);
    Root<Transaction> root = criteria.from(Transaction.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Transaction_.status), status),
        criteriaBuilder.lessThanOrEqualTo(root.get(Transaction_.created), before)
      )
    );

    return entityManager.createQuery(criteria).getResultList();
  }
  
  public Transaction updateStatus(Transaction transaction, TransactionStatus status) {
    transaction.setStatus(status);
    return persist(transaction);
  }
  
  public Transaction updateResponsibleNode(Transaction transaction, String responsibleNode) {
    transaction.setResponsibleNode(responsibleNode);
    return persist(transaction);
  }
  
}
