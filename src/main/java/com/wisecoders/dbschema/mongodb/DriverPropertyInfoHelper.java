package com.wisecoders.dbschema.mongodb;

import java.sql.DriverPropertyInfo;
import java.util.ArrayList;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */
class DriverPropertyInfoHelper {

    private static final String CONNECTIONS_PER_HOST = "connectionsPerHost";
    private static final String CONNECT_TIMEOUT = "connectTimeout";
    private static final String CURSOR_FINALIZER_ENABLED = "cursorFinalizerEnabled";
    private static final String READ_PREFERENCE = "readPreference";
    private static final String SOCKET_TIMEOUT = "socketTimeout";

    DriverPropertyInfo[] getPropertyInfo()
    {
        ArrayList<DriverPropertyInfo> propInfos = new ArrayList<DriverPropertyInfo>();


        addPropInfo(propInfos, CONNECTIONS_PER_HOST, "10", "The maximum number of connections allowed per "
                + "host for this Mongo instance. Those connections will be kept in a pool when idle. Once the "
                + "pool is exhausted, any operation requiring a connection will block waiting for an available "
                + "connection.", null);

        addPropInfo(propInfos, CONNECT_TIMEOUT, "10000", "The connection timeout in milliseconds. A value "
                + "of 0 means no timeout. It is used solely when establishing a new connection "
                + "Socket.connect(java.net.SocketAddress, int)", null);

        addPropInfo(propInfos, CURSOR_FINALIZER_ENABLED, "true", "Sets whether there is a a finalize "
                + "method created that cleans up instances of DBCursor that the client does not close. If you "
                + "are careful to always call the close method of DBCursor, then this can safely be set to false.",
                null);

        addPropInfo(propInfos, READ_PREFERENCE, "primary",
                "represents preferred replica set members to which a query or command can be sent", new String[] {
                "primary", "primary preferred", "secondary", "secondary preferred", "nearest" });

        addPropInfo(propInfos, SOCKET_TIMEOUT, "0", "The socket timeout in milliseconds It is used for "
                + "I/O socket read and write operations "
                + "Socket.setSoTimeout(int) Default is 0 and means no timeout.", null);

        return propInfos.toArray(new DriverPropertyInfo[0]);
    }

    private void addPropInfo(final ArrayList<DriverPropertyInfo> propInfos, final String propName,
                             final String defaultVal, final String description, final String[] choices)
    {
        DriverPropertyInfo newProp = new DriverPropertyInfo(propName, defaultVal);
        newProp.description = description;
        if (choices != null)
        {
            newProp.choices = choices;
        }
        propInfos.add(newProp);
    }
}
