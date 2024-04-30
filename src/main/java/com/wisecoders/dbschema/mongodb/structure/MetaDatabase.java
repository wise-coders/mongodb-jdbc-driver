package com.wisecoders.dbschema.mongodb.structure;

import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoCollection;
import com.wisecoders.dbschema.mongodb.wrappers.WrappedMongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.logging.Level;

import static com.wisecoders.dbschema.mongodb.JdbcDriver.LOGGER;


/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */
public class MetaDatabase {

    public final String name;
    private final Map<String, MetaCollection> metaCollections = new HashMap<>();
    private boolean referencesDiscovered = false;

    public MetaDatabase( String name ){
        this.name =  name;
    }

    public MetaCollection createMetaCollection(String name, boolean isVirtual ){
        MetaCollection metaCollection = new MetaCollection(this, name, isVirtual );
        metaCollections.put( name, metaCollection);
        return metaCollection;
    }

    public MetaCollection getMetaCollection(String name ){
        return metaCollections.get( name );
    }

    public Collection<MetaCollection> getMetaCollections(){
        return metaCollections.values();
    }

    public void dropMetaCollection(String name ){
        metaCollections.remove( name );
    }

    private void collectFieldsWithObjectId( List<MetaField> metaFields ){
        for ( MetaCollection collection : metaCollections.values() ){
            collection.collectFieldsWithObjectId( metaFields );
        }
    }

    public void discoverReferences(WrappedMongoDatabase mongoDatabase ){
        if ( !referencesDiscovered){
            try {
                LOGGER.info("Discover relationships in database " + name );
                referencesDiscovered = true;
                final List<MetaField> metaFields = new ArrayList<>();
                collectFieldsWithObjectId(metaFields);
                if ( !metaFields.isEmpty() ){
                    for ( MetaCollection _metaCollection : getMetaCollections() ){
                        final WrappedMongoCollection<Document> mongoCollection = mongoDatabase.getCollection( _metaCollection.name );
                        if ( mongoCollection != null ){
                            ObjectId[] objectIds = new ObjectId[metaFields.size()];

                            for ( int i = 0; i < metaFields.size(); i++ ) {
                                objectIds[i] = metaFields.get(i).getObjectId();
                            }
                            final Document inQuery = new Document();
                            inQuery.put("$in", objectIds);
                            final Document query = new Document(); //new BasicDBObject();
                            query.put("_id", inQuery );
                            for ( Object obj : mongoCollection.find(query).projection("{_id:1}")) {
                                if ( obj instanceof Map ) {
                                    Object value = ((Map) obj).get("_id");
                                    if ( value != null ){
                                        for ( MetaField metaField : metaFields ){
                                            if ( value.equals( metaField.getObjectId() )){
                                                metaField.createReferenceTo(_metaCollection);
                                                LOGGER.log(Level.INFO, "Found relationship  " + metaField.parentObject.name + " ( " + metaField.name + " ) ref " + _metaCollection.name);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                LOGGER.info("Discover relationships done.");
            } catch ( Throwable ex ){
                LOGGER.log( Level.SEVERE, "Error discovering relationships.", ex );
            }
        }
    }

}
