package com.batty.forgex.entityBuilder.datastore;


import com.batty.forgex.framework.interfaces.DatastoreInterface;
import com.batty.forgex.entityBuilder.model.ServiceCollection;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.batty.forgex.framework.datastore.DatabaseHandler;
import com.batty.forgex.framework.datastore.DatastoreUtil;
import java.util.concurrent.TimeUnit;

@Component("ServiceDataStoreImpl")
public class DatastoreImpl implements DatastoreInterface {

    protected Logger log = LoggerFactory.getLogger(DatastoreImpl.class);

    @Autowired
    protected DatabaseHandler datastore;

    @Autowired
    protected DatastoreUtil utils;

    @PostConstruct
    public void initialize()
    {
        createIndex();
    }
    @Override
    public void createIndex() {
        try
        {
            Document index = new Document();
            index.put("ingestId",1);
            this.datastore.createIndex(index, this.utils.getOptions().unique(true));
            index.clear();
            index.put("lastModifiedTimeStamp",1);
            /*    Set expiry so as to avoid space hold up in cloud db
                 this.datastore.createIndex(index, this.utils.getOptions().expireAfter(15L, TimeUnit.SECONDS));
             */
            this.datastore.createIndex(index,this.utils.getOptions());
        }
        catch(Exception e)
        {

        }
    }
    public boolean insertData(Document doc)
    {
        try
        {
            return this.datastore.insertOne(doc) ;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public ServiceCollection findUser(String userId)
    {
        Object status = null;
        ServiceCollection coll = new ServiceCollection();
        try
        {
            Document doc = new Document();
            doc.put("userId",userId);
            //doc.put("name",userId);
            coll = this.datastore.findOne(doc,ServiceCollection.class);
            log.info("Data from DB: "+coll.toString());
            return coll;

        }
        catch(Exception e)
        {
            log.info("Error while fetch"+ e);
            status = "empty";
            return null;
        }
    }
}
