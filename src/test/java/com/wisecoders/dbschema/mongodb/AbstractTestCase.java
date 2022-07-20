package com.wisecoders.dbschema.mongodb;

import com.google.gson.Gson;
import com.wisecoders.dbschema.mongodb.structure.MetaObject;

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
            if ( rs.getMetaData().getColumnCount() == 1 && ( rs.getMetaData().getColumnType(1) == MetaObject.TYPE_ARRAY || rs.getMetaData().getColumnType(1) == MetaObject.TYPE_OBJECT ) ) {
                Object obj = rs.getObject(1);
                if (obj != null) {
                    try {
                        System.out.println(new Gson().toJson(obj));
                    } catch (Throwable ex) {
                        System.out.println(obj);
                    }
                }
            } else {
                for ( int c = 1; c <= rs.getMetaData().getColumnCount(); c++ ) {
                    System.out.print( rs.getMetaData().getColumnName(c) + " = "  + rs.getObject(c) + " ");
                }
                System.out.println("");
            }
        }
    }
}
