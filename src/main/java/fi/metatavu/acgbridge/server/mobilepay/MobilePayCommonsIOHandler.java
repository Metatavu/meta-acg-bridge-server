package fi.metatavu.acgbridge.server.mobilepay;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import fi.metatavu.mobilepay.io.IOHandler;
import fi.metatavu.mobilepay.io.IOHandlerResult;

@ApplicationScoped
public class MobilePayCommonsIOHandler implements IOHandler {
  
  @Inject
  private Logger logger;

  @Override
  public IOHandlerResult doPost(String url, String data, String authorization) throws IOException {
    try {
      CloseableHttpClient httpClient = createClient();
      try {
        return executePostRequest(httpClient, url, data, authorization);
      } finally {
        closeClient(httpClient);
      }
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      logger.log(Level.SEVERE, "Failed to create MobilePay io handler", e);
      throw new IOException(e);
    }
  }

  private CloseableHttpClient createClient() throws KeyManagementException, NoSuchAlgorithmException {
    // MobilePay uses deprecated TLSv1 certificates
    return HttpClients.custom()
      .setSSLContext(SSLContexts.custom().useProtocol("TLSv1").build())
      .build();
  }

  private IOHandlerResult executePostRequest(CloseableHttpClient httpClient, String url, String data, String authorization) throws IOException {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(new StringEntity(data, "UTF-8"));
    
    return executeRequest(httpClient, httpPost, authorization);
  }
  
  private IOHandlerResult executeRequest(CloseableHttpClient httpClient, HttpPost request, String authorization) throws IOException {
    request.addHeader("Authorization", authorization);
    request.addHeader("Content-Type", "application/json");
    
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      StatusLine statusLine = response.getStatusLine();
      String message = statusLine.getReasonPhrase();
      int code = statusLine.getStatusCode();
      String content = getResponseContent(response, code);      
      return new IOHandlerResult(code, message, content);
    }
  }

  private String getResponseContent(CloseableHttpResponse response, int code) throws IOException {
    if (code != 204) {
      HttpEntity entity = response.getEntity();
      try {
        return IOUtils.toString(entity.getContent(), "UTF-8");
      } finally {
        EntityUtils.consume(entity);
      }
    }
    
    return null;
  }
  
  private void closeClient(CloseableHttpClient httpClient) {
    try {
      httpClient.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to close http client", e);
    }
  }
  
}
