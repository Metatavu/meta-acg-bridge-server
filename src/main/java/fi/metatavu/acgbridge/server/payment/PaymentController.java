package fi.metatavu.acgbridge.server.payment;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.Transaction;

public class PaymentController {
  
  @Inject
  @Any
  private Instance<PaymentStrategy> paymentStrategies;

  public Response createTransaction(Client client, fi.metatavu.acgbridge.server.rest.model.Transaction payload) {
    PaymentStrategy paymentStrategy = findPaymentStrategy(payload.getPaymentStrategy());
    if (paymentStrategy == null) {
      return Response.status(Status.BAD_REQUEST)
        .entity(String.format("Invalid payment strategy %s", payload.getPaymentStrategy()))
        .build();
    }
    
    Transaction transaction = paymentStrategy.createTransaction(client, payload);    
    if (transaction == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE)
        .entity("Failed to create transaction")
        .build();
    }
    
    if (!paymentStrategy.initatePayment(transaction)) {
      return Response.status(Status.SERVICE_UNAVAILABLE)
        .entity("Failed to initiate payment")
        .build();
    }
    
    return Response.noContent().build();
  }

  private PaymentStrategy findPaymentStrategy(String name) {
    for (PaymentStrategy paymentStrategy : paymentStrategies) {
      if (StringUtils.equals(paymentStrategy.getName(), name)) {
        return paymentStrategy;
      }
    }
    
    return null;
  }
  
  
}
