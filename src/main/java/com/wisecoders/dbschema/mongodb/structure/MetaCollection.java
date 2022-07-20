package com.wisecoders.dbschema.mongodb.structure;

import com.mongodb.client.ListIndexesIterable;
import com.wisecoders.dbschema.mongodb.ScanStrategy;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedFindIterable;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class MetaCollection extends MetaObject {

    public boolean referencesDiscovered = false;
    public final MetaDatabase metaDatabase;

    public final List<MetaIndex> metaIndexes = new ArrayList<>();

    public static final Logger LOGGER = Logger.getLogger( MetaCollection.class.getName() );

    public MetaCollection( final MetaDatabase metaDatabase, final String name ){
        super( null, name, "object", TYPE_OBJECT);
        this.metaDatabase = metaDatabase;
    }

    public MetaIndex createMetaIndex(String name, boolean pk, boolean unique){
        MetaIndex index = new MetaIndex( this, name, "_id_".endsWith( name), false );
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
        long skipTime = 0;
        long documentCount = mongoCollection.count();
        int skipCount = 1;
        if ( documentCount > 2 * strategy.SCAN_FIRST_LAST ) {
            skipCount =  (int)Math.max( 1, ( documentCount - (2 * strategy.SCAN_FIRST_LAST) ) / strategy.SCAN_BETWEEN );
        }
        long i = 0;
        final WrappedFindIterable jFindIterable = mongoCollection.find();
        for (Object o : jFindIterable) {
            scanDocument(o);
            if (skipCount > 1 && i + skipCount < documentCount && i > strategy.SCAN_FIRST_LAST && i < documentCount - strategy.SCAN_FIRST_LAST) {
                skipTime -= System.currentTimeMillis();
                jFindIterable.skip(skipCount);
                skipTime += System.currentTimeMillis();
            }
            i++;
            // DON'T SCAN IF THE FIELD COUNT IS ALREADY TOO HIGH
            if ( getFieldCount() > 400 || System.currentTimeMillis() - scanStartTime > 4000 ){
                break;
            }
        }
        LOGGER.log( Level.INFO, "Scanned " + mongoCollection + " with " + documentCount + " documents, " + i + " scanned, " + getFieldCount() + " fields in " + ( System.currentTimeMillis() - scanStartTime ) + "ms, skipTime=" + skipTime );
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
