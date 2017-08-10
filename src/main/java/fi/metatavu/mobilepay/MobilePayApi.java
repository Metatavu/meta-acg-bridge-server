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
import fi.metatavu.mobilepay.model.GetCurrentPaymentRequest;
import fi.metatavu.mobilepay.model.GetCurrentPaymentResponse;
import fi.metatavu.mobilepay.model.GetCurrentReservationRequest;
import fi.metatavu.mobilepay.model.GetCurrentReservationResponse;
import fi.metatavu.mobilepay.model.GetUniquePoSIdRequest;
import fi.metatavu.mobilepay.model.GetUniquePoSIdResponse;
import fi.metatavu.mobilepay.model.PaymentCancelRequest;
import fi.metatavu.mobilepay.model.PaymentCancelResponse;
import fi.metatavu.mobilepay.model.PaymentRefundRequest;
import fi.metatavu.mobilepay.model.PaymentRefundResponse;
import fi.metatavu.mobilepay.model.PaymentStartRequest;
import fi.metatavu.mobilepay.model.PaymentStartResponse;
import fi.metatavu.mobilepay.model.PaymentStatusRequest;
import fi.metatavu.mobilepay.model.PaymentStatusResponse;
import fi.metatavu.mobilepay.model.ReadPoSAssignPoSUnitIdRequest;
import fi.metatavu.mobilepay.model.ReadPoSAssignPoSUnitIdResponse;
import fi.metatavu.mobilepay.model.RegisterPoSRequest;
import fi.metatavu.mobilepay.model.RegisterPoSResponse;
import fi.metatavu.mobilepay.model.ReservationCancelRequest;
import fi.metatavu.mobilepay.model.ReservationCancelResponse;
import fi.metatavu.mobilepay.model.ReservationCaptureRequest;
import fi.metatavu.mobilepay.model.ReservationCaptureResponse;
import fi.metatavu.mobilepay.model.ReservationStartRequest;
import fi.metatavu.mobilepay.model.ReservationStartResponse;
import fi.metatavu.mobilepay.model.ReservationStatusRequest;
import fi.metatavu.mobilepay.model.ReservationStatusResponse;
import fi.metatavu.mobilepay.model.UnAssignPoSUnitIdToPoSResponse;
import fi.metatavu.mobilepay.model.UnAssignPoSUnitIdToPosRequest;
import fi.metatavu.mobilepay.model.UnRegisterPoSRequest;
import fi.metatavu.mobilepay.model.UnRegisterPoSResponse;

public class MobilePayApi {
  
  private static final String API_VERSION = "V08";
  
  private String apiUrl;
  private MobilePayClient client;
  
  public MobilePayApi(MobilePayClient client, String apiUrl) {
    super();
    this.client = client;
    this.apiUrl = apiUrl;
  }

