package com.dbschema.wrappers;

import com.dbschema.ScanStrategy;
import com.dbschema.schema.MetaCollection;
import com.dbschema.schema.MetaField;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.event.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.logging.Level;

import static com.dbschema.MongoJdbcDriver.LOGGER;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedMongoClient {

    private final MongoClient mongoClient;
    private final String databaseName;
    private String uri;
    private final HashMap<String, MetaCollection> metaCollections = new HashMap<String,MetaCollection>();
    private final ScanStrategy scanStrategy;
    public final boolean expandResultSet;
    public enum PingStatus{ INIT, SUCCEED, FAILED, TIMEOUT }
    private PingStatus pingStatus = PingStatus.INIT;


    class LocalServerMonitorListener implements ServerMonitorListener{
        @Override
        public void serverHearbeatStarted(ServerHeartbeatStartedEvent serverHeartbeatStartedEvent) {}
        @Override
        public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent serverHeartbeatSucceededEvent) {
            pingStatus = PingStatus.SUCCEED;
        }
        @Override
        public void serverHeartbeatFailed(ServerHeartbeatFailedEvent serverHeartbeatFailedEvent) {
            pingStatus = PingStatus.FAILED;
        }
    }


    public WrappedMongoClient(String uri, final Properties prop, final ScanStrategy scanStrategy, boolean expandResultSet ){
        final MongoClientOptions.Builder builder = MongoClientOptions.builder().
                addServerMonitorListener(new LocalServerMonitorListener());
        final MongoClientURI clientURI = new MongoClientURI(uri, builder);
        this.databaseName = clientURI.getDatabase();
        this.mongoClient = new MongoClient(clientURI );
        this.uri = uri;
        this.expandResultSet = expandResultSet;
        this.scanStrategy = scanStrategy;
    }

    public PingStatus pingServer(){
        final long start = System.currentTimeMillis();
        while( pingStatus == PingStatus.INIT ){
            try { Thread.sleep( 200 ); } catch (InterruptedException ex ){LOGGER.info("Wait for heartbeat message");}
            if ( System.currentTimeMillis() - start > 8000 ) pingStatus = PingStatus.TIMEOUT;
        }
        return pingStatus;
    }

    public MongoClientOptions getMongoClientOptions() {
        return mongoClient.getMongoClientOptions();
    }

    public List<MongoCredential> getCredentialsList() {
        return mongoClient.getCredentialsList();
    }

    public MongoCredential getCredential() {
        return mongoClient.getCredential();
    }

    public MongoIterable<String> listDatabaseNames() {
        return mongoClient.listDatabaseNames();
    }

    public ListDatabasesIterable<Document> listDatabases() {
        return mongoClient.listDatabases();
    }

    public <T> ListDatabasesIterable<T> listDatabases(Class<T> clazz) {
        return mongoClient.listDatabases(clazz);
    }

    // USE STATIC SO OPENING A NEW CONNECTION WILL REMEMBER THIS
    public static final List<String> createdDatabases = new ArrayList<String>();


    public String getCurrentDatabaseName() {
        // SEE THIS TO SEE HOW DATABASE NAME IS USED : http://api.mongodb.org/java/current/com/mongodb/MongoClientURI.html
        return databaseName != null ? databaseName : "admin";
    }

    public List<String> getDatabaseNames() {
        final List<String> names = new ArrayList<String>();
        try {
            // THIS OFTEN THROWS EXCEPTION BECAUSE OF MISSING RIGHTS. IN THIS CASE WE ONLY ADD CURRENT KNOWN DB.
            for ( String c : listDatabaseNames() ){
                names.add( c );
            }
        } catch ( Throwable ex ){
            names.add( getCurrentDatabaseName() );
        }
        for ( String str : createdDatabases ){
            if ( !names.contains( str )){
                names.add( str );
            }
        }
        return names;
    }

    public WrappedMongoDatabase getDatabase(String dbName) {
        return new WrappedMongoDatabase( mongoClient.getDatabase(dbName));
    }

    public List<WrappedMongoDatabase> getDatabases() {
        final List<WrappedMongoDatabase> list = new ArrayList<WrappedMongoDatabase>();

        for ( String dbName : getDatabaseNames() ){
            list.add( getDatabase(dbName));
        }
        return list;
    }

    public Collection<MetaCollection> getMetaCollections(){
        return metaCollections.values();
    }


    public void clear() {
        metaCollections.clear();
        referencesDiscovered = false;
    }


    public String getVersion(){
        return "1.1";
    }


    public MetaCollection getMetaCollection(String catalogName, String collectionName){
        if ( collectionName == null || collectionName.length() == 0 ) return null;
        int idx = collectionName.indexOf('.');
        if ( idx > -1 ) collectionName = collectionName.substring(0, idx );

        String key = catalogName + "." + collectionName;
        MetaCollection metaCollection = metaCollections.get( key );
        if ( metaCollection == null ){
            metaCollection = discoverCollection( catalogName, collectionName );
            if ( metaCollection != null ){
                metaCollections.put( key, metaCollection );
            }
        }
        return metaCollection;

    }

    public String getURI() {
        return uri;
    }


    public List<String> getCollectionNames(String databaseName) {
        List<String> list = new ArrayList<String>();
        try {
            WrappedMongoDatabase db = getDatabase(databaseName);
            if ( db != null ){
                for ( String str : db.listCollectionNames() ){
                    list.add( str );
                }
            }
            list.remove("system.indexes");
            list.remove("system.users");
            list.remove("system.version");
        } catch ( Throwable ex ){
            LOGGER.log(Level.SEVERE, "Cannot list collection names for " + databaseName + ". ", ex );
        }
        return list;
    }


    private MetaCollection discoverCollection(String dbOrCatalog, String collectionName){
        final WrappedMongoDatabase mongoDatabase = getDatabase(dbOrCatalog);
        if ( mongoDatabase != null ){
            try {
                final WrappedMongoCollection mongoCollection = mongoDatabase.getCollection( collectionName );
                if ( mongoCollection != null ){
                    return new MetaCollection( mongoCollection, dbOrCatalog, collectionName, scanStrategy );
                }
            } catch ( Throwable ex ){
                LOGGER.log(Level.SEVERE, "Error discovering collection " + dbOrCatalog + "." + collectionName + ". ", ex );
            }
        }
        return null;
    }


    private WrappedMongoCollection getWrappedMongoCollection(String databaseName, String collectionName ){
        final WrappedMongoDatabase mongoDatabase = getDatabase( databaseName );
        if ( mongoDatabase != null ){
            return mongoDatabase.getCollection( collectionName );
        }
        return null;
    }


    private boolean referencesDiscovered = false;

    public void discoverReferences(){
        if ( !referencesDiscovered){
            try {
                referencesDiscovered = true;
                final List<MetaField> unsolvedFields = new ArrayList<>();
                final List<MetaField> solvedFields = new ArrayList<>();
                for ( MetaCollection collection : metaCollections.values() ){
                    collection.collectFieldsWithObjectId(unsolvedFields);
                }
                if ( !unsolvedFields.isEmpty() ){
                    for ( MetaCollection collection : metaCollections.values() ){
                        final WrappedMongoCollection mongoCollection = getWrappedMongoCollection( collection.db, collection.name );
                        if ( mongoCollection != null ){
                            for ( MetaField metaField : unsolvedFields ){
                                for ( ObjectId objectId : metaField.objectIds){
                                    final Document query = new Document(); //new BasicDBObject();
                                    query.put("_id", objectId );
                                    if ( !solvedFields.contains( metaField ) && mongoCollection.find( query ).iterator().hasNext() ){
                                        solvedFields.add( metaField );
                                        metaField.createReferenceTo(collection);
                                        System.out.println("Found ref " + metaField.parentJson.name + " ( " + metaField.name + " ) ref " + collection.name );
                                    }
                                }
                            }
                        }
                    }
                }

            } catch ( Throwable ex ){
                LOGGER.log( Level.SEVERE, "Error in discover foreign keys", ex );
            }
        }
    }



}
