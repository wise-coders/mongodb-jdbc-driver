
package com.wisecoders.dbschema.mongodb;

import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoClient;

import java.sql.*;
import java.util.Properties;
import java.util.logging.*;


/**
 * Minimal implementation of the JDBC standards for MongoDb database.
 * The URL excepting the jdbc: prefix is passed as it is to the MongoDb native Java driver.
 * Example :
 * jdbc:mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
 *
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class JdbcDriver implements Driver
{
    private final DriverPropertyInfoHelper propertyInfoHelper = new DriverPropertyInfoHelper();

    public static final Logger LOGGER = Logger.getLogger( JdbcDriver.class.getName() );

    static {
        try {
            DriverManager.registerDriver( new JdbcDriver());
            LOGGER.setLevel(Level.SEVERE);
            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINEST);
            consoleHandler.setFormatter(new SimpleFormatter());

            LOGGER.setLevel(Level.FINEST);
            LOGGER.addHandler(consoleHandler);

            final FileHandler fileHandler = new FileHandler(System.getProperty("user.home") + "/.DbSchema/logs/MongoDbJdbcDriver.log");
            fileHandler.setFormatter( new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

        } catch ( Exception ex ){
            ex.printStackTrace();
        }
    }


    /**
     * Connect to the database using a URL like :
     * jdbc:mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
     * The URL excepting the jdbc: prefix is passed as it is to the MongoDb native Java driver.
     */
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if ( url != null && acceptsURL( url )){
            if ( url.startsWith("jdbc:")) {
                url = url.substring("jdbc:".length());
            }
            LOGGER.info("Connect URL: " + url );
            int idx;
            ScanStrategy scan = ScanStrategy.fast;
            boolean expand = false;
            String newUrl = url, urlWithoutParams = url;
            if ( ( idx = url.indexOf("?")) > 0 ){
                String paramsURL = url.substring( idx+1);
                urlWithoutParams = url.substring( 0, idx );
                StringBuilder sbParams = new StringBuilder();
                for ( String pair: paramsURL.split("&")){
                    String[] pairArr = pair.split("=");
                    String key = pairArr.length == 2 ? pairArr[0].toLowerCase() : "";
                    switch( key ){
                        case "scan": try { scan = ScanStrategy.valueOf( pairArr[1]);} catch ( IllegalArgumentException ex ){} break;
                        case "expand": expand = Boolean.parseBoolean( pairArr[1]); break;
                        default:
                            if ( sbParams.length() > 0 ) sbParams.append("&");
                            sbParams.append( pair );
                            break;
                    }
                }
                newUrl = url.substring(0, idx) + "?" + sbParams;
            }
            String databaseName = "admin";
            if ( ( idx = urlWithoutParams.lastIndexOf("/")) > 1 && urlWithoutParams.charAt( idx -1) != '/' ){
                databaseName = urlWithoutParams.substring( idx + 1 );
            }

            LOGGER.info("MongoClient URL: " + url + " rewritten as " + newUrl );
            final WrappedMongoClient client = new WrappedMongoClient(url, info, databaseName, scan, expand );
            return new MongoConnection(client);
        }
        return null;
    }


    /**
     * URLs accepted are of the form: jdbc:mongodb[+srv]://<server>[:27017]/<db-name>
     *
     * @see java.sql.Driver#acceptsURL(java.lang.String)
     */
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("mongodb") || url.startsWith("jdbc:mongodb");
    }

    /**
     * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
     */
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return propertyInfoHelper.getPropertyInfo();
    }

    /**
     * @see java.sql.Driver#getMajorVersion()
     */
    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    /**
     * @see java.sql.Driver#getMinorVersion()
     */
    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    /**
     * @see java.sql.Driver#jdbcCompliant()
     */
    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

}
