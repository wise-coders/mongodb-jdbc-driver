package com.wisecoders.dbschema.mongodb.structure;


/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with  <a href="https://dbschema.com">DbSchema Database Designer</a>
 * Free to use by everyone, code modifications allowed only to the  <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public repository</a>
 */
public class MetaReference {

    public final MetaField fromField;
    public final MetaCollection pkCollection;

    public MetaReference( MetaField fromField, MetaCollection pkCollection ){
        this.fromField = fromField;
        this.pkCollection = pkCollection;
    }

}
