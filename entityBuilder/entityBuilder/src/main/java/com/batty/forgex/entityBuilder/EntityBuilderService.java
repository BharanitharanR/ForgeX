package com.batty.forgex.entityBuilder;
import com.batty.forgex.entityBuilder.api.EntityApi;
import com.batty.forgex.entityBuilder.datastore.DatastoreImpl;

import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component("EntityBuilderService")
@RestController
public class EntityBuilderService implements EntityApi {

    protected Logger log = LoggerFactory.getLogger(EntityBuilderService.class);
    @Autowired
    protected DatastoreImpl dbConnection;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    Optional<InsertOneResult> responseData = Optional.empty();

    AtomicReference<String> resp =new AtomicReference<>();


    @Override
    public ResponseEntity<InlineResponse200> entityProcessPost(MultipartFile message)  {
        try
        {
            Document entityProcessDocument = new Document();
            String json = mapper.writeValueAsString(entityProcessDocument);
            entityProcessDocument.put("data", json);
            responseData  = dbConnection.insertDataResponse(entityProcessDocument);
            AtomicReference<String> resp =new AtomicReference<>();
            responseData.ifPresent(
                    (value) ->
                    {
                        resp.set(((BsonObjectId) value.getInsertedId().asObjectId()).getValue().toHexString());
                    });

            log.info("filename: {}",message.getName());
            message.transferTo(Path.of("/tmp/openapiSpec.zip"));
            return null;
        }
        catch(Exception e)
        {
            return null;
        }

    }
}
