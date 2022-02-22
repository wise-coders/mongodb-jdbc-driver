package com.wisecoders.dbschema.mongodb;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */

public class ExpandedResultSet extends AbstractTestCase{

    private Connection con;

    private static final String urlWithoutAuth = "jdbc:mongodb://localhost?expand=true";


    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("com.wisecoders.dbschema.mongodb.JdbcDriver");
        con = DriverManager.getConnection( urlWithoutAuth, null, null);
        Statement stmt=con.createStatement();
        stmt.execute("local.words.drop();");
        stmt.execute("local.words.insertOne({word: 'sample1'});");
        stmt.execute("local.words.insertOne({word: 'sample2', qty:5});");
        stmt.close();
    }

    @Test
    public void testFind() throws Exception {
        Statement stmt=con.createStatement();
        ResultSet rs = stmt.executeQuery("local.words.find()");
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++ ){
            System.out.printf( "%30s", metaData.getColumnName( i)+ "(" + metaData.getColumnType(i) + ")");
        }
        System.out.println();
        while ( rs.next() ){
            for (int i = 1; i <= metaData.getColumnCount(); i++ ) {
                System.out.printf("%30s", rs.getString(i));
            }
            System.out.println();
        }
        stmt.close();
    }


}
