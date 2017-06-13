package fi.metatavu.mobilepay.hmac;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class AbstractHmacBuilder {

  protected String calculateHmac(String hmacContent, String key, String encoding) throws MobilePayHmacException {
    try {
      byte[] keyBytes = key.getBytes(encoding);
      byte[] contentBytes = hmacContent.getBytes(encoding);
      return calculateHmac(keyBytes, contentBytes);
    } catch (UnsupportedEncodingException e) {
      throw new MobilePayHmacException(e);
    }
  }

  protected String calculateHmac(byte[] keyBytes, byte[] contentBytes) throws MobilePayHmacException {
    try {
      Mac hmacSha256 = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
      hmacSha256.init(secretKeySpec);
      byte[] hmacBytes = hmacSha256.doFinal(contentBytes);
      return Base64.getEncoder().encodeToString(hmacBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new MobilePayHmacException(e);
    }
  }
  
}
