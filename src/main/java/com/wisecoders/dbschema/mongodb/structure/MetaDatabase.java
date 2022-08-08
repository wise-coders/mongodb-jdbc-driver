package com.wisecoders.dbschema.mongodb.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class MetaDatabase {

    public final String name;
    private final Map<String, MetaCollection> metaCollections = new HashMap<>();

    public MetaDatabase( String name ){
        this.name =  name;
    }

    public MetaCollection createMetaCollection(String name ){
        MetaCollection metaCollection = new MetaCollection(this, name);
        metaCollections.put( name, metaCollection);
        return metaCollection;
    }

    public MetaCollection getMetaCollection(String name ){
        return metaCollections.get( name );
    }

    public Collection<MetaCollection> getMetaCollections(){
        return metaCollections.values();
    }

    public void dropMetaCollection(String name ){
        metaCollections.remove( name );
    }
}
