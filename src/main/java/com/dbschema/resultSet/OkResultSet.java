package com.dbschema.resultSet;

import com.dbschema.MongoResultSetMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class OkResultSet extends ResultSetIterator {

    public OkResultSet(){
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return "Ok";
    }

    @Override
    public boolean next() throws SQLException {
        return false;
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new MongoResultSetMetaData("Result", new String[]{"object"},  new int[]{Types.JAVA_OBJECT},new int[]{300});
    }

}
