
package com.dbschema;

import com.dbschema.resultSet.ArrayResultSet;
import com.dbschema.resultSet.ObjectAsResultSet;
import com.dbschema.resultSet.OkResultSet;
import com.dbschema.resultSet.ResultSetIterator;
import com.dbschema.wrappers.WrappedMongoClient;
import com.dbschema.wrappers.WrappedMongoCollection;
import com.dbschema.wrappers.WrappedMongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Implementation of the prepared statements. Can execute any native MongoDb queries and a bunch of 'show..' commands
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MongoPreparedStatement implements PreparedStatement {

    private final MongoConnection connection;
    private ResultSet lastResultSet;
    private boolean isClosed = false;
    private int maxRows = -1;
    private final String query;

    MongoPreparedStatement(final MongoConnection connection) {
        this.connection = connection;
        this.query = null;
    }

    MongoPreparedStatement(final MongoConnection connection, String query) {
        this.connection = connection;
        this.query = query;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }


    private static final Pattern PATTERN_USE_DATABASE = Pattern.compile("USE\\s+(.*)", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_CREATE_DATABASE = Pattern.compile("CREATE\\s+DATABASE\\s*'(.*)'\\s*", Pattern.CASE_INSENSITIVE );

    private static final Pattern PATTERN_SHOW_DATABASES = Pattern.compile("SHOW\\s+DATABASES\\s*", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_SHOW_DBS = Pattern.compile("SHOW\\s+DBS\\s*", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_SHOW_COLLECTIONS = Pattern.compile("SHOW\\s+COLLECTIONS\\s*", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_SHOW_USERS = Pattern.compile("SHOW\\s+USERS\\s*", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_SHOW_RULES = Pattern.compile("SHOW\\s+RULES\\s*", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_SHOW_PROFILES = Pattern.compile("SHOW\\s+PROFILES\\s*", Pattern.CASE_INSENSITIVE );

    private static final String INITIALIZATION_SCRIPT = "var ObjectId = function( oid ) { return new org.bson.types.ObjectId( oid );}\n" +
            "" +
            "var ISODate = function( str ) { " +
            "var formats = [\"yyyy-MM-dd'T'HH:mm:ss'Z'\", \"yyyy-MM-dd'T'HH:mm.ss'Z'\", \"yyyy-MM-dd'T'HH:mm:ss\", \"yyyy-MM-dd' 'HH:mm:ss\",\"yyyy-MM-dd'T'HH:mm:ssXXX\"];\n" +
            "\n" +
            "for (i = 0; i < formats.length; i++)  {\n" +
            "    try {\n" +
            "        return new java.text.SimpleDateFormat( formats[i] ).parse(str);\n" +
            "    } catch (error) { }\n" +
            "}\n" +
            "throw new java.text.ParseException(\"Un-parsable ISO date: \" + str + \" Configured formats: \" + formats, 0);" +
            "return null;" +
            "};\n\n" +

            "var Date = function( str ) { " +
            "var formats = [\"yyyy-MM-dd\", \"dd-MM-yyyy\", \"dd.MM.yyyy\", \"d.MM.yyyy\", \"dd/MM/yyyy\", \"yyyy.MM.dd\", \"M/d/yyyy\" ];\n" +
            "\n" +
            "for (i = 0; i < formats.length; i++)  {\n" +
            "    try {\n" +
            "        return new java.text.SimpleDateFormat( formats[i] ).parse(str);\n" +
            "    } catch (error) { }\n" +
            "}\n" +
            "throw new java.text.ParseException(\"Un-parsable date: \" + str + \" Configured formats: \" + formats, 0);" +
            "return null;" +
            "}";

    @Override
    public ResultSet executeQuery(String query) throws SQLException	{
        checkClosed();
        System.out.println("Execute " + query );
        if (lastResultSet != null ) {
            lastResultSet.close();
        }
        if ( query == null ){
            throw new SQLException("Null statement.");
        }
        String plainQuery = query.trim();
        if ( plainQuery.endsWith(";")){
            plainQuery = plainQuery.substring(0, plainQuery.length()-1);
        }
        Matcher matcherSetDb = PATTERN_USE_DATABASE.matcher( plainQuery );
        if ( matcherSetDb.matches() ){
            String db = matcherSetDb.group(1).trim();
            if ( ( db.startsWith("\"") && db.endsWith("\"")) || ( db.startsWith("'") && db.endsWith("'"))){
                db = db.substring( 1, db.length()-1);
            }
            connection.setCatalog( db );
            return new OkResultSet();
        }
        Matcher matcherCreateDatabase = PATTERN_CREATE_DATABASE.matcher( plainQuery );
        if ( matcherCreateDatabase.matches() ){
            final String dbName = matcherCreateDatabase.group(1);
            connection.getDatabase(dbName);
            WrappedMongoClient.createdDatabases.add(dbName);
            return new OkResultSet();
        }
        if ( query.toLowerCase().startsWith("show ")){
            if ( PATTERN_SHOW_DATABASES.matcher( query ).matches() || PATTERN_SHOW_DBS.matcher( plainQuery ).matches() ){
                ArrayResultSet result = new ArrayResultSet();
                result.setColumnNames(new String[]{"DATABASE_NAME"});
                for ( String str : connection.getDatabaseNames() ){
                    result.addRow( new String[]{ str });
                }
                return lastResultSet = result;
            } else if ( PATTERN_SHOW_COLLECTIONS.matcher( plainQuery ).matches()){
                ArrayResultSet result = new ArrayResultSet();
                result.setColumnNames(new String[]{"COLLECTION_NAME"});
                for ( String str : connection.client.getCollectionNames(connection.getCatalog()) ){
                    result.addRow( new String[]{ str });
                }
                return lastResultSet = result;
            } else if ( PATTERN_SHOW_USERS.matcher( plainQuery ).matches()){
                query = "db.runCommand(\"{usersInfo:'" + connection.getCatalog() + "'}\")";
            } else if ( PATTERN_SHOW_PROFILES.matcher( plainQuery ).matches() || PATTERN_SHOW_RULES.matcher( plainQuery ).matches() ){
                throw new SQLException("Not yet implemented in this driver.");
            } else {
                throw new SQLException("Invalid command : " + plainQuery);
            }
        }
        try {
            Context context = Context.newBuilder("js").allowAllAccess(true).build();
            boolean dbIsSet = false;
            Value bindings = context.getBindings("js");
            for ( WrappedMongoDatabase db : connection.getDatabases() ){
                bindings.putMember(db.getName(), db );
                if ( connection.getCatalog() != null && connection.getCatalog().equals(db.getName())){
                    bindings.putMember("db", db);
                    dbIsSet = true;
                }
            }
            if ( !dbIsSet ){
                bindings.putMember("db", connection.getDatabase("admin"));
            }
            bindings.putMember("client", connection);
            context.eval("js", INITIALIZATION_SCRIPT);

            Value value = context.eval( "js", query );
            if ( value.isHostObject() ) {
                Object obj = value.asHostObject();
                if (obj instanceof Iterable) {
                    lastResultSet = new ResultSetIterator(((Iterable) obj).iterator(), connection.client.expandResultSet);
                } else if (obj instanceof Iterator) {
                    lastResultSet = new ResultSetIterator((Iterator) obj, connection.client.expandResultSet);
                } else if (obj instanceof WrappedMongoCollection) {
                    lastResultSet = new ResultSetIterator(((WrappedMongoCollection) obj).find(), connection.client.expandResultSet);
                } else if (obj != null) {
                    lastResultSet = new ObjectAsResultSet(obj);
                }
            }
            return lastResultSet;
        } catch ( Throwable ex ){
            ex.printStackTrace();
            throw new SQLException( ex.getMessage(), ex );
        }
    }

    public StringBuilder debug( Document doc, String prefix, StringBuilder out ){
        for ( String key : doc.keySet() ){
            Object value = doc.get( key );
            out.append(prefix ).append( key ).append( " = " ).append( getClassDetails( value ) ).append( " " ).append( value ).append( "\n");
            if ( value instanceof Document ){
                debug( (Document)value, prefix + "  ", out );
            }
        }
        return out;
    }

    private String getClassDetails( Object obj ){
        StringBuilder sb = new StringBuilder();
        if ( obj != null ){
            sb.append( "[ Class:").append( obj.getClass().getName() ).append( " implements ");
            for ( Class inf : obj.getClass().getInterfaces() ){
                sb.append( inf.getName());
            }
            sb.append( " ] ").append( obj );
        }
        return sb.toString();
    }


    @Override
    public boolean execute(final String query) throws SQLException {
        executeQuery( query );
        return lastResultSet != null;
    }

    private Document documentParam;

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        if ( x instanceof Document ){
            documentParam = (Document)x;
        } else if ( x instanceof Map ){
            documentParam = new Document( (Map)x);
        } else if (x == null) {
            throw new SQLException("Map object expected. You currently did setObject( NULL ) ");
        } else {
            throw new SQLException("Map object expected. You currently did setObject( " + x.getClass().getName() + " ) ");
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeUpdate(query);
    }

    private WrappedMongoDatabase getDatabase(String name){
        for ( WrappedMongoDatabase scan : connection.getDatabases() ){
            if ( scan.getName().equalsIgnoreCase( name )){
                return scan;
            }
        }
        if ( "db".equals( name ) && connection.getCatalog() != null ){
            for ( WrappedMongoDatabase scan : connection.getDatabases() ){
                if ( scan.getName().equalsIgnoreCase( connection.getCatalog() )){
                    return scan;
                }
            }
        }
        return null;
    }

    private static final Pattern PATTERN_UPDATE = Pattern.compile("UPDATE\\s+(.*)", Pattern.CASE_INSENSITIVE );
    private static final Pattern PATTERN_DELETE = Pattern.compile("DELETE\\s+FROM\\s+(.*)", Pattern.CASE_INSENSITIVE );
    private static final String ERROR_MESSAGE = "Allowed statements: update(<dbname>.<collectionName>) or delete(<dbname>.<collectionName>). Before calling this do setObject(0,<dbobject>).";

    @Override
    public int executeUpdate( String sql) throws SQLException	{
        if ( sql != null ) {
            if ( documentParam == null ){
                // IF HAS NO PARAMETERS, EXECUTE AS NORMAL SQL
                execute( sql );
                return 1;
            } else {
                sql = sql.trim();
                Matcher matcher = PATTERN_UPDATE.matcher( sql );
                final Object id = documentParam.get("_id");
                if ( matcher.matches() ){
                    WrappedMongoCollection collection = getCollectionMandatory(matcher.group(1), true);
                    if (id == null) {
                        collection.insertOne(documentParam);
                    } else {
                        collection.replaceOne( new Document("_id", id), documentParam, new ReplaceOptions().upsert(true));
                    }
                    return 1;
                }
                matcher = PATTERN_DELETE.matcher( sql );
                if ( matcher.matches() ){
                    WrappedMongoCollection collection = getCollectionMandatory(matcher.group(1), false);
                    collection.deleteOne((new Document().append("_id", id)) );
                    return 1;
                }
            }
        }
        throw new SQLException( ERROR_MESSAGE );
    }

    private static final Pattern PATTERN_DB_IDENTIFIER = Pattern.compile("client\\.getDatabase\\('(.*)'\\).(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
    private static final Pattern PATTERN_COLLECTION_IDENTIFIER = Pattern.compile("getCollection\\('(.*)'\\).(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
    private static final Pattern PATTERN_DOT = Pattern.compile("(.*)\\.(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL );


    private WrappedMongoCollection getCollectionMandatory( String collectionRef, boolean createCollectionIfMissing ) throws SQLException {
        WrappedMongoDatabase mongoDatabase = null;
        Matcher matcherDbIdentifier = PATTERN_DB_IDENTIFIER.matcher( collectionRef );
        Matcher matcherDbDot = PATTERN_DOT.matcher( collectionRef );
        if ( matcherDbIdentifier.matches() ){
            mongoDatabase = getDatabase( matcherDbIdentifier.group(1));
            collectionRef = matcherDbIdentifier.group(2);
        } else if ( matcherDbDot.matches() ){
            mongoDatabase = getDatabase( matcherDbDot.group(1));
            collectionRef = matcherDbDot.group(2);
        }
        if ( mongoDatabase == null ) throw new SQLException( "Cannot find database '" + collectionRef + "'.");
        Matcher matcherCollectionIdentifier = PATTERN_COLLECTION_IDENTIFIER.matcher( collectionRef );
        if ( matcherCollectionIdentifier.matches() ){
            collectionRef = matcherDbIdentifier.group(1);
        }
        WrappedMongoCollection collection = mongoDatabase.getCollection( collectionRef );
        if ( collection == null && createCollectionIfMissing ) {
            mongoDatabase.createCollection( collectionRef );
            collection = mongoDatabase.getCollection( collectionRef);
        }
        if ( collection == null ) throw new SQLException( "Cannot find collection '" + collectionRef + "'.");
        return collection;
    }

    @Override
    public void close() throws SQLException	{
        if (lastResultSet != null) {
            lastResultSet.close();
        }
        this.isClosed = true;
    }

    @Override
    public int getMaxFieldSize()
    {

        return 0;
    }

    @Override
    public void setMaxFieldSize(final int max) {	}

    @Override
    public int getMaxRows() {
        return maxRows;
    }

    @Override
    public void setMaxRows(final int max)
    {
        this.maxRows = max;
    }

    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException{}

    @Override
    public int getQueryTimeout() throws SQLException {
        checkClosed();
        throw new SQLFeatureNotSupportedException("MongoDB provides no support for query timeouts.");
    }

    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        checkClosed();
        throw new SQLFeatureNotSupportedException("MongoDB provides no support for query timeouts.");
    }

    @Override
    public void cancel() throws SQLException {
        checkClosed();
        throw new SQLFeatureNotSupportedException("MongoDB provides no support for interrupting an operation.");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException	{
        checkClosed();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException	{
        checkClosed();
    }

    @Override
    public void setCursorName(final String name) throws SQLException {
        checkClosed();
        // Driver doesn't support positioned updates for now, so no-op.
    }

    @Override
    public ResultSet getResultSet() throws SQLException	{
        checkClosed();
        return lastResultSet;
    }

    @Override
    public int getUpdateCount() throws SQLException	{
        checkClosed();
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException{}

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException{}

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(final String sql) throws SQLException{
        executeUpdate(sql);
    }

    @Override
    public void clearBatch() throws SQLException{}

    @Override
    public int[] executeBatch() throws SQLException	{
        checkClosed();
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkClosed();
        return this.connection;
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException
    {
        checkClosed();
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException	{
        checkClosed();
        return null;
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException	{
        checkClosed();
        executeUpdate( sql );
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        checkClosed();
        executeUpdate( sql );
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        checkClosed();
        executeUpdate( sql );
        return 0;
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        checkClosed();
        executeUpdate( sql );
        return false;
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException	{
        checkClosed();
        executeUpdate( sql );
        return false;
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        checkClosed();
        executeUpdate( sql );
        return false;
    }

    @Override
    public int getResultSetHoldability()  {
        return 0;
    }

    @Override
    public boolean isClosed()  {
        return isClosed;
    }

    @Override
    public void setPoolable(final boolean poolable) {}

    @Override
    public boolean isPoolable()	{
        return false;
    }

    private void checkClosed() throws SQLException {
        if (isClosed) {
            throw new SQLException("Statement was previously closed.");
        }
    }

    @Override
    public void closeOnCompletion() throws SQLException {
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        execute(query);
        return lastResultSet;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {

    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {

    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {

    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {

    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {

    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {

    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {

    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {

    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {

    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {

    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {

    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {

    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    }

    @Override
    public void clearParameters() throws SQLException {
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    }


    @Override
    public boolean execute() throws SQLException {
        return false;
    }

    @Override
    public void addBatch() throws SQLException {
        executeUpdate();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {

    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {

    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {

    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {

    }
}




