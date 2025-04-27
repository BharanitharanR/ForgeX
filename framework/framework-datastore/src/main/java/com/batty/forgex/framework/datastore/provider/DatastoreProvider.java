package com.batty.forgex.framework.datastore.provider;


import com.batty.forgex.framework.datastore.DatabaseHandler;
import com.batty.forgex.framework.datastore.impl.MongoDatastoreImpl;
import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("ForgexDataStoreProvider")
@Primary
public class DatastoreProvider {

    @Autowired
    private DatabaseHandler handler;

    public <T> GenericDatastore<T> getDatastore(Class<T> clazz) {
        return new MongoDatastoreImpl<>(clazz, handler);
    }
}
