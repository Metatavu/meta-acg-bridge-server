package fi.metatavu.acgbridge.server.persistence.model;

public enum TransactionStatus {

  PENDING,
  
  ERRORED, 
  
  CANCELLED,
  
  SUCCESS,
  
  TIMED_OUT
  
}
