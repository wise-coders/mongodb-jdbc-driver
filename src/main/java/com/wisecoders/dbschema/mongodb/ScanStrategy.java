package com.wisecoders.dbschema.mongodb;


/**
 * How deep the driver should look into collections in order to deduce the collection structure ( fields, data types ).
 *
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public enum ScanStrategy {


    fast(50), medium(200 ), full( Long.MAX_VALUE );

    public final long SCAN_COUNT;

    ScanStrategy( long scanFistLast ){
        this.SCAN_COUNT = scanFistLast;
    }
}
