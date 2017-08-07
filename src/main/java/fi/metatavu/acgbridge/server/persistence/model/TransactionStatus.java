package fi.metatavu.acgbridge.server.persistence.model;

public enum TransactionStatus {

  PENDING,
  
  REFUSED,
  
  ERRORED, 
  
  CANCELLED,
  
  SUCCESS,
  
  WAITING_CAPTURE,
  
  TIMED_OUT
  
}
