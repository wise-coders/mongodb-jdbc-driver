package com.wisecoders.dbschema.mongodb.wrappers;

import com.google.gson.GsonBuilder;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import com.wisecoders.dbschema.mongodb.GraalConvertor;
import com.wisecoders.dbschema.mongodb.ScanStrategy;
import com.wisecoders.dbschema.mongodb.Util;
import com.wisecoders.dbschema.mongodb.structure.MetaCollection;
import com.wisecoders.dbschema.mongodb.structure.MetaDatabase;
import org.bson.Document;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static com.wisecoders.dbschema.mongodb.JdbcDriver.LOGGER;


/**
 * Wrapper class around MongoDatabase with direct access to collections as member variables.
 *
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with  <a href="https://dbschema.com">DbSchema Database Designer</a>
 * Free to use by everyone, code modifications allowed only to the  <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public repository</a>
 */
public class WrappedMongoDatabase implements ProxyObject {

    private final MongoDatabase mongoDatabase;
    private final ScanStrategy scanStrategy;
    public final MetaDatabase metaDatabase;
    private final boolean sortFields;

    WrappedMongoDatabase( MongoDatabase mongoDatabase, ScanStrategy scanStrategy, boolean sortFields ){
        this.mongoDatabase = mongoDatabase;
        this.scanStrategy = scanStrategy;
        this.metaDatabase = new MetaDatabase(mongoDatabase.getName());
        this.sortFields = sortFields;
        try {
            if ( !"config".equals(mongoDatabase.getName()) && !"admin".equals(mongoDatabase.getName()) && !"local".equals(mongoDatabase.getName())) {
                for (Document info : mongoDatabase.listCollections()) {
                    Document definition = (Document) Util.getByPath(info, "options.validator.$jsonSchema");
                    if (definition != null) {
                        final String name = info.getString("name");
                        final MetaCollection metaCollection = metaDatabase.createMetaCollection(name, false);
                        try {
                            metaCollection.visitValidatorNode(null, true, definition, sortFields );
                        } catch (Throwable ex) {
                            LOGGER.log(Level.SEVERE, "Error parsing validation rule for " + name + "\n\n" + new GsonBuilder().setPrettyPrinting().create().toJson(definition) + "\n", ex);
                            metaDatabase.dropMetaCollection(name);
                        }
                        metaCollection.scanIndexes(getCollection(name));
                    }
                }
            }
        } catch ( Throwable ex ){
            LOGGER.log(Level.SEVERE, "Error listing database '" + mongoDatabase.getName() + "' collections\n\n", ex);
        }
    }

    public MetaCollection getMetaCollectionIfAlreadyLoaded( String collectionName) {
        if (collectionName == null || collectionName.isEmpty()) return null;

        return metaDatabase.getMetaCollection(collectionName);
    }


    public MetaCollection getMetaCollection( String collectionName){
        if ( collectionName == null || collectionName.isEmpty()) return null;

        final MetaCollection metaCollection = metaDatabase.getMetaCollection(collectionName);
        if (metaCollection == null) {
            try {
                return metaDatabase.createMetaCollection( collectionName, true ).scanDocumentsAndIndexes( getCollection(collectionName), scanStrategy, sortFields );
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


}

