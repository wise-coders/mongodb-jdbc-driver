package com.dbschema.wrappers;

import com.mongodb.BasicDBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import jdk.nashorn.api.scripting.AbstractJSObject;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;


/**
 * Wrapper class around MongoDatabase with direct access to collections as member variables.
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedMongoDatabase extends AbstractJSObject {

    private final MongoDatabase mongoDatabase;

    WrappedMongoDatabase(MongoDatabase mongoDatabase ){
        this.mongoDatabase = mongoDatabase;
    }

    public String getName() {
        return mongoDatabase.getName();
    }

    public CodecRegistry getCodecRegistry() {
        return mongoDatabase.getCodecRegistry();
    }

    public ReadPreference getReadPreference() {
        return mongoDatabase.getReadPreference();
    }

    public WriteConcern getWriteConcern() {
        return mongoDatabase.getWriteConcern();
    }

    public WrappedMongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
        mongoDatabase.withCodecRegistry(codecRegistry);
        return this;
    }

    public WrappedMongoDatabase withReadPreference(ReadPreference readPreference) {
        mongoDatabase.withReadPreference(readPreference);
        return this;
    }

    public WrappedMongoDatabase withWriteConcern(WriteConcern writeConcern) {
        mongoDatabase.withWriteConcern(writeConcern );
        return this;
    }

    public WrappedMongoCollection<Document> getCollection(String s) {
        return new WrappedMongoCollection<>(mongoDatabase.getCollection(s ));
    }

    public <TDocument> WrappedMongoCollection<TDocument> getCollection(String s, Class<TDocument> tDocumentClass) {
        return new WrappedMongoCollection<TDocument>( mongoDatabase.getCollection( s, tDocumentClass ) );
    }

    public Document runCommand(String str) {
        return mongoDatabase.runCommand( BasicDBObject.parse(str) ) ;
    }

    public Document runCommand( Map map ){
        return mongoDatabase.runCommand(WrappedMongoCollection.toBson(map));
    }

    public Document runCommand(Bson bson, ReadPreference readPreference) {
        return mongoDatabase.runCommand( bson, readPreference );
    }

    public <TResult> TResult runCommand(Bson bson, Class<TResult> tResultClass) {
        return mongoDatabase.runCommand(bson, tResultClass);
    }

    public <TResult> TResult runCommand(Bson bson, ReadPreference readPreference, Class<TResult> tResultClass) {
        return mongoDatabase.runCommand( bson, readPreference,  tResultClass);
    }



    public void drop() {
        mongoDatabase.drop();
    }

    public MongoIterable<String> listCollectionNames() {
        return mongoDatabase.listCollectionNames();
    }

    public ListCollectionsIterable<Document> listCollections() {
        return mongoDatabase.listCollections();
    }

    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> tResultClass) {
        return mongoDatabase.listCollections(tResultClass);
    }

    public void createCollection( String s ) {
        mongoDatabase.createCollection( s );
    }

    public void createCollection(String s, CreateCollectionOptions createCollectionOptions) {
        mongoDatabase.createCollection(s, createCollectionOptions);
    }

    /*
     *******  RHINO
     * We overwrite this methods in order to support direct access to collections.
     * Calling db.myCollection will return myCollection.
     * @param name The name of the member
     * @param start Scriptable
     * @return true if exists
     */
    /*
    @Override
    public boolean has(String name, Scriptable start) {
        if ( getCollection(name ) != null ) return true;
        return super.has(name, start);
    }

    @Override
    public Object get(String name, Scriptable start) {
        if ( getCollection( name ) != null ){
            return getCollection(name);
        }
        return super.get(name, start);
    }
*/

    // **** NASHORN
    // https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions
    // http://sites.psu.edu/robertbcolton/2015/07/31/java-8-nashorn-script-engine/

    @Override
    public boolean hasMember(String name) {
        return "getCollection".equals( name ) ||
                "createCollection".equals(name)||
                "createView".equals(name)||
                "getReadConcern".equals(name)||
                "listCollections".equals(name)||
                "listCollectionNames".equals(name)||
                "drop".equals(name)||
                "runCommand".equals(name);
    }

    @Override
    public Object getMember(final String name) {
        if ( hasMember( name ) ){
                return new AbstractJSObject() {
                    @Override
                    public Object call(Object thiz, Object... args) {
                        switch( name ){
                            case "getCollection":
                                if ( args.length == 1 && args[0] instanceof String ){
                                    return getCollection( (String)args[0]);
                                }
                                break;
                            case "createCollection":
                                if ( args.length == 1 && args[0] instanceof String ){
                                    createCollection( (String)args[0]);
                                } else if ( args.length == 2 && args[0] instanceof String && args[1] instanceof CreateCollectionOptions ){
                                    createCollection( (String)args[0], (CreateCollectionOptions)args[1]);
                                }
                                break;
                            case "createView":
                                if ( args.length == 3 && args[0] instanceof String && args[1] instanceof String ){
                                    mongoDatabase.createView( (String)args[0], (String)args[1], (List<? extends Bson>) args[2]);
                                }
                                break;
                            case "runCommand":
                                if ( args.length == 1 && args[0] instanceof String ){
                                    runCommand( (String)args[0] );
                                } else if ( args.length == 1 && args[0] instanceof Map ){
                                    runCommand( (Map)args[0] );
                                } else if ( args.length == 2 && args[0] instanceof Bson && args[1] instanceof Class ){
                                    runCommand( (Bson)args[0], (Class)args[0] );
                                }
                                break;
                            case "drop":
                                drop();
                                break;
                            case "listCollectionNames":
                                return listCollectionNames();
                            case "listCollections":
                                return listCollections();
                        }
                        return ( args.length == 1 ) ? getCollection(String.valueOf(args[0])) : null;
                    }
                    @Override
                    public boolean isFunction() {
                        return true;
                    }
                };
        }
        return getCollection( name );
    }


    @Override
    public String toString() {
        return getName();
    }



}
