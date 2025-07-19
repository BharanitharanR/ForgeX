package com.batty.forgex.framework.datastore.impl;

import com.batty.forgex.framework.datastore.DatabaseHandler;
import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.Optional;

public class MongoDatastoreImpl<T> implements GenericDatastore<T> {

    private final Class<T> type;
    private final DatabaseHandler handler;

    public MongoDatastoreImpl(Class<T> type, DatabaseHandler handler) {
        this.type = type;
        this.handler = handler;
    }

    @Override
    public boolean insert(T doc) {
        return handler.insertOne(doc);
    }

    @Override
    public Optional<InsertOneResult> insertWithResponse(T doc) {
        return Optional.ofNullable(handler.insertOneResponse(doc));
    }

    @Override
    public Optional<T> findOne(Document query) {
        return Optional.ofNullable(handler.findOne(query, type));
    }

    @Override
    public Optional<T> findOneFromKey(Document query, String key) {
        return Optional.ofNullable(handler.findOneFromKey(query, type, key));
    }

    @Override
    public UpdateResult update(Document query, Document update) {
        return handler.updateOne(query, update,type);
    }

    @Override
    public void createIndex(Document index, IndexOptions options) {
        handler.createIndex(type,index, options);
    }
}
