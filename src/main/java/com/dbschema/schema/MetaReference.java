package com.dbschema.schema;


/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaReference {

    public final MetaField fromField;
    public final MetaCollection pkCollection;

    public MetaReference( MetaField fromField, MetaCollection pkCollection ){
        this.fromField = fromField;
        this.pkCollection = pkCollection;
    }

}
