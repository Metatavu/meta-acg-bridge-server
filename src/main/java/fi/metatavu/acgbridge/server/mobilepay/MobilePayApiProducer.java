package fi.metatavu.acgbridge.server.mobilepay;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.settings.SystemSettingController;
import fi.metatavu.mobilepay.MobilePayApi;
import fi.metatavu.mobilepay.client.MobilePayClient;

@ApplicationScoped
public class MobilePayApiProducer {

  private static final String APIKEY_SETTING = "mobilepay.apikey";
  private static final String MERCHANTID_SETTING = "mobilepay.merchantid";
  private static final String APIURL_SETTING = "mobilepay.apiurl";

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private MobilePayCommonsIOHandler mobilePayCommonsIOHandler;
  
  @Produces
  public MobilePayApi produceMobilePayApi() {
    MobilePayClient mobilePayClient = new MobilePayClient(mobilePayCommonsIOHandler);
    return new MobilePayApi(mobilePayClient, getApiUrl(), getMerchantId(), getApiKey());
  }

  private String getApiUrl() {
    return systemSettingController.getSettingValue(APIURL_SETTING);
  }
  
  private String getMerchantId() {
    return systemSettingController.getSettingValue(MERCHANTID_SETTING);
  }
  
  private String getApiKey() {
    return systemSettingController.getSettingValue(APIKEY_SETTING);
  }
  
}
