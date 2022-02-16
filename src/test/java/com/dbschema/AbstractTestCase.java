package com.dbschema;

import com.google.gson.Gson;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */

class AbstractTestCase {

    void printResultSet(ResultSet rs ) throws SQLException  {
        while ( rs != null && rs.next()){
            Object obj = rs.getObject(1 );
            if ( obj != null ){
                try {
                    System.out.println(new Gson().toJson(obj));
                } catch ( Throwable ex ){
                    System.out.println(obj);
                }
            }
        }
    }
}
