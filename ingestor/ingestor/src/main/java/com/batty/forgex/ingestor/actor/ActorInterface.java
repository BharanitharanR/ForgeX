package com.batty.forgex.ingestor.actor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public interface ActorInterface {
    ObjectMapper mapper = new ObjectMapper();
    void act(String obj) throws IOException;
}