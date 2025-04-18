package com.batty.forgex.entityBuilder;
import com.batty.forgex.entityBuilder.api.EntityApi;
import com.batty.forgex.entityBuilder.datastore.ServiceCollectionDataStoreImpl;
import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.batty.forgex.entityBuilder.model.ServiceCollection;
import com.batty.forgex.framework.datastore.DatabaseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component("EntityBuilderService")
@RestController
public class EntityBuilderService implements EntityApi {

    protected Logger log = LoggerFactory.getLogger(EntityBuilderService.class);
    @Autowired
    protected ServiceCollectionDataStoreImpl dbConnection;

    @Autowired
    protected DatabaseHandler dbHandler;
    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    Optional<InsertOneResult> responseData = Optional.empty();

    AtomicReference<String> resp =new AtomicReference<>();


    @Override
    public ResponseEntity<InlineResponse200> entityProcessPost(MultipartFile message)  {
        try (InputStream inputStream = message.getInputStream())
        {
            ObjectId fileId = dbHandler.uploadFileToGridFS(message.getOriginalFilename(), inputStream, message.getContentType());
            log.info("Stored ZIP in GridFS with ID: {}", fileId.toHexString());
            log.info("filename: {}",message.getOriginalFilename());
            Document entityProcessDocument = new Document();

            entityProcessDocument.put("fileId",fileId.toHexString());
            entityProcessDocument.put("fileName",message.getOriginalFilename());

            ServiceCollection sc = new ServiceCollection();
            sc.setName(fileId.toHexString());
            responseData  = dbConnection.insertDataResponse(sc);
            AtomicReference<String> resp =new AtomicReference<>();
            responseData.ifPresent(
                    (value) ->
                    {
                        resp.set(((BsonObjectId) value.getInsertedId().asObjectId()).getValue().toHexString());
                    });


            message.transferTo(Path.of("/tmp/openapiSpec.zip"));
            // Response Builder
            InlineResponse200 response200 = new InlineResponse200();
            response200.setMessage(resp.get());
            ResponseEntity<InlineResponse200> res = new ResponseEntity<>( response200, HttpStatusCode.valueOf(200));
            return res;
        }
        catch(Exception e)
        {
            log.error("Exception {}",e.getMessage());
            return null;
        }

    }
}
