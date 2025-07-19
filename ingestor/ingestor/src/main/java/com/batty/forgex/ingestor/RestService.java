package com.batty.forgex.ingestor;

import com.batty.forgex.ingestor.api.HikeListApi;
import com.batty.forgex.ingestor.datastore.ServiceDataStoreImpl;
import com.batty.forgex.ingestor.model.RTPHikers;
import com.batty.forgex.ingestor.model.ServiceCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component("ServiceController")
@RestController
public class RestService implements HikeListApi
{
    @Autowired
    protected ServiceDataStoreImpl dbConnection;
  // https://mydeveloperplanet.com/2022/02/08/generate-server-code-using-openapi-generator/

  // https://github.com/mydeveloperplanet/myopenapiplanet/tree/master
    @Override
    public ResponseEntity<RTPHikers> addUser(String userId) {
        try {
            RTPHikers response = new RTPHikers();
            ServiceCollection sc = new ServiceCollection();
            sc.setName(userId);
            Document servicecollection =  new Document();
            if( dbConnection.insertDataResponse(sc).isPresent())
           {
                response.setUserID(userId);
                response.setName(userId);
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException();
            }
        }
        catch(Exception e)
        {
            return (ResponseEntity<RTPHikers>) ResponseEntity.internalServerError();
        }

    }

    @Override
    public ResponseEntity<RTPHikers> getUser(String userId) {
        RTPHikers response = new RTPHikers();
        String data = String.valueOf(dbConnection.findStatus(userId));
        response.setUserID(data);
        response.setName(data);

        return ResponseEntity.ok(response);
    }


}
