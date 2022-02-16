package com.dbschema.structure;

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
    private final Map<String, MetaCollection> collections = new HashMap<>();

    public MetaDatabase( String name ){
        this.name =  name;
    }

    public MetaCollection createCollection( String name ){
        MetaCollection metaCollection = new MetaCollection(this, name);
        collections.put( name, metaCollection);
        return metaCollection;
    }

    public MetaCollection getCollection( String name ){
        return collections.get( name );
    }

    public Collection<MetaCollection> getCollections(){
        return collections.values();
    }

    public void dropCollection( String name ){
        collections.remove( name );
    }
}
