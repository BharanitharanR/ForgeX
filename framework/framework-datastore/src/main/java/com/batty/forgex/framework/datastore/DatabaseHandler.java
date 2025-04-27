package com.batty.forgex.framework.datastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.bson.conversions.Bson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

@Component
@Primary
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
public class DatabaseHandler {
    protected Logger log = LoggerFactory.getLogger(DatabaseHandler.class);
    private boolean isDBConnected;

    protected MongoClient client;
    protected MongoDatabase database;
    private GridFSBucket gridFSBucket;

    @Value("${mongodb.atlas.connection}")
    public String dbConnectionString;

    @Value("${mongodb.database.name}")
    public String dbName;

    @Value("${mongodb.collection.name}")
    public String collectionName;
    @Autowired
    protected ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        log.info("Initialized + db string: " + dbConnectionString);
        this.connectToDB();
    }

    public boolean isDataStoreConnected() {
        return this.isDBConnected;
    }

    private void connectToDB() {
        try {
            this.client = MongoClients.create(dbConnectionString);
            this.database = this.client.getDatabase(dbName);
            this.gridFSBucket = GridFSBuckets.create(this.database);
            isDBConnected = true;
        } catch (Exception e) {
            log.error("Connection error: " + e.getMessage());
            isDBConnected = false;
        }
    }

    private <T> MongoCollection<Document> getCollection(Class<T> clazz) {
        String collectionName = this.collectionName;// clazz.getSimpleName();
        return this.database.getCollection(collectionName);
    }

    public <T> boolean insertOne(T doc) {
        try {
            Document document = Document.parse(objectMapper.writeValueAsString(doc));
            document.append("lastModifiedTimeStamp", new Date());
            getCollection(doc.getClass()).insertOne(document);
            return true;
        } catch (Exception e) {
            log.error("Insert error", e);
            return false;
        }
    }

    public <T> InsertOneResult insertOneResponse(T doc) {
        try {
            Document document = Document.parse(objectMapper.writeValueAsString(doc));
            document.append("lastModifiedTimeStamp", new Date());
            return getCollection(doc.getClass()).insertOne(document);
        } catch (Exception e) {
            log.error("Insert failed", e);
            throw new RuntimeException("Insert failed", e);
        }
    }

    public <T> UpdateResult updateOne(Document query, Document doc, Class<T> clazz) {
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);
        try {
            return getCollection(clazz).updateOne(query, doc, updateOptions);
        } catch (Exception e) {
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }

    public <T> Long removeOne(Bson query, Class<T> clazz) {
        try {
            return getCollection(clazz).deleteOne(query).getDeletedCount();
        } catch (Exception e) {
            log.error("Remove error", e);
            return 0L;
        }
    }

    public <T> T findOne(Document query, Class<T> clazz) {
        try {
            Document response = getCollection(clazz).find(query).first();
            return response != null ? objectMapper.readValue(response.toJson(), clazz) : null;
        } catch (Exception e) {
            log.error("Find error", e);
            return null;
        }
    }

    public <T> T findOneFromKey(Document query, Class<T> clazz, String key) {
        try {
            Document response = getCollection(clazz).find(query).first();
            return response != null ? objectMapper.readValue((String) response.get(key), clazz) : null;
        } catch (Exception e) {
            log.error("Find from key error", e);
            return null;
        }
    }

    public <T> Document findOneRaw(Document query, Class<T> clazz) {
        try {
            return getCollection(clazz).find(query).first();
        } catch (Exception e) {
            log.error("Find raw error", e);
            return null;
        }
    }

    public <T> void createIndex(Class<T> clazz, Document doc, IndexOptions options) {
        getCollection(clazz).createIndex(doc, options);
    }

    public ObjectId uploadFileToGridFS(String filename, InputStream inputStream, String contentType) {
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1024 * 1024)
                .metadata(new Document("type", contentType));
        return gridFSBucket.uploadFromStream(filename, inputStream, options);
    }

    public byte[] downloadFileFromGridFS(String filename) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(filename, outputStream);
        return outputStream.toByteArray();
    }

    public void deleteFileFromGridFS(String filename) {
        GridFSFile file = gridFSBucket.find(new Document("filename", filename)).first();
        if (file != null) {
            gridFSBucket.delete(file.getObjectId());
        }
    }
}
