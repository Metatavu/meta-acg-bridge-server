package fi.metatavu.acgbridge.server.mobilepay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.metatavu.mobilepay.hmac.AuthorizationBuilder;
import fi.metatavu.mobilepay.hmac.MobilePayHmacException;

public class AuthorizationBuilderTest {

  @Test
  public void testTestHMAC() throws MobilePayHmacException {
    String apiKey = "1234567890";
    String requestUrl = "https://localhost:9003/API/V07/PaymentCancel";
    String contentBody = "{\"POSId\":\"a123456-b123-c123-d123-e12345678901\",\"LocationId\":\"88888\",\"MerchantId\":\"POSDK99999\"}";
    long timeStampUtc = 1413794400l;
    String authorization = (new AuthorizationBuilder()).createAuthorization(apiKey, requestUrl, contentBody, timeStampUtc);
    assertEquals("isGAb6zjnrOmHEj/d7ZE6VTEggY5zQqX7bnJ1/Y0gjc= 1413794400", authorization);
  }
  
}
