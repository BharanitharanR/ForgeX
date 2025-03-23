package com.batty.forgex.ingestor.actor;

import com.batty.forgex.entityBuilder.api.DefaultApi;
import com.batty.forgex.entityBuilder.api.client.ApiClient;
import com.batty.forgex.entityBuilder.api.model.InlineResponse200;
import com.batty.forgex.entityBuilder.api.model.Node;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("EntityBuilderActor")
@EnableAutoConfiguration
public class EntityBuilderActor implements ActorInterface {

    protected Logger log = LoggerFactory.getLogger(EntityBuilderActor.class);

    @Value("${entityBuilder.service.hostname}")
    public String entityBuilderHostname;

    @Override
    public void act(String obj) {
        try {
            log.info("data in act {} ", obj);
            log.info("entityBuilderHostname {}",entityBuilderHostname);
            // Set up the API client
            com.batty.forgex.entityBuilder.api.client.Configuration.setDefaultApiClient(
                    new ApiClient().setBasePath(entityBuilderHostname)
            );
            DefaultApi entityBuilderSDK = new DefaultApi();

            List<Node> nodeList = mapper.readValue(
                    obj, new TypeReference<List<Node>>() {}
            );
            InlineResponse200 response = entityBuilderSDK.entityProcessPost(mapper.readValue(obj, new TypeReference<List<Node>>() {}));
        } catch (Exception e) {
            log.error("Exception {}", e.getMessage(), e); // Log the full exception for debugging
        }
    }


}
