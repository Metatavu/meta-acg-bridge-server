package fi.metatavu.acgbridge.server.rest;

import javax.enterprise.context.RequestScoped;

import fi.metatavu.acgbridge.server.persistence.model.Client;

@RequestScoped
public class ClientContainer {

  private Client client;
  
  public void setClient(Client client) {
    this.client = client;
  }
  
  public Client getClient() {
    return client;
  }
  
}
