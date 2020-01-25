package com.dbschema.schema;


import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaIndex {

    private final MetaJson metaMap;
    public final String name;
    public final List<MetaField> metaFields = new ArrayList<MetaField>();
    public final boolean pk, unique;

    MetaIndex(MetaJson metaMap, String name, boolean pk, boolean unique){
        this.metaMap = metaMap;
        this.name = name;
        this.pk = pk;
        this.unique = unique;
    }

    void addColumn( MetaField metaField ){
        if ( metaField != null ){
            metaFields.add( metaField );
        }
    }
}
