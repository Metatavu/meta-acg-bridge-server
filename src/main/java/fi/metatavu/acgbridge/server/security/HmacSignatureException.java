package fi.metatavu.acgbridge.server.security;

public class HmacSignatureException extends Exception {

  private static final long serialVersionUID = -4559181697980293291L;

  public HmacSignatureException(Exception e) {
    super(e);
  }
  
}
