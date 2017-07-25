package fi.metatavu.mobilepay;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.mobilepay.client.MobilePayClient;
import fi.metatavu.mobilepay.client.MobilePayResponse;
import fi.metatavu.mobilepay.hmac.AuthorizationBuilder;
import fi.metatavu.mobilepay.hmac.HmacBuilder;
import fi.metatavu.mobilepay.hmac.MobilePayHmacException;
import fi.metatavu.mobilepay.model.AssignPoSUnitIdToPosRequest;
import fi.metatavu.mobilepay.model.AssignPoSUnitIdToPosResponse;
import fi.metatavu.mobilepay.model.GetUniquePoSIdRequest;
import fi.metatavu.mobilepay.model.GetUniquePoSIdResponse;
import fi.metatavu.mobilepay.model.PaymentCancelRequest;
import fi.metatavu.mobilepay.model.PaymentCancelResponse;
import fi.metatavu.mobilepay.model.PaymentStartRequest;
import fi.metatavu.mobilepay.model.PaymentStartResponse;
import fi.metatavu.mobilepay.model.PaymentStatusRequest;
import fi.metatavu.mobilepay.model.PaymentStatusResponse;
import fi.metatavu.mobilepay.model.ReadPoSAssignPoSUnitIdRequest;
import fi.metatavu.mobilepay.model.ReadPoSAssignPoSUnitIdResponse;
import fi.metatavu.mobilepay.model.RegisterPoSRequest;
import fi.metatavu.mobilepay.model.RegisterPoSResponse;
import fi.metatavu.mobilepay.model.UnAssignPoSUnitIdToPoSResponse;
import fi.metatavu.mobilepay.model.UnAssignPoSUnitIdToPosRequest;
import fi.metatavu.mobilepay.model.UnRegisterPoSRequest;
import fi.metatavu.mobilepay.model.UnRegisterPoSResponse;

public class MobilePayApi {
  
  private static final String API_VERSION = "V08";
  
  private String apiUrl;
  private String merchantId;
  private String apiKey;
  private MobilePayClient client;
  
  public MobilePayApi(MobilePayClient client, String apiUrl, String merchantId, String apiKey) {
    super();
    this.client = client;
    this.apiUrl = apiUrl;
    this.merchantId = merchantId;
    this.apiKey = apiKey;
  }

