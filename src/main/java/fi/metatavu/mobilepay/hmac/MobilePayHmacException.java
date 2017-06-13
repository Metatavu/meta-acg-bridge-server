package fi.metatavu.mobilepay.hmac;

public class MobilePayHmacException extends Exception {

  private static final long serialVersionUID = 5294216446575321890L;

  public MobilePayHmacException(Exception original) {
    super(original);
  }
  
}