package com.dbschema;


/**
 * How deep the driver should look into collections in order to deduce the collection structure ( fields, data types ).
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public enum ScanStrategy {

    fast, medium, full

}
