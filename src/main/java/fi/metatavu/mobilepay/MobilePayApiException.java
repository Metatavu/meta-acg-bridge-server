package fi.metatavu.mobilepay;

public class MobilePayApiException extends Exception {

  private static final long serialVersionUID = 7249443966145916654L;
  
  public MobilePayApiException(Exception original) {
    super(original);
  }
  
}
