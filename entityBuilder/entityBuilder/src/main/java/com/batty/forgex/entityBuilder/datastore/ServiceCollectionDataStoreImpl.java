package com.batty.forgex.entityBuilder.datastore;


import com.batty.forgex.entityBuilder.model.ServiceCollection;
import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.batty.forgex.framework.datastore.provider.DatastoreProvider;
import com.mongodb.client.result.InsertOneResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Optional;

@Component("ServiceCollectionDataStoreImpl")
public class ServiceCollectionDataStoreImpl {


    private static final Logger log = LoggerFactory.getLogger(ServiceCollectionDataStoreImpl.class);

    private final GenericDatastore<ServiceCollection> datastore;

    @Autowired
    public ServiceCollectionDataStoreImpl(DatastoreProvider provider) {
        this.datastore = provider.getDatastore(ServiceCollection.class);
    }


    public Optional<InsertOneResult> insertDataResponse(ServiceCollection doc) {
        return datastore.insertWithResponse(doc);
    }
}
