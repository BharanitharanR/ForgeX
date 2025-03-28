package com.batty.forgex.ingestor.service;


import com.batty.forgex.ingestor.IngestorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


/*
 *  With thread pool managed by the Framework.
 *  This AsyncService Handles asynchronous methods across the service
 */
@Service
public class AsyncService {
    protected Logger log = LoggerFactory.getLogger(IngestorService.class);


    /*
     *  With thread pool managed by the Framework.
     *  This AsyncService Handles asynchronous methods across the service
     */
    @Async("forgexAsyncExecutor")
    public CompletableFuture<String> testingCompletable(String user) throws InterruptedException {
        log.info("Running in thread: " + Thread.currentThread().getName());
        log.info("Looking up " + user);
        String url = String.format("https://api.github.com/users/%s", user);

        // Artificial delay of 1s for demonstration purposes
        Thread.sleep(1000L);
        return CompletableFuture.completedFuture(user);
    }
}
