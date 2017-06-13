package fi.metatavu.acgbridge.server.persistence.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.acgbridge.server.persistence.model.MobilePayPosId;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayPosId_;

/**
 * DAO class for MobilePayPosId entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MobilePayPosIdDAO extends AbstractDAO<MobilePayPosId> {
  
  public MobilePayPosId create(String posId, String posUnitId) {
    MobilePayPosId mobilePayPosId = new MobilePayPosId();
    mobilePayPosId.setPosId(posId);
    mobilePayPosId.setPosUnitId(posUnitId);
    return persist(mobilePayPosId);
  }

  public MobilePayPosId findByPosUnitId(String posUnitId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<MobilePayPosId> criteria = criteriaBuilder.createQuery(MobilePayPosId.class);
    Root<MobilePayPosId> root = criteria.from(MobilePayPosId.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.equal(root.get(MobilePayPosId_.posUnitId), posUnitId)
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
}
