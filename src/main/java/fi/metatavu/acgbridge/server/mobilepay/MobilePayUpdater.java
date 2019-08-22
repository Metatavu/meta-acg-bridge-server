package fi.metatavu.acgbridge.server.mobilepay;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Singleton
@ApplicationScoped
@Startup
public class MobilePayUpdater {

  @Inject
  @Any
  private Instance<MobilePayUpdateTask> mobilePayUpdateTask;
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @PostConstruct
  public void postConstruct() {
    startTimer(1200, 1200);
  }
  
  private void startTimer(long warmup, long delay) {
    managedScheduledExecutorService.scheduleWithFixedDelay(mobilePayUpdateTask.get(), warmup, delay, TimeUnit.MILLISECONDS);
  }
  
}
