package fi.metatavu.acgbridge.server.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

public class HmacSignatureBuilder {
  
  private String key;
  private List<String> parts;
  
  public HmacSignatureBuilder(String key) {
    this.key = key;
    this.parts = new ArrayList<>();
  }
  
  public HmacSignatureBuilder append(String value) {
    parts.add(value);
    return this;
  }

  public HmacSignatureBuilder append(Double value) {
    return this.append(String.valueOf(value));
  }
  
  public String build() throws HmacSignatureException {
    String payload = StringUtils.join(parts.toArray(new String[0]), "|");
    return calculateHmac(payload, key);
  }
  
  private String calculateHmac(String hmacContent, String key) throws HmacSignatureException {
    try {
      byte[] keyBytes = key.getBytes("UTF-8");
      byte[] contentBytes = hmacContent.getBytes("UTF-8");
      return calculateHmac(keyBytes, contentBytes);
    } catch (UnsupportedEncodingException e) {
      throw new HmacSignatureException(e);
    }
  }

  private String calculateHmac(byte[] keyBytes, byte[] contentBytes) throws HmacSignatureException {
    try {
      Mac hmacSha256 = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
      hmacSha256.init(secretKeySpec);
      byte[] hmacBytes = hmacSha256.doFinal(contentBytes);
      return Base64.getEncoder().encodeToString(hmacBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new HmacSignatureException(e);
    }
  }
  
}
