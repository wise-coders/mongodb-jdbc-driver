package com.dbschema;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;



/**
 * We tested using a MongoDb docker container running local.
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class ResourceScripts extends AbstractTestCase {

    Connection con;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("com.dbschema.MongoJdbcDriver");
        con = DriverManager.getConnection("jdbc:mongodb://localhost", "root", "root");
    }

    @Test
    public void script() throws IOException, SQLException {
        executeFile("script.txt");
    }
    @Test
    public void aggregate() throws IOException, SQLException {
        executeFile("testAggregate.txt");
    }
    @Test
    public void inventory() throws IOException, SQLException {
        executeFile("inventory.txt");
    }
    @Test
    public void mapReduce() throws IOException, SQLException {
        executeFile("mapReduce.txt");
    }
    @Test
    public void masterSlave() throws IOException, SQLException {
        executeFile("masterSlave.txt");
    }

    private void executeFile( String fileName ) throws IOException, SQLException {
        InputStream is = getClass().getResourceAsStream( fileName );
        BufferedReader br = new BufferedReader(new InputStreamReader(is ));

        String line;
        final StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
            if ( !line.isBlank()) {
                sb.append(line).append("\n");
            }
            if ( line.trim().endsWith(";")){
                executeQuery( sb.toString() );
                sb.delete(0, sb.length());
            }
        }
        if ( sb.length() > 0 ){
            executeQuery( sb.toString() );
        }
    }

    private void executeQuery( String query ) throws SQLException {
        System.out.println("> " + query );
        final Statement stmt = con.createStatement();
        try {
            printResultSet( stmt.executeQuery(query ) );
        } catch ( Throwable ex ){
            System.out.println( ex.getLocalizedMessage() );
            throw ex;
        }
        stmt.close();
    }
}
