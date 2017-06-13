package fi.metatavu.acgbridge.server.transactions;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Singleton
@ApplicationScoped
@Startup
public class TransactionResponsibleNodeUpdater {

  @Inject
  private Instance<TransactionResponsibleNodeUpdateTask> transactionResponsibleNodeUpdateTask;
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @PostConstruct
  public void postConstruct() {
    startTimer(1000, 1000);
  }
  
  private void startTimer(long warmup, long delay) {
    managedScheduledExecutorService.scheduleWithFixedDelay(transactionResponsibleNodeUpdateTask.get(), warmup, delay, TimeUnit.MILLISECONDS);
  }
  
}
