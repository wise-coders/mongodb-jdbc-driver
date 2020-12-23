package com.dbschema;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;

public class ReverseEngineer extends AbstractTestCase {
    private Connection con;

    private static final String urlWithAuth = "jdbc:mongodb://admin:fictivpwd@localhost:27017/local?authSource=local&connectTimeoutMS=1000";
    private static final String urlWithoutAuth = "jdbc:mongodb://localhost";


    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("com.dbschema.MongoJdbcDriver");
        con = DriverManager.getConnection( urlWithoutAuth, null, null);
        Statement stmt=con.createStatement();
        stmt.execute("local.words.drop();");
        stmt.execute("local.words.insertOne({word: 'sample', qty:2, prop: [{ category:'verb'},{ base:'latin'}]});");
        stmt.execute("local.words.createIndex( { word: 1, 'prop.category':1 }, {name:'sampleIndex'} );");
        stmt.close();
    }

    @Test
    public void testIndex() throws SQLException {
        ResultSet rs = con.getMetaData().getTables("local", "local", null, null );
        while ( rs.next() ){
            String colName = rs.getString(3);
            printResultSet( con.getMetaData().getColumns("local", "local", colName, null));
            printResultSet( con.getMetaData().getIndexInfo("local", "local", colName, false, false ));
        }
    }
}
