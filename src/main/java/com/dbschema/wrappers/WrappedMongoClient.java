package com.dbschema.wrappers;

import com.dbschema.ScanStrategy;
import com.dbschema.schema.MetaCollection;
import com.dbschema.schema.MetaDatabase;
import com.dbschema.schema.MetaField;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import com.mongodb.event.ServerMonitorListener;
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
    private final String uri;
    private final HashMap<String, MetaDatabase> metaDatabases = new HashMap<>();
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
        getDatabaseNames();
    }

    public PingStatus pingServer(){
        final long start = System.currentTimeMillis();
        while( pingStatus == PingStatus.INIT ){
            try { Thread.sleep( 200 ); } catch (InterruptedException ex ){LOGGER.info("Wait for heartbeat message");}
            if ( System.currentTimeMillis() - start > 8000 ) pingStatus = PingStatus.TIMEOUT;
        }
        return pingStatus;
    }

    public void close(){
        mongoClient.close();
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
                if( !metaDatabases.containsKey(c ) ){
                    metaDatabases.put( c, new MetaDatabase(c ));
                }
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

    public Collection<MetaDatabase> getMetaDatabases(){
        return metaDatabases.values();
    }

    public MetaDatabase getMetaDatabase( String name ){
        return metaDatabases.get( name );
    }


    public void clear() {
        metaDatabases.clear();
    }


    public String getVersion(){
        return "1.1";
    }


    public MetaCollection getMetaCollection(String catalogName, String collectionName){
        if ( collectionName == null || collectionName.length() == 0 ) return null;
        int idx = collectionName.indexOf('.');
        if ( idx > -1 ) collectionName = collectionName.substring(0, idx );

        if ( metaDatabases.isEmpty() ){
            getDatabaseNames();
        }
        MetaDatabase metaDatabase = metaDatabases.get( catalogName );
        if ( metaDatabase != null ) {
            MetaCollection metaCollection = metaDatabase.getCollection(collectionName);
            if (metaCollection == null) {
                metaCollection = discoverCollection(metaDatabase, collectionName);
            }
            return metaCollection;
        }
        return null;

    }

    public String getURI() {
        return uri;
    }


    public List<String> getCollectionNames(String databaseName) {
        List<String> list = new ArrayList<String>();
        try {
            WrappedMongoDatabase db = getDatabase(databaseName);
            if ( db != null ){
                for ( Document doc : db.listCollections()){
                    if ( doc.containsKey("type") && "collection".equals(doc.get("type"))){
                        list.add( String.valueOf( doc.get("name")) );
                    }
                }
            }
            list.remove("system.indexes");
            list.remove("system.users");
            list.remove("system.views");
            list.remove("system.version");
        } catch ( Throwable ex ){
            LOGGER.log(Level.SEVERE, "Cannot list collection names for " + databaseName + ". ", ex );
        }
        return list;
    }

    public List<String> getViewNames(String databaseName) {
        List<String> list = new ArrayList<String>();
        try {
            WrappedMongoDatabase db = getDatabase(databaseName);
            if ( db != null ){
                for ( Document doc : db.listCollections()){
                    if ( doc.containsKey("type") && "view".equals(doc.get("type"))){
                        list.add( String.valueOf( doc.get("name")) );
                    }
                }
            }
        } catch ( Throwable ex ){
            LOGGER.log(Level.SEVERE, "Cannot list collection names for " + databaseName + ". ", ex );
        }
        return list;
    }

    private MetaCollection discoverCollection(MetaDatabase metaDatabase, String collectionName){
        final WrappedMongoDatabase mongoDatabase = getDatabase(metaDatabase.name);
        if ( mongoDatabase != null ){
            try {
                final WrappedMongoCollection mongoCollection = mongoDatabase.getCollection( collectionName );
                if ( mongoCollection != null ){
                    return metaDatabase.createCollection( mongoCollection, collectionName, scanStrategy );
                }
            } catch ( Throwable ex ){
                LOGGER.log(Level.SEVERE, "Error discovering collection " + metaDatabase + "." + collectionName + ". ", ex );
            }
        }
        return null;
    }


    private WrappedMongoCollection getWrappedMongoCollection(MetaDatabase metaDatabase, String collectionName ){
        final WrappedMongoDatabase mongoDatabase = getDatabase( metaDatabase.name );
        if ( mongoDatabase != null ){
            return mongoDatabase.getCollection( collectionName );
        }
        return null;
    }



    public void discoverReferences(MetaCollection master ){
        if ( !master.referencesDiscovered){
            try {
                master.referencesDiscovered = true;
                final List<MetaField> unsolvedFields = new ArrayList<>();
                final List<MetaField> solvedFields = new ArrayList<>();
                master.collectFieldsWithObjectId(unsolvedFields);
                if ( !unsolvedFields.isEmpty() ){
                    for ( MetaCollection _metaCollection : master.metaDatabase.getMetaCollections() ){
                        final WrappedMongoCollection mongoCollection = getWrappedMongoCollection( _metaCollection.metaDatabase, _metaCollection.name );
                        if ( mongoCollection != null ){
                            for ( MetaField metaField : unsolvedFields ){
                                for ( ObjectId objectId : metaField.objectIds){
                                    final Document query = new Document(); //new BasicDBObject();
                                    query.put("_id", objectId);
                                    if ( !solvedFields.contains( metaField ) && mongoCollection.find(query).iterator().hasNext()) {
                                        solvedFields.add( metaField );
                                        metaField.createReferenceTo(_metaCollection);
                                        System.out.println("Found ref " + metaField.parentObject.name + " ( " + metaField.name + " ) ref " + _metaCollection.name);
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
