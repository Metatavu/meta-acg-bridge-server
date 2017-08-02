package fi.metatavu.acgbridge.server.mobilepay;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.client.MobilePayClient;

@ApplicationScoped
public class MobilePayApiProducer {
  
  @Inject
  private MobilePayCommonsIOHandler mobilePayCommonsIOHandler;

  @Inject
  private MobilePaySettingsController mobilePaySettingsController;
  
  @Produces
  public MobilePayApi produceMobilePayApi() {
    MobilePayClient mobilePayClient = new MobilePayClient(mobilePayCommonsIOHandler);
    return new MobilePayApi(mobilePayClient, mobilePaySettingsController.getApiUrl());
  }
  
}