  /**
   * This method is called when the Point of Sale (cash register / terminal) wishes to start a payment 1.
   * PaymentStart is only possible if no active MobilePay payment entity exists for current PoS – 
   * unless it is an update (Action=Update) for a payment entity in state "AwaitTokenRecalc". 
   * A PaymentStart will delete earlier finished payment entities – i.e., payment entities in status 'Done', 'Cancel' or 'Error'.
   * 
   * @param merchantId MerchantId
   * @param locationId Location ID related to current merchant ID and PoS ID.
   * @param posId Current Point of Sale ID (cash register/terminal).
   * @param orderId The OrderId is a unique id that identifies the payment. The OrderId is issued by the merchant and is attached to the payment inside Danske Bank system. The order ID must be unique for the merchant/location combination. This means that there should be only one completed payment with any given order ID for the same merchant and location (store) during the lifetime of the merchant/location. CASE SENTITIVE
   * @param amount The amount for the payment.
   * @param bulkRef An option for grouping the payments – a text or ID. The field has a maximum length of 18 characters. If the field remains empty and the merchant does not have a Bulkpost agreement, the merchant will receive all mobile payments from any connected shops as individual postings in the reconciliation file. If the field remains empty and the merchant does have a Bulkpost agreement, the merchant will receive all mobile payments bulked with a default bulkref of the MP Enterprise Serialnumber value in the reconciliation file. It must be a merchant decision whether they want all individual postings or a bulk posting per store or the entire group as one posting.
   * @param action Action values: "Start": Initiate a payment. "Update": Update a current payment after recalculation.
   * @throws MobilePayApiException
   */
  public MobilePayResponse<PaymentStartResponse> paymentStart(String apiKey, String merchantId, String locationId, String posId, String orderId, Double amount, String bulkRef, String action) throws MobilePayApiException {
    try {
      HmacBuilder hmacBuilder = new HmacBuilder();
      String amountStr = formatAmount(amount);
      Long customerTokenCalc = 0l;
      String hmac = hmacBuilder.createHmac(orderId, posId, merchantId, locationId, amountStr, bulkRef);
      PaymentStartRequest startRequest = new PaymentStartRequest(merchantId, locationId, posId, orderId, amountStr, bulkRef, action, customerTokenCalc, hmac);
      return executeRequest("PaymentStart", apiKey, startRequest, PaymentStartResponse.class);
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
   * @param merchantId MerchantId
   * @param locationId Location ID related to current merchant ID and PoS ID.
   * @param posId Current Point of Sale ID (cash register/terminal).
   * @throws MobilePayApiException
   */
  public MobilePayResponse<PaymentCancelResponse> paymentCancel(String apiKey, String merchantId, String locationId, String posId) throws MobilePayApiException {
    try {
      PaymentCancelRequest cancelRequest = new PaymentCancelRequest(merchantId, locationId, posId);
      return executeRequest("PaymentCancel", apiKey, cancelRequest, PaymentCancelResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * Get a payment status for current PoS ID.
   * 
   * Used for polling a payment status. Polling has to be done every 1 second until the PaymentStatus is 100 ('Done') or if it rejects the payment request (PaymentStatus 40 ('Cancel') or 50 ('Error')).
   * 
   * @param merchantId MerchantId
   * @param locationId Location ID related to current merchant ID and PoS ID.
   * @param posId Current Point of Sale ID (cash register/terminal).
   * @param orderId The OrderId is a unique id that identifies the payment. The OrderId is issued by the merchant and is attached to the payment inside Danske Bank system. The order ID must be unique for the merchant/location combination. This means that there should be only one completed payment with any given order ID for the same merchant and location (store) during the lifetime of the merchant/location. CASE SENTITIVE
   */
  public MobilePayResponse<PaymentStatusResponse> paymentStatus(String apiKey, String merchantId, String locationId, String posId, String orderId) throws MobilePayApiException {
    try {
      PaymentStatusRequest statusRequest = new PaymentStatusRequest(merchantId, locationId, posId, orderId);
      return executeRequest("GetPaymentStatus", apiKey, statusRequest, PaymentStatusResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  /**
   * Refund part of or the entire amount of the payment.
   * 
   * A payment refund can be made days/weeks after the original payment has been made. 
   * 
   * "PaymentRefund" is a stand-alone method and must be called directly. The response code from this call will indicate success or failure.
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @param orderId orderId
   * @param amount amount
   * @param bulkRef bulkRef
   * @return Payment refund response
   * @throws MobilePayApiException
   */
  public MobilePayResponse<PaymentRefundResponse> paymentRefund(String apiKey, String merchantId, String locationId, String posId, String orderId, Double amount, String bulkRef) throws MobilePayApiException {
    try {
      String amountStr = formatAmount(amount);
      PaymentRefundRequest statusRequest = new PaymentRefundRequest(merchantId, locationId, posId, orderId, amountStr, bulkRef);
      return executeRequest("PaymentRefund", apiKey, statusRequest, PaymentRefundResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<GetUniquePoSIdResponse> getUniquePoSId(String apiKey, String merchantId) throws MobilePayApiException {
    try {
      GetUniquePoSIdRequest request = new GetUniquePoSIdRequest(merchantId);
      return executeRequest("GetUniquePoSId", apiKey, request, GetUniquePoSIdResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<RegisterPoSResponse> registerPoS(String apiKey, String merchantId, String locationId, String posId, String name) throws MobilePayApiException {
    try {
      RegisterPoSRequest request = new RegisterPoSRequest(merchantId, locationId, posId, name);
      return executeRequest("RegisterPoS", apiKey, request, RegisterPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<UnRegisterPoSResponse> unregisterPoS(String apiKey, String merchantId, String locationId, String posId) throws MobilePayApiException {
    try {
      UnRegisterPoSRequest request = new UnRegisterPoSRequest(merchantId, locationId, posId);
      return executeRequest("UnRegisterPoS", apiKey, request, UnRegisterPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<AssignPoSUnitIdToPosResponse> assignPoSUnitIdToPos(String apiKey, String merchantId, String locationId, String posId, String posUnitIt) throws MobilePayApiException {
    try {
      AssignPoSUnitIdToPosRequest request = new AssignPoSUnitIdToPosRequest(merchantId, locationId, posId, posUnitIt);
      return executeRequest("AssignPoSUnitIdToPos", apiKey, request, AssignPoSUnitIdToPosResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  public MobilePayResponse<UnAssignPoSUnitIdToPoSResponse> unassignPoSUnitIdToPos(String apiKey, String merchantId, String locationId, String posId, String posUnitIt) throws MobilePayApiException {
    try {
      UnAssignPoSUnitIdToPosRequest request = new UnAssignPoSUnitIdToPosRequest(merchantId, locationId, posId, posUnitIt);
      return executeRequest("UnAssignPoSUnitIdToPos", apiKey, request, UnAssignPoSUnitIdToPoSResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  public MobilePayResponse<ReadPoSAssignPoSUnitIdResponse> readPoSAssignPoSUnitId(String apiKey, String merchantId, String locationId, String posId) throws MobilePayApiException {
    try {
      ReadPoSAssignPoSUnitIdRequest request = new ReadPoSAssignPoSUnitIdRequest(merchantId, locationId, posId);
      return executeRequest("ReadPoSAssignPoSUnitId", apiKey, request, ReadPoSAssignPoSUnitIdResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * This method is called when the Point of Sale (cash register / terminal) wishes to start a reservation 3.
   *  
   * ReservationStart is only possible if no active MobilePay reservation entity exists for current PoS.
   * 
   * A ReservationStart will delete an earlier finished reservation entity - if it is in status 'Done', 'Cancel' or 'Error'.
   * 
   *  It is expected that the PoS system keeps track of reservations internally in order to be able to capture them late
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId 
   * @param orderId orderId
   * @param amount amount
   * @param bulkRef bulkRef
   * @param captureType captureType
   * @return Reservation start response
   * @throws MobilePayApiException
   */
  public MobilePayResponse<ReservationStartResponse> reservationStart(String apiKey, String merchantId, String locationId, String posId, String orderId, Double amount,
      String bulkRef, String captureType) throws MobilePayApiException {
    try {
      String amountStr = formatAmount(amount);
      ReservationStartRequest request = new ReservationStartRequest(merchantId, locationId, posId, orderId, amountStr, bulkRef, captureType);
      return executeRequest("ReservationStart", apiKey, request, ReservationStartResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  /**
   * Get a Reservation status for current PoS ID.
   * 
   * Used for polling for status. Polling has to be done every 1 second until the ReservationStatus is 100 ('Done') or if the reservation request has been rejected (ReservationStatus 40 ('Cancel') or 50 ('Error')).
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @param orderId orderId
   * @return Reservation status response
   * @throws MobilePayApiException
   */
  public MobilePayResponse<ReservationStatusResponse> getReservationStatus(String apiKey, String merchantId, String locationId, String posId, String orderId) throws MobilePayApiException {
    try {
      ReservationStatusRequest request = new ReservationStatusRequest(merchantId, locationId, posId, orderId);
      return executeRequest("GetReservationStatus", apiKey, request, ReservationStatusResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  /**
   * Cancel Reservation request for current PoS ID. Cancel is principal possible as long as earlier request for reservation hasn't been finalized.
   * 
   * A ReservationCancel will delete current reservation entity active or not unless earlier finished reservation ended in status Done (status code 100) which will remains until a new reservation starts.
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @param orderId orderId
   * @return Reservation cancel response
   * @throws MobilePayApiException
   */
  public MobilePayResponse<ReservationCancelResponse> reservationCancel(String apiKey, String merchantId, String locationId, String posId, String orderId) throws MobilePayApiException {
    try {
      ReservationCancelRequest request = new ReservationCancelRequest(merchantId, locationId, posId, orderId);
      return executeRequest("ReservationCancel", apiKey, request, ReservationCancelResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  /**
   * This method is called when the Point of Sale (cash register / terminal) wishes to Capture the Reservation ReservationCapture is only possible when a Reservation exists with the provided order ID.
   * 
   * Reservations made as Full Capture reservations should always be captured with 0.00 in amount, and Partial Capture with the amount to capture
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @param orderId orderId
   * @param amount amount
   * @param bulkRef bulkRef
   * @return Reservation capture response
   * @throws MobilePayApiException
   */
  public MobilePayResponse<ReservationCaptureResponse> reservationCapture(String apiKey, String merchantId, String locationId, String posId, String orderId, Double amount,
      String bulkRef) throws MobilePayApiException {
    try {
      String amountStr = formatAmount(amount);
      ReservationCaptureRequest request = new ReservationCaptureRequest(merchantId, locationId, posId, orderId, amountStr, bulkRef);
      return executeRequest("ReservationCapture", apiKey, request, ReservationCaptureResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * Get the current payment transaction for the provided PoSId.
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @return current payment transaction for the provided PoSId.
   * @throws MobilePayApiException
   */
  public MobilePayResponse<GetCurrentPaymentResponse> getCurrentPayment(String apiKey, String merchantId, String locationId, String posId) throws MobilePayApiException {
    try {
      GetCurrentPaymentRequest request = new GetCurrentPaymentRequest(merchantId, locationId, posId);
      return executeRequest("GetCurrentPayment", apiKey, request, GetCurrentPaymentResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }

  /**
   * Get the current reservation transaction for the provided PoSId.
   * 
   * @param apiKey apiKey
   * @param merchantId merchantId
   * @param locationId locationId
   * @param posId posId
   * @return current reservation transaction for the provided PoSId.
   * @throws MobilePayApiException
   */
  public MobilePayResponse<GetCurrentReservationResponse> getCurrentReservation(String apiKey, String merchantId, String locationId, String posId) throws MobilePayApiException {
    try {
      GetCurrentReservationRequest request = new GetCurrentReservationRequest(merchantId, locationId, posId);
      return executeRequest("GetCurrentReservation", apiKey, request, GetCurrentReservationResponse.class);
    } catch (MobilePayHmacException | IOException e) {
      throw new MobilePayApiException(e);
    }
  }
  
  private String formatAmount(double amount) {
    return String.format("%.2f", amount);
  }
  
  private <T> MobilePayResponse<T> executeRequest(String command, String apiKey, Object payload, Class<T> responseClass) throws JsonProcessingException, MobilePayHmacException, IOException {
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
