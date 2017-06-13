package fi.metatavu.acgbridge.server.mobilepay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.metatavu.mobilepay.hmac.HmacBuilder;
import fi.metatavu.mobilepay.hmac.MobilePayHmacException;

public class HmacBuilderTest {

  @Test
  public void testTestHMAC() throws MobilePayHmacException {
    String orderId = "123A124321";
    String posId = "a123456-b123-c123-d123-e12345678901";
    String merchantId = "POSDK99999";
    String locationId = "88888";
    String amount = "43.33";
    String bulkRef = "MP Bulk Reference";
    
    String hmac = (new HmacBuilder()).createHmac(orderId, posId, merchantId, locationId, amount, bulkRef);
    assertEquals("igy/mfeHupubDAu/GZgDRuzmMTGQ6ECHWZ4kFjMUMss=", hmac);
  }
  
}
