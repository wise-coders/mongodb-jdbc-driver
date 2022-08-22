package com.wisecoders.dbschema.mongodb.structure;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import com.wisecoders.dbschema.mongodb.ScanStrategy;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedFindIterable;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoCollection;
import org.bson.Document;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wisecoders.dbschema.mongodb.JdbcDriver.LOGGER;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class MetaCollection extends MetaObject {

    public final MetaDatabase metaDatabase;

    public final List<MetaIndex> metaIndexes = new ArrayList<>();

    public MetaCollection( final MetaDatabase metaDatabase, final String name ) {
        super(null, name, "object", TYPE_OBJECT);
        this.metaDatabase = metaDatabase;
        final MetaField id = new MetaField(this, "_id", "ObjectID", Types.ROWID);
        id.setMandatory(true);
        fields.add(id);
        MetaIndex pkId = createMetaIndex( "_id_", true, false );
        pkId.addColumn( id );
    }

    public MetaIndex createMetaIndex(String name, boolean pk, boolean unique){
        MetaIndex index = new MetaIndex( this, name, pk, unique );
        metaIndexes.add( index );
        return index;
    }

    public MetaCollection scanDocumentsAndIndexes(final WrappedMongoCollection mongoCollection, final ScanStrategy strategy ) {
        scanDocuments( mongoCollection, strategy );
        scanIndexes( mongoCollection );
        return this;
    }

    private void scanDocuments(final WrappedMongoCollection mongoCollection, ScanStrategy strategy) {
        long scanStartTime = System.currentTimeMillis();
        long cnt = 0;
        {
            final MongoCursor cursor = mongoCollection.find().iterator();
            try {
                while (cursor.hasNext() && ++cnt < strategy.SCAN_COUNT) {
                    scanDocument(cursor.next());
                }
            } finally {
                cursor.close();
            }
        }
        if ( getFieldCount() < 400 && cnt < strategy.SCAN_COUNT && strategy != ScanStrategy.full ){
            int cnt2 = 0;
            final MongoCursor cursor = mongoCollection.find().sort("{_id:-1}").iterator();
            try {
                while ( cursor.hasNext() && ++cnt2 < strategy.SCAN_COUNT ) {
                    scanDocument(cursor.next());
                }
            } finally {
                cursor.close();
            }
            cnt +=cnt2;
        }
        LOGGER.log( Level.INFO, "Scanned " + mongoCollection + " " + cnt + " documents, " + getFieldCount() + " fields in " + ( System.currentTimeMillis() - scanStartTime ) + "ms" );
    }

    private static final String KEY_NAME = "name";
    private static final String KEY_UNIQUE = "unique";
    private static final String KEY_KEY = "key";

    public void scanIndexes(final WrappedMongoCollection mongoCollection ){
        try {
            ListIndexesIterable<Document> iterable = mongoCollection.listIndexes();
            for ( Object indexObject : iterable ){
                if ( indexObject instanceof Map){
                    Map indexMap = (Map)indexObject;
                    final String indexName = String.valueOf(indexMap.get(KEY_NAME));
                    final boolean indexIsPk = "_id_".endsWith(indexName);
                    final boolean indexIsUnique = Boolean.TRUE.equals(indexMap.get(KEY_UNIQUE));
                    final Object columnsObj = indexMap.get(KEY_KEY);
                    if ( columnsObj instanceof Map ){
                        final Map columnsMap = (Map)columnsObj;
                        MetaIndex metaIndex = createMetaIndex(indexName, indexIsPk, indexIsUnique);
                        for ( Object fieldNameObj : columnsMap.keySet() ){
                            final MetaField metaField = findField((String) fieldNameObj);
                            if (metaField == null) {
                                LOGGER.log(Level.INFO, "MongoJDBC discover index cannot find metaField '" + fieldNameObj + "' for index " + indexObject );
                            } else {
                                metaIndex.addColumn( metaField );
                            }
                        }
                    }
                }
            }
        } catch ( Throwable ex ){
            LOGGER.log( Level.SEVERE, "Error in discover indexes " + getNameWithPath() + ". ", ex );
        }
    }

}
