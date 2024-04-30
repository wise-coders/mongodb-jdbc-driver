package com.wisecoders.dbschema.mongodb.structure;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import com.wisecoders.dbschema.mongodb.ScanStrategy;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.wisecoders.dbschema.mongodb.JdbcDriver.LOGGER;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */
public class MetaCollection extends MetaObject {

    public final MetaDatabase metaDatabase;
    public final boolean isVirtual;

    public final List<MetaIndex> metaIndexes = new ArrayList<>();

    public MetaCollection( final MetaDatabase metaDatabase, final String name, boolean isVirtual) {
        super(null, name );
        this.metaDatabase = metaDatabase;
        this.isVirtual = isVirtual;
        setTypeName("object");
        setJavaType( TYPE_OBJECT );
        final MetaField idField = new MetaField(this, "_id" );
        idField.setMandatory(true);
        idField.setTypeClass( ObjectId.class );
        fields.add(idField);
        MetaIndex pkId = createMetaIndex( "_id_", true, false );
        pkId.addColumn( idField );
    }

    public MetaIndex createMetaIndex(String name, boolean pk, boolean unique){
        MetaIndex index = new MetaIndex( this, name, pk, unique );
        metaIndexes.add( index );
        return index;
    }

    public MetaCollection scanDocumentsAndIndexes(final WrappedMongoCollection mongoCollection, final ScanStrategy strategy, boolean sortFields ) {
        scanDocuments( mongoCollection, strategy, sortFields );
        scanIndexes( mongoCollection );
        return this;
    }

    private void scanDocuments(final WrappedMongoCollection mongoCollection, ScanStrategy strategy, boolean sortFields ) {
        long scanStartTime = System.currentTimeMillis();
        long cnt = scan(mongoCollection, strategy, true, sortFields);
        if ( getFieldCount() < 400 && cnt == strategy.SCAN_COUNT && strategy != ScanStrategy.full ){
            cnt +=scan(mongoCollection, strategy, false, sortFields);;
        }
        LOGGER.log( Level.INFO, "Scanned " + mongoCollection + " " + cnt + " documents, " + getFieldCount() + " fields in " + ( System.currentTimeMillis() - scanStartTime ) + "ms" );
    }

    private long scan(WrappedMongoCollection mongoCollection, ScanStrategy strategy, boolean directionUp, boolean sortFields ) {
        long cnt = 0;
        try ( MongoCursor cursor = mongoCollection.find().sort("{_id:" + (directionUp ? "1" : "-1") + "}" ).iterator() ) {
            while (cursor.hasNext() && cnt < strategy.SCAN_COUNT) {
                scanDocument(cursor.next(), sortFields, 0);
                cnt++;
            }
        }
        return cnt;
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
