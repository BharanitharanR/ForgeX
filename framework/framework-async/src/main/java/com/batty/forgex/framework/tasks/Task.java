package com.batty.forgex.framework.tasks;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface Task<T> {


  public String getName();
  public <U> void setObject(U obj, Class<U> type) ;

    public  <U> U getObject(Class<U> type);

    public void setParentId(String id);

    public String getParentId();

    CompletableFuture<T> execute();
}
