package com.batty.forgex.framework.datastore.interfaces;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.Optional;

public interface GenericDatastore<T> {
    boolean insert(T doc);
    Optional<InsertOneResult> insertWithResponse(T doc);
    Optional<T> findOne(Document query);
    Optional<T> findOneFromKey(Document query, String key);
    UpdateResult update(Document query, Document update);
    void createIndex(Document index, com.mongodb.client.model.IndexOptions options);
}
