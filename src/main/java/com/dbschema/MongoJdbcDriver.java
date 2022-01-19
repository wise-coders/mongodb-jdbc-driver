
package com.dbschema;

import com.dbschema.wrappers.WrappedMongoClient;

import java.sql.*;
import java.util.Properties;
import java.util.logging.*;


/**
 * Minimal implementation of the JDBC standards for MongoDb database.
 * The URL excepting the jdbc: prefix is passed as it is to the MongoDb native Java driver.
 * Example :
 * jdbc:mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MongoJdbcDriver implements Driver
{
    private final DriverPropertyInfoHelper propertyInfoHelper = new DriverPropertyInfoHelper();

    public static final Logger LOGGER = Logger.getLogger( MongoJdbcDriver.class.getName() );

    static {
        try {
            DriverManager.registerDriver( new MongoJdbcDriver());
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
            ScanStrategy scan = ScanStrategy.fast;
            boolean expand = false;
            for (ScanStrategy s : ScanStrategy.values() ){
                if ( url.contains( "?scan=" + s ) || url.contains( "&scan=" + s )){
                    scan = s;
                    url = url.replaceFirst("\\?scan=" + s, "" ).replaceFirst("&scan=" + s, "");
                }
            }
            if ( url.contains( "?expand=true" ) || url.contains( "&expand=true" )){
                expand = true;
                url = url.replaceFirst("\\?expand=true", "" ).replaceFirst("&expand=true", "");
            }
            if ( url.startsWith("jdbc:")) {
                url = url.substring("jdbc:".length());
            }
            final WrappedMongoClient client = new WrappedMongoClient(url, info, scan, expand );
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
