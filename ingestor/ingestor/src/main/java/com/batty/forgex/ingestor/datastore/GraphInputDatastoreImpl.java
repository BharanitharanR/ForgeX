package com.batty.forgex.ingestor.datastore;

import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.batty.forgex.framework.datastore.provider.DatastoreProvider;
import com.batty.forgex.ingestor.model.GraphInput;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("GraphInputDatastoreImpl")
public class GraphInputDatastoreImpl {

    private static final Logger log = LoggerFactory.getLogger(GraphInputDatastoreImpl.class);
    private final GenericDatastore<GraphInput> datastore;

    @Autowired
    public GraphInputDatastoreImpl(DatastoreProvider provider) {
        this.datastore = provider.getDatastore(GraphInput.class);
    }


    public Optional<GraphInput> findStatus(String objId) {
        Document query = new Document("_id", new ObjectId(objId));
        return datastore.findOneFromKey(query, "data");
    }

    public Optional<InsertOneResult> insertDataResponse(GraphInput doc) {
            return datastore.insertWithResponse(doc);
        }

    public Optional<GraphInput> findOne(Document query) {
            return datastore.findOne(query);
    }

    @SuppressWarnings(value = "unchecked")
    public UpdateResult updateRecord(Document query, Document update) {
        return datastore.update(query,update);
    }
}
