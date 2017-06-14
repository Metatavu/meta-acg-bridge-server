package fi.metatavu.acgbridge.server.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.acgbridge.server.persistence.model.Client;
import fi.metatavu.acgbridge.server.security.ClientController;
import fi.metatavu.acgbridge.server.security.HmacSignatureBuilder;
import fi.metatavu.acgbridge.server.security.HmacSignatureException;

@Provider
public class SecurityFilter implements ContainerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  
  @Inject
  private Logger logger; 
  
  @Inject
  private ClientController clientController;
  
  @Inject
  private ClientContainer clientContainer;

  @Context
  private HttpServletRequest request;
  
  @Override
  public void filter(ContainerRequestContext requestContext) {
    String authorizationHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
    if (StringUtils.isBlank(authorizationHeader)) {
      handleUnuauthorized(requestContext, "Missing authorization header");
      return;
    }
    
    String authorization = decodeAuthorization(authorizationHeader);
    if (StringUtils.isBlank(authorization)) {
      handleUnuauthorized(requestContext, "Invalid credentials");
      return;        
    }
    
    String[] credentials = StringUtils.split(authorization, ":", 2);
    if (credentials.length != 2) {
      handleUnuauthorized(requestContext, "Missing credentials");
      return;        
    }
    
    Client client = clientController.findClientByClientId(credentials[0]);
    if (client == null) {
      handleUnuauthorized(requestContext, "Invalid clientId");
      return;        
    }
    
    HmacSignatureBuilder signatureBuilder = new HmacSignatureBuilder(client.getSecretKey());
    String url = requestContext.getUriInfo().getAbsolutePath().toString();
    
    signatureBuilder.append(url);
    
    String method = StringUtils.upperCase(requestContext.getMethod());
    if ("POST".equals(method)) {
      try {
        InputStream entityStream = requestContext.getEntityStream();
        byte[] entityBytes = IOUtils.toByteArray(entityStream);
        signatureBuilder.append(IOUtils.toString(entityBytes, "UTF-8"));
        requestContext.setEntityStream(new ByteArrayInputStream(entityBytes));
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to read entity stream", e);
      }  
    }
    
    try {
      if (!StringUtils.equals(credentials[1], signatureBuilder.build())) {
        handleUnuauthorized(requestContext, "Invalid signature");
        return;        
      }
    } catch (HmacSignatureException e) {
      logger.log(Level.WARNING, "Malformed HMAC signature", e);
      handleUnuauthorized(requestContext, "Malformed signature");
      return;
    }

    clientContainer.setClient(client);
  }
  
  private String decodeAuthorization(String authorization) {
    try {
      return new String(Base64.getDecoder().decode(authorization), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.WARNING, "Invalid credential encoding", e);
      return null;
    }
  }

  private void handleUnuauthorized(ContainerRequestContext requestContext, String message) {
    logger.log(Level.WARNING, () -> String.format("%s from %s", message, getRequestDetails(requestContext)));
    requestContext.abortWith(Response.status(Status.FORBIDDEN).entity(message).build());
  }

  private String getRequestDetails(ContainerRequestContext requestContext) {
    return String.format("%s (%s)", request.getRemoteHost(), requestContext.getHeaderString("User-Agent"));
  }
  
  
}
