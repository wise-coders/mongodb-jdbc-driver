package com.dbschema.wrappers;

import com.dbschema.GraalConvertor;
import com.dbschema.MongoJdbcDriver;
import com.dbschema.ScanStrategy;
import com.dbschema.Util;
import com.dbschema.structure.MetaCollection;
import com.dbschema.structure.MetaDatabase;
import com.dbschema.structure.MetaField;
import com.google.gson.GsonBuilder;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dbschema.MongoJdbcDriver.LOGGER;


/**
 * Wrapper class around MongoDatabase with direct access to collections as member variables.
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedMongoDatabase implements ProxyObject {

    private final MongoDatabase mongoDatabase;
    private final ScanStrategy scanStrategy;
    public final MetaDatabase metaDatabase;

    public static final Logger LOGGER = Logger.getLogger( WrappedMongoDatabase.class.getName() );

    WrappedMongoDatabase( MongoDatabase mongoDatabase, ScanStrategy scanStrategy ){
        this.mongoDatabase = mongoDatabase;
        this.scanStrategy = scanStrategy;
        this.metaDatabase = new MetaDatabase(mongoDatabase.getName());
        for ( Document info : mongoDatabase.listCollections()){
            Document definition = (Document) Util.getByPath(info, "options.validator.$jsonSchema");
            if ( definition != null ){
                final String name = info.getString("name");
                final MetaCollection metaCollection = metaDatabase.createCollection( name );
                LOGGER.log(Level.INFO, "--- Collection " + name + "\n\n" + new GsonBuilder().setPrettyPrinting().create().toJson(definition) + "\n"); ;
                try {
                    metaCollection.visitValidatorNode(null, true, definition);
                } catch ( Throwable ex ){
                    LOGGER.log(Level.SEVERE, "Error parsing validation rule.", ex );
                    metaDatabase.dropCollection( name );
                }
            }
        }
    }


    public MetaCollection getMetaCollection( String collectionName){
        if ( collectionName == null || collectionName.length() == 0 ) return null;

        final MetaCollection metaCollection = metaDatabase.getCollection(collectionName);
        if (metaCollection == null) {
            try {
                return metaDatabase.createCollection( collectionName ).scanDocuments( getCollection(collectionName), scanStrategy );
            } catch ( Throwable ex ){
                LOGGER.log(Level.SEVERE, "Error discovering collection " + mongoDatabase.getName() + "." + collectionName + ". ", ex );
            }
        } else {
            return metaCollection;
        }
        return null;
    }

    @Override
    public boolean hasMember(String key) {
        return true;
    }

    @Override
    public Object getMember(String key) {
        switch (key){
            case "createView": return new CreateViewProxyExecutable();
            case "getCollection" : return new GetCollectionProxyExecutable();
            case "createCollection" : return new CreateCollectionProxyExecutable();
            case "runCommand" : return new RunCommandProxyExecutable();
            case "drop" : return new DropProxyExecutable();
            case "listCollectionNames" : return new ListCollectionNamesProxyExecutable();
            case "listCollections" : return new ListCollectionsProxyExecutable();
            case "getViewSource" : return new GetViewSourceProxyExecutable();
            case "getName" : return new GetNameProxyExecutable();
            default: return getCollection( key );
        }
    }

    public WrappedMongoCollection<Document> getCollection(String collectionName) {
        return new WrappedMongoCollection<>( this, mongoDatabase.getCollection(collectionName));
    }

    @Override
    public Object getMemberKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for ( String name : mongoDatabase.listCollectionNames() ){
            keys.add( name );
        }
        return keys.toArray();
    }

    @Override
    public void putMember(String key, Value value) {
    }


    @Override
    public String toString() {
        return mongoDatabase.getName();
    }

    private class CreateViewProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 3 && args[0].isString() && args[1].isString() && args[2].hasArrayElements()) {
                mongoDatabase.createView(args[0].asString(), args[1].asString(), GraalConvertor.toList( args[2].as(List.class)) );
            }
            return null;
        }
    }
    private class GetCollectionProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 && args[0].isString() ) {
                LOGGER.log(Level.INFO, "Get collection " + args[0].asString() + " " + getCollection( args[0].asString() ));
                return getCollection( args[0].asString() );
            }
            return null;
        }
    }
    private class CreateCollectionProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 && args[0].isString() ) {
                mongoDatabase.createCollection(args[0].asString());
            }
            if( args.length == 2 && args[0].isString()){
                if ( args[1].isHostObject() ) {
                    mongoDatabase.createCollection( args[0].asString(), args[1].asHostObject() );
                }
                final Map map = args[1].as(Map.class);
                if ( map != null ){
                    CreateCollectionOptions options = new CreateCollectionOptions();
                    if ( map.containsKey("validator") ) {
                        options.validationOptions(new ValidationOptions().validator(GraalConvertor.toBson(map.get("validator"))));
                    }
                    if ( map.containsKey("storageEngine") ) {
                        options.storageEngineOptions(GraalConvertor.toBson(map.get("storageEngine")));
                    }
                    if ( map.containsKey("capped") ) {
                        options.capped(Boolean.parseBoolean( String.valueOf( map.get("capped"))));
                    }
                    if ( map.containsKey("max") ) {
                        options.maxDocuments(Long.parseLong( String.valueOf( map.get("max"))));
                    }
                    mongoDatabase.createCollection( args[0].asString(), options);
                }
            }
            return null;
        }
    }
    private class RunCommandProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 ) {
                return mongoDatabase.runCommand( GraalConvertor.toBson( args[0].as(Map.class)) );
            }
            return null;
        }
    }
    private class DropProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 0 ) {
                mongoDatabase.drop();
            }
            return null;
        }
    }
    private class ListCollectionNamesProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 0 ) {
                return mongoDatabase.listCollectionNames();
            }
            return null;
        }
    }
    private class ListCollectionsProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 0 ) {
                return mongoDatabase.listCollections();
            }
            return null;
        }
    }
    private class GetNameProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 0 ) {
                return mongoDatabase.getName();
            }
            return null;
        }
    }

    private class GetViewSourceProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 && args[0].isString()) {
                for (Document doc : mongoDatabase.listCollections()) {
                    if ( args[0].asString().equals( doc.get("name")) && "view".equals(doc.get("type"))) {
                        Document options = (Document)doc.get("options");
                        final StringBuilder sb = new StringBuilder();
                        sb.append( mongoDatabase.getName()).append(".createView(\n\t\"");
                        sb.append(doc.get("name")).append("\",\n\t\"").
                                append(options.get("viewOn")).
                                append("\",\n\t[\n\t\t");
                        boolean addComma = false;
                        for ( Object d : (List)options.get("pipeline")) {
                            if ( addComma) sb.append(",\n\t\t");
                            if ( d instanceof Document){
                                sb.append( ((Document)d).toJson() );
                            }else {
                                sb.append( d);
                            }
                            addComma = true;
                        }
                        sb.append("\n\t]\n)");
                        Document ret = new Document();
                        ret.put("source", sb.toString());
                        return ret;
                    }
                }
            }
            return null;
        }
    }

    public String getName(){
        return mongoDatabase.getName();
    }

    public MongoIterable<String> listCollectionNames() {
        return mongoDatabase.listCollectionNames();
    }

    public ListCollectionsIterable<Document> listCollections() {
        return mongoDatabase.listCollections();
    }

    public void createCollection( String s ) {
        mongoDatabase.createCollection( s );
    }

    public void discoverReferences(MetaCollection master ){
        if ( !master.referencesDiscovered){
            try {
                master.referencesDiscovered = true;
                final List<MetaField> unsolvedFields = new ArrayList<>();
                final List<MetaField> solvedFields = new ArrayList<>();
                master.collectFieldsWithObjectId(unsolvedFields);
                if ( !unsolvedFields.isEmpty() ){
                    for ( MetaCollection _metaCollection : metaDatabase.getCollections() ){
                        final WrappedMongoCollection mongoCollection = getCollection( _metaCollection.name );
                        if ( mongoCollection != null ){
                            for ( MetaField metaField : unsolvedFields ){
                                for ( ObjectId objectId : metaField.objectIds){
                                    final Document query = new Document(); //new BasicDBObject();
                                    query.put("_id", objectId);
                                    if ( !solvedFields.contains( metaField ) && mongoCollection.find(query).iterator().hasNext()) {
                                        solvedFields.add( metaField );
                                        metaField.createReferenceTo(_metaCollection);
                                        LOGGER.log(Level.INFO, "Found virtual relation  " + metaField.parentObject.name + " ( " + metaField.name + " ) ref " + _metaCollection.name);
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

