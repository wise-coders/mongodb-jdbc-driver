package com.wisecoders.dbschema.mongodb;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */

public class ReverseEngineer extends AbstractTestCase {
    private Connection con;

    private static final String urlWithAuth = "jdbc:mongodb://admin:fictivpwd@localhost:27017/local?authSource=local&connectTimeoutMS=1000";
    private static final String urlWithoutAuth = "jdbc:mongodb://localhost";


    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("com.wisecoders.dbschema.mongodb.JdbcDriver");
        con = DriverManager.getConnection( urlWithoutAuth, null, null);
        Statement stmt=con.createStatement();
        stmt.execute("local.words.drop();");
        stmt.execute("local.words.insertOne({word: 'sample', qty:2, prop: [{ category:'verb'},{ base:'latin'}]});");
        stmt.execute("local.words.createIndex( { word: 1, 'prop.category':1 }, {name:'sampleIndex'} );");
        stmt.execute("use tournament;");
        stmt.execute("tournament.students.drop();");
        stmt.execute("tournament.createCollection('students', {\n" +
                "   validator: {\n" +
                "      $jsonSchema: {\n" +
                "         bsonType: 'object',\n" +
                "         required: [ 'name', 'year', 'major', 'address' ],\n" +
                "         properties: {\n" +
                "            name: {\n" +
                "               bsonType: 'string',\n" +
                "               description: 'must be a string and is required'\n" +
                "            },\n" +
                "            year: {\n" +
                "               bsonType: 'int',\n" +
                "               minimum: 2017,\n" +
                "               maximum: 3017,\n" +
                "               description: 'must be an integer in [ 2017, 3017 ] and is required'\n" +
                "            },\n" +
                "            major: {\n" +
                "               enum: [ 'Math', 'English', 'Computer Science', 'History', null ],\n" +
                "               description: 'can only be one of the enum values and is required'\n" +
                "            },\n" +
                "            gpa: {\n" +
                "               bsonType: [ 'double' ],\n" +
                "               description: 'must be a double if the field exists'\n" +
                "            },\n" +
                "            address: {\n" +
                "               bsonType: 'object',\n" +
                "               required: [ 'city' ],\n" +
                "               properties: {\n" +
                "                  street: {\n" +
                "                     bsonType: 'string',\n" +
                "                     description: 'must be a string if the field exists'\n" +
                "                  },\n" +
                "                  city: {\n" +
                "                     bsonType: 'string',\n" +
                "                     description: 'must be a string and is required'\n" +
                "                  }\n" +
                "               }\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "})");
        stmt.execute("tournament.contacts.drop();");
        stmt.execute("tournament.createCollection( 'contacts',\n" +
                "   { validator: { $or:\n" +
                "      [\n" +
                "         { phone: { $type: 'string' } },\n" +
                "         { email: { $regex: \"@mongodb\\.com$\" } },\n" +
                "         { status: { $in: [ 'Unknown', 'Incomplete' ] } }\n" +
                "      ]\n" +
                "   }\n" +
                "} )" );

                stmt.close();
    }

    @Test
    public void testIndex() throws SQLException {
        ResultSet rs = con.getMetaData().getTables("local", "local", null, null );
        while ( rs.next() ){
            String colName = rs.getString(3);
            printResultSet( con.getMetaData().getColumns("local", "local", colName, null));
            printResultSet( con.getMetaData().getColumns("tournament", "local", colName, null));
            printResultSet( con.getMetaData().getIndexInfo("local", "local", colName, false, false ));
        }
    }
}
