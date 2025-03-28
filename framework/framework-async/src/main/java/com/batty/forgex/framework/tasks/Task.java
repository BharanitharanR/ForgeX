package com.batty.forgex.framework.tasks;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface Task<T> {

  public String getName();
  public <U> void setObject(U obj, Class<U> type) ;

    public  <U> U getObject(Class<U> type);

    @Async("forgexAsyncExecutor")
    CompletableFuture<T> execute();
}
