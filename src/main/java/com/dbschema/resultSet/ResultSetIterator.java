package com.dbschema.resultSet;

import com.dbschema.MongoResultSetMetaData;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.util.*;

/**
 * The Iterator can be instantiated with expand=true. In this case we read ahead MAX_READ_AHEAD documents and we fill the metaColumnNames and metaColumnTypes.
 * We need to do this as the result document can be first time for example {firstname='Luise'} and second record {firstname='John',lastname='Carry'}.
 * I mean with this that some keys may miss in some records, so expanding should look ahead for all possible keys.
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class ResultSetIterator implements ResultSet {

    private final Iterator iterator;
    private Object current;
    private static final int MAX_READ_AHEAD = 300;
    private List<Object> readAhead = new ArrayList<>();
    private boolean expandResultSet = false;
    private final List<String> metaColumnsNames = new ArrayList<>();
    private final List<Integer> metaJavaTypes = new ArrayList<>();
    private final List<Integer> metaDisplaySize = new ArrayList<>();

    ResultSetIterator(){
        this.iterator = null;
        initMetaData();
    }

    public ResultSetIterator(Iterable iterable, boolean expandResultSet){
        iterator = (iterable != null ? iterable.iterator() : null);
        this.expandResultSet = expandResultSet && iterator != null;
        initMetaData();
    }

    public ResultSetIterator(Iterator iterator, boolean expandResultSet ){
        this.iterator = iterator;
        this.expandResultSet = expandResultSet && iterator != null;
        initMetaData();
    }

    private void initMetaData(){
        if ( expandResultSet ){
            while ( iterator.hasNext() && readAhead.size() < MAX_READ_AHEAD ){
                Object obj = iterator.next();
                fillMetaData( obj );
                readAhead.add( obj );
            }
        } else {
            addMetaColumn( "document", Types.JAVA_OBJECT, 300 );
        }
    }

    private void addMetaColumn( String columnName, int javaType, int displaySize ){
        metaColumnsNames.add( columnName );
        metaJavaTypes.add( javaType );
        metaDisplaySize.add( displaySize );
    }

    private void fillMetaData(Object obj ){
        if ( obj instanceof Map ){
            Map<String,Object> skipOneMap = (Map<String,Object>)obj;
            for ( String key : skipOneMap.keySet() ){
                if ( !metaColumnsNames.contains( key )) {
                    addMetaColumn( key, getJavaTypeForObject(skipOneMap.get(key)), 300 );
                }
            }
        }
    }

    private int getJavaTypeForObject( Object obj ){
        if ( obj instanceof String ) return Types.VARCHAR;
        if ( obj instanceof Integer ) return Types.INTEGER;
        if ( obj instanceof Long ) return Types.BIGINT;
        if ( obj instanceof Double ) return Types.DOUBLE;
        if ( obj instanceof Float ) return Types.FLOAT;
        if ( obj instanceof Boolean ) return Types.BOOLEAN;
        if ( obj instanceof Date ) return Types.DATE;
        if ( obj instanceof Timestamp ) return Types.TIMESTAMP;
        if ( obj instanceof Time ) return Types.TIME;
        return Types.OTHER;
    }


    @Override
    public Object getObject(int columnIndex) throws SQLException {
        if ( expandResultSet && current instanceof Map ){
            return ((Map)current).get( metaColumnsNames.get(columnIndex-1));
        }
        return current;
    }

    @Override
    public boolean next() throws SQLException {
        current = null;
        if ( readAhead.size() > 0 ){
            current = readAhead.get(0);
            readAhead.remove(0);
            return true;
        }
        if ( iterator != null ) {
            if ( iterator.hasNext() ) {
                current = iterator.next();
                fillMetaData( current );
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        final String[] columnNames = new String[metaColumnsNames.size()];
        final int[] columnTypes = new int[metaColumnsNames.size()];
        final int[] displaySize = new int[metaColumnsNames.size()];
        int i = 0;
        for ( String key : metaColumnsNames ){
            columnNames[i] = key;
            columnTypes[i] = metaJavaTypes.get(i);
            displaySize[i] = metaDisplaySize.get(i);
            i++;
        }
        return new MongoResultSetMetaData("Result", columnNames, columnTypes, displaySize );
    }

    @Override
    public boolean wasNull()  {
        return false;
    }

    @Override
    public String getString(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            return String.valueOf( ((Map)current).get( metaColumnsNames.get(columnIndex-1)));
        }
        return null;
    }

    @Override
    public boolean getBoolean(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Boolean ) return (Boolean) obj;
            return obj != null ? Boolean.valueOf( String.valueOf(obj) ) : false;

        }
        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Number ) return ((Number) obj).shortValue();
            return obj != null ? Short.valueOf( String.valueOf(obj) ) : -1;

        }
        return 0;
    }

    @Override
    public int getInt(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Number ) return ((Number) obj).intValue();
            return obj != null ? Integer.valueOf( String.valueOf(obj) ) : -1;
        }
        return 0;
    }

    @Override
    public long getLong(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Number ) return ((Number) obj).longValue();
            return obj != null ? Long.valueOf( String.valueOf(obj) ) : -1;
        }
        return 0;
    }

    @Override
    public float getFloat(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Number ) return ((Number) obj).floatValue();
            return obj != null ? Float.valueOf( String.valueOf(obj) ) : -1f;
        }
        return 0;
    }

    @Override
    public double getDouble(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof Double ) return (Double) obj;
            return obj != null ? Double.valueOf( obj.toString() ) : -1d;
        }
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex-1));
            if ( obj instanceof BigDecimal ) return (BigDecimal) obj;
            return  null;
        }
        return null;
    }

    @Override
    public byte[] getBytes(int columnIndex)  {
        return new byte[0];
    }

    @Override
    public Date getDate(int columnIndex) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( metaColumnsNames.get(columnIndex));
            if ( obj instanceof Date ) return (Date)obj;
            if ( obj instanceof java.util.Date ) return new Date( ((java.util.Date)obj).getTime() );
            return obj != null ? Date.valueOf( String.valueOf( obj ) ) : null;
        }
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        if ( expandResultSet && current instanceof Map ){
            return String.valueOf( ((Map)current).get( columnLabel));
        }
        return null;
    }

    @Override
    public boolean getBoolean(String columnLabel) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return false;
            if ( obj instanceof Boolean ) return (Boolean)obj;
            return Boolean.valueOf( obj.toString() );
        }
        return false;
    }

    @Override
    public byte getByte(String columnLabel) {
        return 0;
    }

    @Override
    public short getShort(String columnLabel) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return -1;
            if ( obj instanceof Number ) return ((Number)obj).shortValue();
            return Short.valueOf( obj.toString() );
        }
        return -1;
    }

    @Override
    public int getInt(String columnLabel) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return -1;
            if ( obj instanceof Number ) return ((Number)obj).intValue();
            return Integer.valueOf( obj.toString() );
        }
        return -1;
    }

    @Override
    public long getLong(String columnLabel) {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return -1;
            if ( obj instanceof Number ) return ((Number)obj).longValue();
            return Long.valueOf( obj.toString() );
        }
        return -1;
    }

    @Override
    public float getFloat(String columnLabel)  {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return -1;
            if ( obj instanceof Number ) return ((Number)obj).floatValue();
            return Float.valueOf( obj.toString() );
        }
        return -1f;
    }

    @Override
    public double getDouble(String columnLabel) {
        if ( expandResultSet && expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return -1;
            if ( obj instanceof Number ) return ((Number)obj).doubleValue();
            return Double.valueOf( obj.toString() );
        }
        return -1d;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale)  {
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) {
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel)  {
        if ( expandResultSet && current instanceof Map ){
            Object obj = ((Map)current).get( columnLabel);
            if ( obj == null ) return null;
            if ( obj instanceof Date ) return (Date)obj;
            if ( obj instanceof java.util.Date ) return new Date( ((java.util.Date)obj).getTime() );
            return Date.valueOf( obj.toString() );
        }
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel) {
        if ( expandResultSet && current instanceof Map ){
            return ((Map)current).get( columnLabel);
        }
        return current;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
