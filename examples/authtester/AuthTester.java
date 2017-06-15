package fi.metatavu.acgbridge.server.rest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class AuthTester {

  public static void main(String[] args) throws Exception {
    // Esimerkissä body ladataan tiedostosta. Normaalisti tämä tulisi palvelimelta
    String body = IOUtils.toString(AuthTester.class.getClassLoader().getResourceAsStream("test.json"), "UTF-8");
    // Polku, johon kutsu lähetetään
    String path = "/v1/transactions";
    // ClientId sekä secret
    String clientId = "test-client";
    String secretKey = "mega-secret";
    
    // Luodaan signaturen sisäĺtö yhdistämällä url -osoite sekä body erottimella |
    String signatureContents = String.format("%s|%s", path, body);
    // Lasketaan signaturesta HMAC
    String hmac = calculateHmac(signatureContents, secretKey);
    // Yhdistetään clientId ja hmac merkillä : ja base64 enkoodataan tulos
    String authorization = Base64.encodeBase64String(String.format("%s:%s", clientId, hmac).getBytes("UTF-8"));
    // Tästä tuloksena Authorization headerin sisältö eli esimerkissä:
    System.out.println(authorization);
  }
  
  private static String calculateHmac(String hmacContent, String secretKey) throws Exception {
    byte[] keyBytes = secretKey.getBytes("UTF-8");
    byte[] contentBytes = hmacContent.getBytes("UTF-8");
    return calculateHmac(contentBytes, keyBytes);    
  }

  private static String calculateHmac(byte[] contentBytes, byte[] keyBytes) throws Exception {
    Mac hmacSha256 = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
    hmacSha256.init(secretKeySpec);
    byte[] hmacBytes = hmacSha256.doFinal(contentBytes);
    return Base64.encodeBase64String(hmacBytes);
  }

}
