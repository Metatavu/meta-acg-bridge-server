package fi.metatavu.acgbridge.server.payment;

import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;

public interface PaymentStrategy {
  
  public String getName();
  
  public Transaction createTransaction(Client client, fi.metatavu.acgbridge.server.rest.model.Transaction payload);

  public boolean initatePayment(Transaction transaction);

}
