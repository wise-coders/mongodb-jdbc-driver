package com.dbschema;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

public class ExpandedResultSet extends AbstractTestCase{

    private Connection con;

    private static final String urlWithoutAuth = "jdbc:mongodb://localhost?expand=true";


    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("com.dbschema.MongoJdbcDriver");
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
