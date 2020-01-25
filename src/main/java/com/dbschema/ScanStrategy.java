package com.dbschema;


/**
 * How deep the driver should look into collections in order to deduce the collection structure ( fields, data types ).
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public enum ScanStrategy {

    fast, medium, full

}
