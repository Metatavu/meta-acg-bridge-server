package fi.metatavu.mobilepay.hmac;

public class AuthorizationBuilder extends AbstractHmacBuilder {
  
  public String createAuthorization(String apiKey, String requestUrl, String contentBody, long timeStampUtc) throws MobilePayHmacException {
    String hmacContent = String.format("%s %s %d", requestUrl, contentBody, timeStampUtc);
    return getAuthorizationHeader(hmacContent, apiKey, timeStampUtc);
  }
  
  private String getAuthorizationHeader(String hmacContent, String apiKey, long timeStampUtc) throws MobilePayHmacException {
    String base64Hmac = calculateHmac(hmacContent, apiKey, "UTF-8");
    return String.format("%s %d", base64Hmac, timeStampUtc);
  }
  
}
