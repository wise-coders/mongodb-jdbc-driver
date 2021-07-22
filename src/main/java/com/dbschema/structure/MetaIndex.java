package com.dbschema.structure;


import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaIndex {

    private final MetaObject metaMap;
    public final String name;
    public final List<MetaField> metaFields = new ArrayList<MetaField>();
    public final boolean pk, unique;

    MetaIndex(MetaObject metaMap, String name, boolean pk, boolean unique){
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
