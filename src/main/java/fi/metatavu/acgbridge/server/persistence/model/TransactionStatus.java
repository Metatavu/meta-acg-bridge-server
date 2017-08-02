package fi.metatavu.acgbridge.server.persistence.model;

public enum TransactionStatus {

  PENDING,
  
  REFUSED,
  
  ERRORED, 
  
  CANCELLED,
  
  SUCCESS,
  
  TIMED_OUT
  
}
