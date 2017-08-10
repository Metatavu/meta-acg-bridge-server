package fi.metatavu.mobilepay;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.metatavu.acgbridge.server.mobilepay.MobilePayCommonsIOHandler;
import fi.metatavu.mobilepay.client.MobilePayClient;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.model.GetCurrentPaymentResponse;

public class GetCurrentPaymentRequestTest {

  private static final String API_VERSION = "V08";
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule();

  @Test
  public void testGetCurrentPaymentRequest() throws IOException, MobilePayApiException {
    mockMobilePayCommand("basic", "GetCurrentPayment");
    MobilePayApi api = getMobilePayApi();
    
    MobilePayResponse<GetCurrentPaymentResponse> response = api.getCurrentPayment("fake", "POSDK99999", "88888", "123456789012345");
    assertEquals(200, response.getStatus());
    
    GetCurrentPaymentResponse currentPaymentResponse = response.getResponse();

    assertEquals("a123456-b123-c123-d123-e12345678901", currentPaymentResponse.getPoSId());
    assertEquals("100000000000001", currentPaymentResponse.getPosUnitId());
    assertEquals(new Integer(100), currentPaymentResponse.getPaymentStatus());

    assertEquals("123A124321", currentPaymentResponse.getOrderId());
    assertEquals("BA12366351512", currentPaymentResponse.getTransactionId());
    assertEquals(new Double(1023.43), currentPaymentResponse.getAmount());
    assertEquals("f123456-a123-b123-c123-d12345678901", currentPaymentResponse.getCustomerId());
    assertNull(currentPaymentResponse.getCustomerToken());
    assertNull(currentPaymentResponse.getCustomerReceiptToken());
    assertEquals("06-02-2017 09:30:39", currentPaymentResponse.getLastestUpdate());
  }
  
  private MobilePayApi getMobilePayApi() {
    MobilePayCommonsIOHandler mobilePayCommonsIOHandler = new MobilePayCommonsIOHandler();
    MobilePayClient mobilePayClient = new MobilePayClient(mobilePayCommonsIOHandler);
    return new MobilePayApi(mobilePayClient, getMobilePayApiUrl());
  }

  protected void mockMobilePayCommand(String scenario, String command) throws IOException {
    String urlPath = String.format("/%s/%s", API_VERSION, command);
    String requestBody = getMobileyPayMockResource(String.format("%s/%sRequest.json", scenario, command));
    String responseBody = getMobileyPayMockResource(String.format("%s/%sResponse.json", scenario, command));
    
    MappingBuilder mapping = post(urlPathEqualTo(urlPath))
      .withRequestBody(equalToJson(requestBody, true, true)) 
      .willReturn(okJson(responseBody));
    
    wireMockRule.stubFor(mapping);
  }

  private String getMobilePayApiUrl() {
    return "http://localhost:8080";
  }

  private String getMobileyPayMockResource(String resource) throws IOException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
  }
  
}
