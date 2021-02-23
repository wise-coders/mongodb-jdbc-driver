package com.dbschema.schema;

import com.dbschema.ScanStrategy;
import com.dbschema.wrappers.WrappedFindIterable;
import com.dbschema.wrappers.WrappedMongoCollection;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaCollection extends MetaObject {

    private boolean isFirstDiscover = true;
    public boolean referencesDiscovered = false;


    public final MetaDatabase metaDatabase;
    public final List<MetaIndex> metaIndexes = new ArrayList<>();

    public MetaCollection(final WrappedMongoCollection mongoCollection, final MetaDatabase metaDatabase, final String name, final ScanStrategy strategy ){
        super( null, name, TYPE_OBJECT);
        this.metaDatabase = metaDatabase;

        switch ( strategy ){
            case medium:
                if( discoverCollectionFirstRecords( mongoCollection, 300 ) ){
                    discoverCollectionRandomRecords( mongoCollection, 300 );
                }
                break;
            case full:
                discoverCollectionFirstRecords( mongoCollection, Integer.MAX_VALUE);
                break;
            default :
                if( discoverCollectionFirstRecords( mongoCollection, 30 ) ){
                    discoverCollectionRandomRecords( mongoCollection, 100 );
                }
                break;
        }
        discoverIndexes( mongoCollection );

    }

    private MetaIndex createMetaIndex(String name, boolean pk, boolean unique){
        MetaIndex index = new MetaIndex( this, name, "_id_".endsWith( name), false );
        metaIndexes.add( index );
        return index;
    }

    private boolean discoverCollectionFirstRecords( WrappedMongoCollection mongoCollection, int iterations ) {
        MongoCursor cursor = mongoCollection.find().iterator();
        int iteration = 0;
        while( cursor.hasNext() && ++iteration <= iterations ){
            discoverMap(this, cursor.next());
        }
        cursor.close();
        return iteration >= iterations;
    }

    private void discoverCollectionRandomRecords( WrappedMongoCollection mongoCollection, int iterations ) {
        int skip = 10, i = 0;
        final WrappedFindIterable jFindIterable = mongoCollection.find(); // .limit(-1)
        while ( i++ < iterations ){
            final MongoCursor crs = jFindIterable.iterator();
            while( i++ < iterations && crs.hasNext() ){
                discoverMap( this, crs.next());
            }
            jFindIterable.skip( skip );
            skip = skip * 2;
        }
    }

    private void discoverMap(MetaObject parentObject, Object objValue){
        if ( objValue instanceof Map){
            Map map = (Map)objValue;
            for ( Object key : map.keySet() ){
                final Object keyValue = map.get( key );
                String type =( keyValue != null ? keyValue.getClass().getName() : "String" );
                if ( type.lastIndexOf('.') > 0 ) type = type.substring( type.lastIndexOf('.')+1 );
                if ( keyValue instanceof Map ) {
                    Map subMap = (Map)keyValue;
                    // "suburbs":[ { name: "Scarsdale" }, { name: "North Hills" } ] WOULD GENERATE SUB-ENTITIES 0,1,2,... FOR EACH LIST ENTRY. SKIP THIS
                    if ( allKeysAreNumbers( subMap )){
                        final MetaObject childrenMap = parentObject.createJsonArrayField(key.toString(), isFirstDiscover );
                        for ( Object subKey : subMap.keySet() ) {
                            discoverMap(childrenMap, subMap.get( subKey ));
                        }
                    } else {
                        final MetaObject childrenMap = parentObject.createJsonObjectField(key.toString(), isFirstDiscover );
                        if ( subMap.size() > 1 ){
                            childrenMap.setTypeArray();
                        }
                        discoverMap(childrenMap, keyValue);
                    }
                } else if ( keyValue instanceof List ){
                    final List list = (List)keyValue;
                    if ( (list.isEmpty() || isListOfDocuments(keyValue))  ) {
                        final MetaObject subDocument = parentObject.createJsonArrayField(key.toString(), isFirstDiscover );
                        for ( Object child : (List)keyValue ){
                            discoverMap(subDocument, child);
                        }
                    } else {
                        parentObject.createField((String) key, "array", MetaObject.TYPE_ARRAY, isFirstDiscover );
                    }
                } else {
                    MetaField field = parentObject.createField((String) key, type, getJavaType( keyValue ), isFirstDiscover );
                    // VALUES WHICH ARE OBJECTID AND ARE NOT _id IN THE ROOT MAP
                    if ( keyValue instanceof ObjectId && !"_id".equals( field.getNameWithPath() ) ){
                        field.addObjectId((ObjectId) keyValue);
                    }
                }
            }
            for ( MetaField field: parentObject.fields){
                if ( !map.containsKey( field.name )){
                    field.setMandatory( false );
                }
            }
        }
        isFirstDiscover = false;
    }

    private static final Pattern PATTERN_NUMBER = Pattern.compile("\\d+");

    private boolean allKeysAreNumbers( Map map ){
        if ( map.isEmpty() ) return false;
        for( Object key : map.keySet()){
            boolean isNumber = key instanceof Number || ( key instanceof String && PATTERN_NUMBER.matcher( (String)key).matches() );
            if ( !isNumber ) return false;
        }
        return true;
    }

    private int getJavaType( Object value ){
        if ( value instanceof Integer ) return java.sql.Types.INTEGER;
        else if ( value instanceof Timestamp) return java.sql.Types.TIMESTAMP;
        else if ( value instanceof Date) return java.sql.Types.DATE;
        else if ( value instanceof Double ) return java.sql.Types.DOUBLE;
        return java.sql.Types.VARCHAR;
    }


    private boolean isListOfDocuments(Object obj){
        if ( obj instanceof List ){
            List list = (List)obj;
            for ( Object val : list ){
                if ( !( val instanceof Map ) ) return false;
            }
            return list.size() > 0;
        }
        return false;
    }

    private static final String KEY_NAME = "name";
    private static final String KEY_UNIQUE = "unique";
    private static final String KEY_KEY = "key";

    private void discoverIndexes( WrappedMongoCollection dbCollection ){
        try {
            ListIndexesIterable iterable = dbCollection.listIndexes();
            for ( Object indexObject : iterable ){
                if ( indexObject instanceof Map ){
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
                                System.out.println("MongoJDBC discover index cannot find metaField '" + fieldNameObj + "' for index " + indexObject );
                            } else {
                                metaIndex.addColumn( metaField );
                            }
                        }
                    }
                }
            }
        } catch ( Throwable ex ){
            System.out.println("Error in discover indexes " + dbCollection + "." + this + ". " + ex );
        }
    }

}
