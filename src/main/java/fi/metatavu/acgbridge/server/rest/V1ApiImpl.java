package fi.metatavu.acgbridge.server.rest;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import fi.metatavu.acgbridge.server.payment.PaymentController;
import fi.metatavu.acgbridge.server.rest.model.Transaction;

@RequestScoped
@Stateful
public class V1ApiImpl extends fi.metatavu.acgbridge.server.rest.V1Api {
  
  @Inject
  private PaymentController paymentController;
  
  @Override
  public Response createTransaction(Transaction transaction, Request request) {
    return paymentController.createTransaction(transaction);
  }

}
