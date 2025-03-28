package com.batty.forgex.ingestor.actor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ActorInterface {
    ObjectMapper mapper = new ObjectMapper();
    void act(String obj) throws IOException;
}