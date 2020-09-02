package com.dbschema.schema;

import com.dbschema.ScanStrategy;
import com.dbschema.wrappers.WrappedMongoCollection;

import java.util.Collection;
import java.util.HashMap;

public class MetaDatabase {

    public String name;
    private final HashMap<String, MetaCollection> metaCollections = new HashMap<>();


    public MetaDatabase(String name){
        this.name = name;
    }

    public Collection<MetaCollection> getMetaCollections(){
        return metaCollections.values();
    }


    public void clear() {
        metaCollections.clear();
    }

    public MetaCollection getCollection ( String name ){
        return metaCollections.get( name );
    }

    public MetaCollection createCollection(WrappedMongoCollection mongoCollection, String collectionName, ScanStrategy scanStrategy ){
        MetaCollection collection = new MetaCollection( mongoCollection, this, collectionName, scanStrategy );
        metaCollections.put( collectionName, collection);
        return collection;
    }

    @Override
    public String toString() {
        return name;
    }
}
