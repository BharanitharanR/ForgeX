package com.batty.forgex.ingestor.datastore;
import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.batty.forgex.framework.datastore.provider.DatastoreProvider;
import com.batty.forgex.ingestor.model.ServiceCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
// NO LONGER VALID - Just hanging here as a good little sample
@Component("ServiceDataStoreImpl")
public class ServiceDataStoreImpl {

    private static final Logger log = LoggerFactory.getLogger(GraphInputDatastoreImpl.class);
    private final GenericDatastore<ServiceCollection> datastore;

    @Autowired
    public ServiceDataStoreImpl(DatastoreProvider provider) {
        this.datastore = provider.getDatastore(ServiceCollection.class);
    }


    public Optional<ServiceCollection> findStatus(String objId) {
        Document query = new Document("_id", new ObjectId(objId));
        return datastore.findOneFromKey(query, "data");
    }

    public Optional<ServiceCollection> findOne(Document query) {
        return datastore.findOne(query);
    }

    @SuppressWarnings(value = "unchecked")
    public UpdateResult updateRecord(Document query, Document update) {
        return datastore.update(query,update);
    }

    public Optional<InsertOneResult> insertDataResponse(ServiceCollection doc) {
        return datastore.insertWithResponse(doc);
    }


}
