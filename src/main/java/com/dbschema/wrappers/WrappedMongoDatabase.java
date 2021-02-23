package com.dbschema.wrappers;

import com.dbschema.Util;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationOptions;
import org.bson.Document;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Wrapper class around MongoDatabase with direct access to collections as member variables.
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedMongoDatabase implements ProxyObject {


    private final MongoDatabase mongoDatabase;

    WrappedMongoDatabase(MongoDatabase mongoDatabase ){
        this.mongoDatabase = mongoDatabase;
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
            default: return new WrappedMongoCollection(mongoDatabase.getCollection( key ));
        }
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
                mongoDatabase.createView(args[0].asString(), args[1].asString(), Util.toBsonList( args[2].as(List.class)) );
            }
            return null;
        }
    }
    private class GetCollectionProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 && args[0].isString() ) {
                return getCollection(args[0].asString());
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
                    mongoDatabase.createCollection(args[0].asString(), args[1].asHostObject());
                }
                final Map map = args[1].as(Map.class);
                if ( map != null ){
                    CreateCollectionOptions options = new CreateCollectionOptions();
                    if ( map.containsKey("validator") ) {
                        options.validationOptions(new ValidationOptions().validator(Util.toBson(map.get("validator"))));
                    }
                    if ( map.containsKey("storageEngine") ) {
                        options.storageEngineOptions(Util.toBson(map.get("storageEngine")));
                    }
                    if ( map.containsKey("capped") ) {
                        options.capped(Boolean.parseBoolean( String.valueOf( map.get("capped"))));
                    }
                    if ( map.containsKey("max") ) {
                        options.maxDocuments(Long.parseLong( String.valueOf( map.get("max"))));
                    }
                    mongoDatabase.createCollection(args[0].asString(), options);
                }
            }
            return null;
        }
    }
    private class RunCommandProxyExecutable implements ProxyExecutable{
        @Override
        public Object execute(Value... args) {
            if( args.length == 1 && args[0].hasArrayElements() ) {
                mongoDatabase.runCommand( Util.toBson( args[2].as(List.class)) );
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

    public WrappedMongoCollection<Document> getCollection(String s) {
        return new WrappedMongoCollection<>(mongoDatabase.getCollection(s ));
    }
}

