package com.wisecoders.dbschema.mongodb.structure;


import java.util.ArrayList;
import java.util.List;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
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
