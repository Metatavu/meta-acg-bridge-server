package fi.metatavu.acgbridge.server.mobilepay;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.acgbridge.server.persistence.dao.MobilePayPosIdDAO;
import fi.metatavu.acgbridge.server.persistence.model.MobilePayPosId;
import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.MobilePayApiException;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.model.AssignPoSUnitIdToPosResponse;
import fi.metatavu.mobilepay.model.GetUniquePoSIdResponse;
import fi.metatavu.mobilepay.model.ReadPoSAssignPoSUnitIdResponse;
import fi.metatavu.mobilepay.model.RegisterPoSResponse;
import fi.metatavu.mobilepay.model.UnAssignPoSUnitIdToPoSResponse;

@ApplicationScoped
public class MobilePayPosIdController {

  @Inject
  private Logger logger;
  
  @Inject
  private MobilePayApi mobilePayApi;
  
  @Inject
  private MobilePayPosIdDAO mobilePayPosIdDAO;
  
  public String getPosId(String posUnitId, String locationId, String name) throws MobilePayApiException {
    MobilePayPosId mobilePayPosId = mobilePayPosIdDAO.findByPosUnitId(posUnitId);
    String exisitingPosId = mobilePayPosId != null ? mobilePayPosId.getPosId() : null;
    String obtainedPosId = ensurePos(exisitingPosId, posUnitId, locationId, name);
    if (obtainedPosId == null) {
      return null;
    }
    
    if (!StringUtils.equals(exisitingPosId, obtainedPosId)) {
      if (mobilePayPosId != null) {
        mobilePayPosIdDAO.delete(mobilePayPosId);
      }
      
      mobilePayPosIdDAO.create(obtainedPosId, posUnitId);
    }
    
    return obtainedPosId;
  }
  
  private String ensurePos(String existingPosId, String existingPosUnitId, String locationId, String name) throws MobilePayApiException {
    if (existingPosId == null) {
      return obtainFreshPosId(existingPosUnitId, locationId, name);
    } else {
      return ensureExistingPosId(existingPosId, existingPosUnitId, locationId, name);
    }
  }

  private String ensureExistingPosId(String existingPosId, String existingPosUnitId, String locationId, String name) throws MobilePayApiException {
    MobilePayResponse<ReadPoSAssignPoSUnitIdResponse> readPoSassignUnitIdResponse = mobilePayApi.readPoSAssignPoSUnitId(locationId, existingPosId);
    if (!readPoSassignUnitIdResponse.isOk()) {
      MobilePayResponse<RegisterPoSResponse> registerPosResponse = mobilePayApi.registerPoS(locationId, existingPosId, name);
      if (!registerPosResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to register pos id [%d]: %s", registerPosResponse.getStatus(), registerPosResponse.getMessage()));
        return null;
      }
      
      MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPosUnitIdResponse = mobilePayApi.assignPoSUnitIdToPos(locationId, existingPosId, name);
      if (!assignPosUnitIdResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to assign pos id [%d]: %s", assignPosUnitIdResponse.getStatus(), assignPosUnitIdResponse.getMessage()));
        return null;
      }
      
      return existingPosId;
    } else {
      return reassignExistingPosId(existingPosId, existingPosUnitId, locationId, name, readPoSassignUnitIdResponse);
    }
  }

  private String reassignExistingPosId(String existingPosId, String existingPosUnitId, String locationId, String name, MobilePayResponse<ReadPoSAssignPoSUnitIdResponse> readPoSassignUnitIdResponse) throws MobilePayApiException {
    String responsePosUnitId = readPoSassignUnitIdResponse.getResponse().getPosUnitId();
    if (StringUtils.isNotBlank(existingPosUnitId)) {
      if (StringUtils.equals(existingPosUnitId, responsePosUnitId)) {
        return existingPosId;
      } else {
        MobilePayResponse<UnAssignPoSUnitIdToPoSResponse> unAssignPosUnitIdResponse = mobilePayApi.unassignPoSUnitIdToPos(locationId, existingPosId, name);
        if (!unAssignPosUnitIdResponse.isOk()) {
          logger.log(Level.SEVERE, () -> String.format("Failed to unassign pos id [%d]: %s", unAssignPosUnitIdResponse.getStatus(), unAssignPosUnitIdResponse.getMessage()));
          mobilePayApi.unregisterPoS(locationId, existingPosId);
          return null;
        }
        
        MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPoSUnitIdToPosResponse = mobilePayApi.assignPoSUnitIdToPos(locationId, existingPosId, existingPosUnitId);
        if (!assignPoSUnitIdToPosResponse.isOk()) {
          logger.log(Level.SEVERE, () -> String.format("Failed to reassign pos id [%d]: %s", assignPoSUnitIdToPosResponse.getStatus(), assignPoSUnitIdToPosResponse.getMessage()));
          mobilePayApi.unregisterPoS(locationId, existingPosId);
          return null;
        }
        
        return existingPosId;
      }
    } else {
      MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPoSUnitIdToPosResponse = mobilePayApi.assignPoSUnitIdToPos(locationId, existingPosId, existingPosUnitId);
      if (!assignPoSUnitIdToPosResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to reassign pos id [%d]: %s", assignPoSUnitIdToPosResponse.getStatus(), assignPoSUnitIdToPosResponse.getMessage()));
        mobilePayApi.unregisterPoS(locationId, existingPosId);
        return null;
      }
      
      return existingPosId;
    }
  }

  private String obtainFreshPosId(String existingPosUnitId, String locationId, String name) throws MobilePayApiException {
    MobilePayResponse<GetUniquePoSIdResponse> uniquePoSIdResponse = mobilePayApi.getUniquePoSId();
    if (!uniquePoSIdResponse.isOk()) {
      logger.log(Level.SEVERE, () -> String.format("Failed to get unique pos id [%d]: %s", uniquePoSIdResponse.getStatus(), uniquePoSIdResponse.getMessage()));
      return null;
    }
    
    String posId = uniquePoSIdResponse.getResponse().getPoSId();
    MobilePayResponse<RegisterPoSResponse> registerPosResponse = mobilePayApi.registerPoS(locationId, posId, name);
    if (!registerPosResponse.isOk()) {
      logger.log(Level.SEVERE, () -> String.format("Failed to register pos id [%d]: %s", registerPosResponse.getStatus(), registerPosResponse.getMessage()));
      return null;
    } else {
      // Get PosUnitid from posunit box using usb, og read from settings file or database
      MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPosUnitIdResponse = mobilePayApi.assignPoSUnitIdToPos(locationId, posId, existingPosUnitId);
      if (!assignPosUnitIdResponse.isOk()) {
        logger.log(Level.SEVERE, () -> String.format("Failed to assign pos id [%d]: %s", assignPosUnitIdResponse.getStatus(), assignPosUnitIdResponse.getMessage()));
        return null;
      }
      
      return posId;
    }
  }
}