  /**
   * This method is called when the Point of Sale (cash register / terminal) wishes to start a payment 1.
   * PaymentStart is only possible if no active MobilePay payment entity exists for current PoS – 
   * unless it is an update (Action=Update) for a payment entity in state "AwaitTokenRecalc". 
   * A PaymentStart will delete earlier finished payment entities – i.e., payment entities in status 'Done', 'Cancel' or 'Error'.
   * 
   * @param locationId Location ID related to current merchant ID and PoS ID.
   * @param posId Current Point of Sale ID (cash register/terminal).
   * @param orderId The OrderId is a unique id that identifies the payment. The OrderId is issued by the merchant and is attached to the payment inside Danske Bank system. The order ID must be unique for the merchant/location combination. This means that there should be only one completed payment with any given order ID for the same merchant and location (store) during the lifetime of the merchant/location. CASE SENTITIVE
   * @param amount The amount for the payment.
   * @param bulkRef An option for grouping the payments – a text or ID. The field has a maximum length of 18 characters. If the field remains empty and the merchant does not have a Bulkpost agreement, the merchant will receive all mobile payments from any connected shops as individual postings in the reconciliation file. If the field remains empty and the merchant does have a Bulkpost agreement, the merchant will receive all mobile payments bulked with a default bulkref of the MP Enterprise Serialnumber value in the reconciliation file. It must be a merchant decision whether they want all individual postings or a bulk posting per store or the entire group as one posting.
   * @param action Action values: "Start": Initiate a payment. "Update": Update a current payment after recalculation.
   * @throws MobilePayApiException
   */
  public MobilePayResponse<PaymentStartResponse> paymentStart(String locationId, String posId, String orderId, Double amount, String bulkRef, String action) throws MobilePayApiException {
    try {
      HmacBuilder hmacBuilder = new HmacBuilder();
      String amountStr = formatAmount(amount);
      Long customerTokenCalc = 0l;
      String hmac = hmacBuilder.createHmac(orderId, posId, merchantId, locationId, amountStr, bulkRef);
      PaymentStartRequest startRequest = new PaymentStartRequest(merchantId, locationId, posId, orderId, amountStr, bulkRef, action, customerTokenCalc, hmac);
      return executeRequest("PaymentStart", startRequest, PaymentStartResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * Cancel payment request for current PoS ID.
   * 
   * Cancel is principal possible as long as earlier request for payment hasn't been finalized (status 100).
   * 
   * A PaymentCancel will delete current payment entity active or not unless earlier finished payment ended in status Done (status code 100) which will remains until a new payment starts.
   * 
   * @param locationId Location ID related to current merchant ID and PoS ID.
   * @param posId Current Point of Sale ID (cash register/terminal).
   * @throws MobilePayApiException
   */
  public MobilePayResponse<PaymentCancelResponse> paymentCancel(String locationId, String posId) throws MobilePayApiException {
    try {
      PaymentCancelRequest cancelRequest = new PaymentCancelRequest(merchantId, locationId, posId);
      return executeRequest("PaymentCancel", cancelRequest, PaymentCancelResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * Get a payment status for current PoS ID.
   * 
   * Used for polling a payment status. Polling has to be done every 1 second until the PaymentStatus is 100 ('Done') or if it rejects the payment request (PaymentStatus 40 ('Cancel') or 50 ('Error')).
   */
  public MobilePayResponse<PaymentStatusResponse> paymentStatus(String locationId, String posId, String orderId) throws MobilePayApiException {
    try {
      PaymentStatusRequest statusRequest = new PaymentStatusRequest(merchantId, locationId, posId, orderId);
      return executeRequest("GetPaymentStatus", statusRequest, PaymentStatusResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<GetUniquePoSIdResponse> getUniquePoSId() throws MobilePayApiException {
    try {
      GetUniquePoSIdRequest request = new GetUniquePoSIdRequest(merchantId);
      return executeRequest("GetUniquePoSId", request, GetUniquePoSIdResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<RegisterPoSResponse> registerPoS(String locationId, String posId, String name) throws MobilePayApiException {
    try {
      RegisterPoSRequest request = new RegisterPoSRequest(merchantId, locationId, posId, name);
      return executeRequest("RegisterPoS", request, RegisterPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<UnRegisterPoSResponse> unregisterPoS(String locationId, String posId) throws MobilePayApiException {
    try {
      UnRegisterPoSRequest request = new UnRegisterPoSRequest(merchantId, locationId, posId);
      return executeRequest("UnRegisterPoS", request, UnRegisterPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPoSUnitIdToPos(String locationId, String posId, String posUnitIt) throws MobilePayApiException {
    try {
      AssignPoSUnitIdToPosRequest request = new AssignPoSUnitIdToPosRequest(merchantId, locationId, posId, posUnitIt);
      return executeRequest("AssignPoSUnitIdToPos", request, AssignPoSUnitIdToPosResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  public MobilePayResponse<UnAssignPoSUnitIdToPoSResponse> unassignPoSUnitIdToPos(String locationId, String posId, String posUnitIt) throws MobilePayApiException {
    try {
      UnAssignPoSUnitIdToPosRequest request = new UnAssignPoSUnitIdToPosRequest(merchantId, locationId, posId, posUnitIt);
      return executeRequest("UnAssignPoSUnitIdToPos", request, UnAssignPoSUnitIdToPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<ReadPoSAssignPoSUnitIdResponse> readPoSAssignPoSUnitId(String locationId, String posId) throws MobilePayApiException {
    try {
      ReadPoSAssignPoSUnitIdRequest request = new ReadPoSAssignPoSUnitIdRequest(merchantId, locationId, posId);
      return executeRequest("ReadPoSAssignPoSUnitId", request, ReadPoSAssignPoSUnitIdResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  private String formatAmount(double amount) {
    return String.format("%.2f", amount);
  }
  
  private <T> MobilePayResponse<T> executeRequest(String command, Object payload, Class<T> responseClass) throws JsonProcessingException, MobilePayHmacException, IOException {
    String url = String.format("%s/%s/%s", apiUrl, API_VERSION, command);
    AuthorizationBuilder authorizationBuilder = new AuthorizationBuilder();
    String requestBody = toJson(payload);
    long timeStampUtc = OffsetDateTime.now().toEpochSecond();
    String authorization = authorizationBuilder.createAuthorization(apiKey, url, requestBody, timeStampUtc);
    return client.doPostRequest(url, requestBody, authorization, responseClass);
  }

  private String toJson(Object object) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(object);
  }
  
  
}
