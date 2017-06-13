package fi.metatavu.mobilepay.hmac;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HmacBuilder extends AbstractHmacBuilder {
  
  public String createHmac(String orderId, String posId, String merchantId, String locationId, String amount, String bulkRef) throws MobilePayHmacException {
    try {
      String alias = String.format("%s%s", merchantId, locationId);
      String payload = String.format("%s#%s#%s#%s#%s#", alias, posId, orderId, amount, bulkRef);
      byte[] payloadBytes = payload.getBytes("ISO-8859-1");
      byte[] merchantKey = getMerchantKey(alias);
      return calculateHmac(merchantKey, payloadBytes);
    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      throw new MobilePayHmacException(e);
    }
  }

  private byte[] getMerchantKey(String alias) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    String merchantIdSub = alias.substring(3, 9);
    return getSha256(merchantIdSub);
  }
  
  private byte[] getSha256(String value) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    byte[] isoBytes = value.getBytes("ISO-8859-1");
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return digest.digest(isoBytes);   
  }
  
}
