package com.batty.forgex.framework.datastore;

import com.batty.forgex.framework.interfaces.DatastoreInterface;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mongodb.client.model.Updates.*;

@Component
public class DatastoreUtil {
    protected Logger log = LoggerFactory.getLogger(DatastoreUtil.class);

    protected IndexOptions idx;

    protected Filters filter;

    protected Updates updates;

    public IndexOptions getOptions()
    {
        this.idx = new IndexOptions();
        return this.idx;

    }

    public Filters getFilter()
    {
        return this.filter;
    }

    public Updates getUpdates() {
        return updates; }
}
