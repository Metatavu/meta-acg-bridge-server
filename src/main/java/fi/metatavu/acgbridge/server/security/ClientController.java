package fi.metatavu.acgbridge.server.security;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.persistence.dao.ClientDAO;
import fi.metatavu.acgbridge.server.persistence.model.Client;

@ApplicationScoped
public class ClientController {
  
  @Inject
  private ClientDAO clientDAO;

  public Client findClientByClientId(String clientId) {
    return clientDAO.findByClientId(clientId);
  }

}