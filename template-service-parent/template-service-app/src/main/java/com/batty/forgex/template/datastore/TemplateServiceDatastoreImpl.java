package com.batty.forgex.template.datastore;

import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.batty.forgex.framework.datastore.provider.DatastoreProvider;
import com.batty.forgex.template.model.Template;
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
public class TemplateServiceDatastoreImpl {

    private static final Logger log = LoggerFactory.getLogger(TemplateServiceDatastoreImpl.class);
    private final GenericDatastore<Template> datastore;

    @Autowired
    public TemplateServiceDatastoreImpl(DatastoreProvider provider) {
        this.datastore = provider.getDatastore(Template.class);
    }


    public Optional<Template> findStatus(String objId) {
        Document query = new Document("_id", new ObjectId(objId));
        return datastore.findOneFromKey(query, "data");
    }

    public Optional<InsertOneResult> insertDataResponse(Template doc) {
            return datastore.insertWithResponse(doc);
        }

    public Optional<Template> findOne(Document query) {
            return datastore.findOne(query);
    }

    @SuppressWarnings(value = "unchecked")
    public UpdateResult updateRecord(Document query, Document update) {
        return datastore.update(query,update);
    }
}
