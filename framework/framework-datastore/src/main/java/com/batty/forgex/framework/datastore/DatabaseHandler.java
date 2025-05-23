package com.batty.forgex.framework.datastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.stereotype.Component;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.conversions.Bson;

import java.util.Date;
import java.util.List;


@Component
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class DatabaseHandler {
    protected Logger log = LoggerFactory.getLogger(DatabaseHandler.class);
    private boolean isDBConnected;

    protected MongoClient client;

    protected  MongoDatabase database;

    protected MongoCollection collection;


    @Value("${mongodb.atlas.connection}")
    public String dbConnectionString;

    @Value("${mongodb.database.name}")
    public String dbName;

    @Value("${mongodb.collection.name}")
    public String collectionName;


    @Autowired
    protected ObjectMapper objectMapper;

    @PostConstruct
    public void initialize()
    {
        log.info("Intialized + db string"+ dbConnectionString);
        this.connectToDB();

    }
    public  boolean isDataStoreConnected()
    {
        return this.isDBConnected;
    }

    private boolean createCollection() {
        boolean returnStatus = false;
        try
        {
            this.database.createCollection(collectionName);
            returnStatus = true;

        }
        catch(Exception e)
        {
            log.info("Exception : "+e);
            returnStatus = false;
        }
        finally {
            return returnStatus;
        }
    }

    private void connectToDB()
    {
        try {
            this.client = MongoClients.create(dbConnectionString);
            this.database  = this.client.getDatabase(dbName);

            log.info("Collection status: " +  ( (createCollection()) ? "Collection created":"Collection exists"));
            this.collection = this.database.getCollection(collectionName);


        }
        catch(Exception ignored) {
            log.info("Connection error" + ignored.getMessage());
        }
    }

    public void createIndex(Document doc,IndexOptions idx) {
        this.collection.createIndex(doc, idx);
    }

    // Exposed framework function to insert a doc
    public boolean insertOne(Document doc)
    {
        boolean status = false;
        InsertOneResult result = null;
        try {
            doc.append("lastModifiedTimeStamp", new Date());
            result = this.collection.insertOne(doc);
            if(result.getInsertedId() != null ) { status = true; }
            log.info("is inserted:"+result.getInsertedId());
        }
        catch(Exception ignored)
        {
            status = false;
        }
        finally
        {
            return status;
        }
    }

    public boolean updateOne(Document query, Document doc)
    {
        boolean status = false;
        UpdateResult result = null;
        // Set upsert to true, caller have to handle passing the doc efficiently
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);
        try
        {
            result = this.collection.updateOne(query, doc, updateOptions);
            if(result.getModifiedCount() >= 1 ) { status = true; }
            log.info("is inserted:"+result.getUpsertedId());
        }
        catch(Exception ignored)
        {
            log.info("update failed:"+ignored);
            status = false;
        }
        finally
        {
            return status;
        }
    }
    public Long removeOne(Bson doc) {
        Long response = 0L;
        try {
               return this.collection.deleteOne(doc).getDeletedCount();
        } catch (Exception e) {
            log.info("find error: " + e);
            return response;
        }
    }


    public Object findOne(Document doc)
    {
        Document response = null;
        try
        {
            response = (Document) this.collection.find(doc).first();
            if(response != null ) {
                System.out.println("_id: " + response.getObjectId("_id")
                        + ", name: " + response.getString("userId"));
            }
        } catch(Exception e) {
            log.info("find error :"+e);
        } finally {
            return response;
        }
    }

    public <T> T findOne(Document doc, Class<T> clazz) {
        try {
            Document response = (Document) this.collection.find(doc).first();
            if (response != null) {
                return objectMapper.readValue(response.toJson(), clazz);
            }
        } catch (Exception e) {
            log.info("find error: " + e);
        }
        return null;
    }

}
