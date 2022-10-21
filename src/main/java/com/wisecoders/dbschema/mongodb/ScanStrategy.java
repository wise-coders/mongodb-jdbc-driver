package com.wisecoders.dbschema.mongodb;


/**
 * How deep the driver should look into collections in order to deduce the collection structure ( fields, data types ).
 *
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with  <a href="https://dbschema.com">DbSchema Database Designer</a>
 * Free to use by everyone, code modifications allowed only to the  <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public repository</a>
 */
public enum ScanStrategy {


    fast(100), medium(300 ), full( Long.MAX_VALUE );

    public final long SCAN_COUNT;

    ScanStrategy( long scanFistLast ){
        this.SCAN_COUNT = scanFistLast;
    }
}
