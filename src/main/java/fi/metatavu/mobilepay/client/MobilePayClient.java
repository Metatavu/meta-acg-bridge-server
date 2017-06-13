package fi.metatavu.mobilepay.client;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.mobilepay.io.IOHandler;
import fi.metatavu.mobilepay.io.IOHandlerResult;
import fi.metatavu.mobilepay.model.ErrorResponse;

public class MobilePayClient {
  
  private IOHandler ioHandler;
  
  public MobilePayClient(IOHandler ioHandler) {
    this.ioHandler = ioHandler;
  }
  
  public <T> MobilePayResponse<T> doPostRequest(String url, String body, String authorization, Class<T> resultType) throws IOException {
    IOHandlerResult result = ioHandler.doPost(url, body, authorization);
    if (result.getCode() == 200) {
      return handleOkResponse(result.getResponse(), result.getCode(), result.getMessage(), resultType);
    } else {
      return handleErrorResponse(result.getResponse(), result.getCode(), result.getMessage());
    }
  }
  
  private <T> MobilePayResponse<T> handleOkResponse(String response, int statusCode, String message, Class<T> resultType) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    T entity = objectMapper.readValue(response, resultType);
    return new MobilePayResponse<>(statusCode, message, entity, null);
  }
  
  private <T> MobilePayResponse<T> handleErrorResponse(String response, int statusCode, String message) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    ErrorResponse error = objectMapper.readValue(response, ErrorResponse.class);
    return new MobilePayResponse<>(statusCode, message, null, error);
  }
  
}
