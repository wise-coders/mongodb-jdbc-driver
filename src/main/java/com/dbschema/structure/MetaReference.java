package com.dbschema.structure;


/**
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaReference {

    public final MetaField fromField;
    public final MetaCollection pkCollection;

    public MetaReference( MetaField fromField, MetaCollection pkCollection ){
        this.fromField = fromField;
        this.pkCollection = pkCollection;
    }

}
