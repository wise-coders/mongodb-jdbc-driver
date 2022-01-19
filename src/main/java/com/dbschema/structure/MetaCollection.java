package com.dbschema.structure;

import com.dbschema.ScanStrategy;
import com.dbschema.wrappers.WrappedFindIterable;
import com.dbschema.wrappers.WrappedMongoCollection;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
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

    public MetaCollection scanDocuments(final WrappedMongoCollection mongoCollection, final ScanStrategy strategy ) {
        switch (strategy) {
            case medium:
                if (scanFirstDocuments( mongoCollection,500)) {
                    scanRandomDocuments( mongoCollection,700);
                }
                break;
            case full:
                scanFirstDocuments( mongoCollection, Integer.MAX_VALUE);
                break;
            default:
                if (scanFirstDocuments( mongoCollection,50)) {
                    scanRandomDocuments( mongoCollection,150);
                }
                break;
        }
        scanIndexes( mongoCollection );
        return this;
    }



    private boolean scanFirstDocuments(final WrappedMongoCollection mongoCollection, int iterations ) {
        MongoCursor cursor = mongoCollection.find().iterator();
        int iteration = 0;
        while( cursor.hasNext() && ++iteration <= iterations ){
            scanDocument( cursor.next());
        }
        cursor.close();
        return iteration >= iterations;
    }

    private void scanRandomDocuments(final WrappedMongoCollection mongoCollection, int iterations ) {
        int skip = 10, i = 0;
        final WrappedFindIterable jFindIterable = mongoCollection.find(); // .limit(-1)
        while ( i++ < iterations ){
            final MongoCursor crs = jFindIterable.iterator();
            while( i++ < iterations && crs.hasNext() ){
                scanDocument( crs.next());
            }
            jFindIterable.skip( skip );
            skip = skip * 2;
        }
    }

    private static final String KEY_NAME = "name";
    private static final String KEY_UNIQUE = "unique";
    private static final String KEY_KEY = "key";

    private void scanIndexes(final WrappedMongoCollection mongoCollection ){
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
