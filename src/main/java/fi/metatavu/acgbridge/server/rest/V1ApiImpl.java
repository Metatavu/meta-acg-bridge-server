package fi.metatavu.acgbridge.server.rest;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.EnumUtils;

import fi.metatavu.acgbridge.server.payment.PaymentController;
import fi.metatavu.acgbridge.server.payment.PaymentStrategy;
import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.persistence.model.TransactionStatus;
import fi.metatavu.acgbridge.server.rest.model.Transaction;
import fi.metatavu.acgbridge.server.transactions.TransactionController;

@RequestScoped
@Stateful
public class V1ApiImpl extends fi.metatavu.acgbridge.server.rest.V1Api {
  
  @Inject
  private PaymentController paymentController;
  
  @Inject
  private TransactionController transactionController;
  
  @Inject
  private ClientContainer clientContainer;
  
  @Override
  public Response createTransaction(Transaction payload, Request request) {
    Client client = clientContainer.getClient();    

    PaymentStrategy paymentStrategy = paymentController.findPaymentStrategy(payload.getPaymentStrategy());
    if (paymentStrategy == null) {
      return Response.status(Status.BAD_REQUEST)
        .entity(String.format("Invalid payment strategy %s", payload.getPaymentStrategy()))
        .build();
    }
    
    paymentStrategy.cancelActiveTransactions(payload.getMachineId());
    
    fi.metatavu.acgbridge.server.persistence.model.Transaction transaction = paymentStrategy.createTransaction(client, payload);    
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
    
    payload.setId(transaction.getId().toString());
    
    return Response.ok(payload).build();
  }
  
  @Override
  public Response updateTransaction(String transactionId, Transaction payload, Request request) {
    try {
      Long id = Long.parseLong(transactionId);
      TransactionStatus transactionStatus = EnumUtils.getEnum(TransactionStatus.class, payload.getStatus());
      if (transactionStatus == null) {
        return Response.status(Status.BAD_REQUEST).entity(String.format("Invalid transaction status %s", transactionStatus)).build();
      }
      
      fi.metatavu.acgbridge.server.persistence.model.Transaction transaction = transactionController.findTransactionById(id);
      if (transaction != null) {
        if (paymentController.updateTransactionStatus(transaction, transactionStatus) != null) {
          return Response.status(Status.NO_CONTENT).build();
        } else {
          return Response.status(Status.BAD_REQUEST).entity(String.format("Invalid transaction status %s", transactionStatus)).build();
        }
      } else {
        return Response.status(Status.NOT_FOUND).build();
      }
      
    } catch (NumberFormatException e) {
      return Response.status(Status.BAD_REQUEST).entity(String.format("Malformed id %s", transactionId)).build();
    }
  }
  
  @Override
  public Response cancelTransaction(String transactionId, Request request) {
    try {
      Long id = Long.parseLong(transactionId);
      
      fi.metatavu.acgbridge.server.persistence.model.Transaction transaction = transactionController.findTransactionById(id);
      if (transaction != null) {
        paymentController.cancelTransaction(transaction, TransactionStatus.CANCELLED);
        return Response.status(Status.NO_CONTENT).build();
      } else {
        return Response.status(Status.NOT_FOUND).build();
      }
      
    } catch (NumberFormatException e) {
      return Response.status(Status.BAD_REQUEST).entity(String.format("Malformed id %s", transactionId)).build();
    }
  }

  @Override
  public Response getSystemPing(Request request) {
    return Response.ok("PONG").build();
  }

}
