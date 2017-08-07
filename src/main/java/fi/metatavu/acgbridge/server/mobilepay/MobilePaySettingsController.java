package fi.metatavu.acgbridge.server.mobilepay;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.acgbridge.server.settings.SystemSettingController;

@ApplicationScoped
public class MobilePaySettingsController {

  private static final String APIURL_SETTING = "mobilepay.apiurl";
  private static final String APIKEY_SETTING = "mobilepay.%s.apikey";

  @Inject
  private SystemSettingController systemSettingController;

  public String getApiKey(String merchantId) {
    return systemSettingController.getSettingValue(String.format(APIKEY_SETTING, merchantId));
  }

  public String getApiUrl() {
    return systemSettingController.getSettingValue(APIURL_SETTING);
  }

}
