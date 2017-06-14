package fi.metatavu.acgbridge.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.Client_;

/**
 * DAO class for Client entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ClientDAO extends AbstractDAO<Client> {

  /**
   * Finds client setting by clientId
   * 
   * @param clientId clientId
   * @return found client or null if non found
   */
  public Client findByClientId(String clientId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Client> criteria = criteriaBuilder.createQuery(Client.class);
    Root<Client> root = criteria.from(Client.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(Client_.clientId), clientId)
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

}
