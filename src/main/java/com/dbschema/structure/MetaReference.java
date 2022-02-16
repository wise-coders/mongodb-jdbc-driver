package com.dbschema.structure;


/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class MetaReference {

    public final MetaField fromField;
    public final MetaCollection pkCollection;

    public MetaReference( MetaField fromField, MetaCollection pkCollection ){
        this.fromField = fromField;
        this.pkCollection = pkCollection;
    }

}
