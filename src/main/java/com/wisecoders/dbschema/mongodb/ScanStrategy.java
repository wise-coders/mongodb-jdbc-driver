package com.wisecoders.dbschema.mongodb;


/**
 * How deep the driver should look into collections in order to deduce the collection structure ( fields, data types ).
 *
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */
public enum ScanStrategy {


    fast(100), medium(300 ), full( Long.MAX_VALUE );

    public final long SCAN_COUNT;

    ScanStrategy( long scanFistLast ){
        this.SCAN_COUNT = scanFistLast;
    }
}
