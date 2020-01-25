package com.dbschema.wrappers;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedMongoCollection<TDocument> {

    private final MongoCollection<TDocument> mongoCollection;
    WrappedMongoCollection(MongoCollection<TDocument> base ){
        this.mongoCollection = base;
    }

    static Bson toBson(Object obj ){
        if ( obj instanceof Map ){
            return new BasicDBObject((Map)obj);
        } else {
            String json = new Gson().toJson(obj);
            return obj != null ? BasicDBObject.parse(json) : null;
        }
    }

    private TDocument toDocument( Map map ){
        return (TDocument)( new Document( map ));
    }

    private static List toBsonList(List list ){
        if ( list != null ){
            ArrayList<Object> ret = new ArrayList<>();
            for ( Object obj : list ) {
                ret.add( toBson( obj ));
            }
            return ret;
        }
        return list;
    }

    @Override
    public String toString() {
        return mongoCollection.toString();
    }

    public Object explain(){
        // Support for explain()
        return find().explain();
    }

    public MongoNamespace getNamespace() {
        return mongoCollection.getNamespace();
    }


    public Class getDocumentClass() {
        return mongoCollection.getDocumentClass();
    }


    public CodecRegistry getCodecRegistry() {
        return mongoCollection.getCodecRegistry();
    }


    public ReadPreference getReadPreference() {
        return mongoCollection.getReadPreference();
    }


    public WriteConcern getWriteConcern() {
        return mongoCollection.getWriteConcern();
    }


    public ReadConcern getReadConcern() {
        return mongoCollection.getReadConcern();
    }


    public WrappedMongoCollection withDocumentClass(Class clazz) {
        mongoCollection.withDocumentClass( clazz );
        return this;
    }


    public WrappedMongoCollection withCodecRegistry(CodecRegistry codecRegistry) {
        mongoCollection.withCodecRegistry( codecRegistry );
        return this;
    }


    public WrappedMongoCollection withReadPreference(ReadPreference readPreference) {
        mongoCollection.withReadPreference( readPreference );
        return this;
    }


    public WrappedMongoCollection withWriteConcern(WriteConcern writeConcern) {
        mongoCollection.withWriteConcern( writeConcern );
        return this;
    }


    public WrappedMongoCollection withReadConcern(ReadConcern readConcern) {
        mongoCollection.withReadConcern( readConcern );
        return this;
    }


    public long count() {
        return mongoCollection.countDocuments();
    }


    public long count(Map filter) {
        return mongoCollection.countDocuments( toBson(filter));
    }


    public long count(Map filter, CountOptions options) {
        return mongoCollection.countDocuments( toBson( filter), options );
    }


    public long count(ClientSession clientSession) {
        return mongoCollection.countDocuments( clientSession );
    }


    public long count(ClientSession clientSession, Map filter) {
        return mongoCollection.countDocuments(clientSession, toBson( filter ));
    }


    public long count(ClientSession clientSession, Map filter, CountOptions options) {
        return mongoCollection.countDocuments( clientSession, toBson(filter), options );
    }


    public long countDocuments() {
        return mongoCollection.countDocuments();
    }


    public long countDocuments(Map filter) {
        return mongoCollection.countDocuments( toBson( filter ));
    }


    public long countDocuments(Map filter, CountOptions options) {
        return mongoCollection.countDocuments( toBson( filter ), options);
    }


    public long countDocuments(ClientSession clientSession) {
        return mongoCollection.countDocuments( clientSession );
    }


    public long countDocuments(ClientSession clientSession, Map filter) {
        return mongoCollection.countDocuments( clientSession, toBson(filter) );
    }


    public long countDocuments(ClientSession clientSession, Map filter, CountOptions options) {
        return mongoCollection.countDocuments( clientSession, toBson(filter), options);
    }


    public long estimatedDocumentCount() {
        return mongoCollection.estimatedDocumentCount();
    }


    public long estimatedDocumentCount(EstimatedDocumentCountOptions options) {
        return mongoCollection.estimatedDocumentCount(options);
    }


    public DistinctIterable distinct(String fieldName, Class aClass) {
        return mongoCollection.distinct( fieldName, aClass );
    }

    public DistinctIterable distinct(String fieldName) {
        return mongoCollection.distinct( fieldName, BsonString.class );
    }


    public DistinctIterable distinct(String fieldName, Map filter, Class aClass) {
        return mongoCollection.distinct( fieldName, toBson( filter ), aClass);
    }


    public DistinctIterable distinct(ClientSession clientSession, String fieldName, Class aClass) {
        return mongoCollection.distinct( clientSession, fieldName, aClass );
    }


    public DistinctIterable distinct(ClientSession clientSession, String fieldName, Map filter, Class aClass) {
        return mongoCollection.distinct( clientSession, fieldName, toBson(filter), aClass);
    }


    public WrappedFindIterable find() {
        return new WrappedFindIterable(mongoCollection.find());
    }


    public WrappedFindIterable find(Class aClass) {
        return new WrappedFindIterable(mongoCollection.find( aClass ));
    }


    public WrappedFindIterable find(Map filter) {
        return new WrappedFindIterable( mongoCollection.find(toBson(filter)));
    }

    public WrappedFindIterable find(Map filter, Map projection) {
        return new WrappedFindIterable( mongoCollection.find(toBson(filter)).projection( toBson(projection) ));
    }


    public WrappedFindIterable find(Map filter, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( toBson(filter), aClass));
    }


    public WrappedFindIterable find(ClientSession clientSession) {
        return new WrappedFindIterable( mongoCollection.find(clientSession));
    }


    public WrappedFindIterable find(ClientSession clientSession, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, aClass ));
    }


    public WrappedFindIterable find(ClientSession clientSession, Map filter) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, toBson(filter) ));
    }


    public WrappedFindIterable find(ClientSession clientSession, Map filter, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, toBson(filter), aClass ));
    }


    public AggregateIterable aggregate(List pipeline) {
        return mongoCollection.aggregate( toBsonList(pipeline) );
    }


    public AggregateIterable aggregate(List pipeline, Class aClass) {
        return mongoCollection.aggregate( toBsonList(pipeline), aClass );
    }


    public AggregateIterable aggregate(ClientSession clientSession, List pipeline) {
        return mongoCollection.aggregate( clientSession, toBsonList(pipeline) );
    }


    public AggregateIterable aggregate(ClientSession clientSession, List pipeline, Class aClass) {
        return mongoCollection.aggregate( clientSession, toBsonList(pipeline), aClass );
    }


    public ChangeStreamIterable watch() {
        return mongoCollection.watch();
    }


    public ChangeStreamIterable watch(Class aClass) {
        return mongoCollection.watch(aClass);
    }


    public ChangeStreamIterable watch(List pipeline) {
        return mongoCollection.watch(pipeline);
    }


    public ChangeStreamIterable watch(List pipeline, Class aClass) {
        return mongoCollection.watch(pipeline, aClass);
    }


    public ChangeStreamIterable watch(ClientSession clientSession) {
        return mongoCollection.watch(clientSession);
    }


    public ChangeStreamIterable watch(ClientSession clientSession, Class aClass) {
        return mongoCollection.watch(clientSession, aClass);
    }


    public ChangeStreamIterable watch(ClientSession clientSession, List pipeline) {
        return mongoCollection.watch(clientSession, pipeline);
    }


    public ChangeStreamIterable watch(ClientSession clientSession, List pipeline, Class aClass) {
        return mongoCollection.watch(clientSession,pipeline, aClass);
    }


    public MapReduceIterable mapReduce(String mapFunction, String reduceFunction) {
        return mongoCollection.mapReduce( mapFunction, reduceFunction);
    }


    public MapReduceIterable mapReduce(String mapFunction, String reduceFunction, Class aClass) {
        return mongoCollection.mapReduce( mapFunction, reduceFunction, aClass );
    }


    public MapReduceIterable mapReduce(ClientSession clientSession, String mapFunction, String reduceFunction) {
        return mongoCollection.mapReduce( clientSession, mapFunction, reduceFunction);
    }


    public MapReduceIterable mapReduce(ClientSession clientSession, String mapFunction, String reduceFunction, Class aClass) {
        return mongoCollection.mapReduce( clientSession, mapFunction, reduceFunction, aClass );
    }


    public BulkWriteResult bulkWrite(List requests) {
        return mongoCollection.bulkWrite( requests );
    }


    public BulkWriteResult bulkWrite(List requests, BulkWriteOptions options) {
        return mongoCollection.bulkWrite( requests, options );
    }


    public BulkWriteResult bulkWrite(ClientSession clientSession, List requests) {
        return mongoCollection.bulkWrite( clientSession, requests );
    }


    public BulkWriteResult bulkWrite(ClientSession clientSession, List requests, BulkWriteOptions options) {
        return mongoCollection.bulkWrite( clientSession, requests, options );
    }


    public void insertOne(Map o) {
        mongoCollection.insertOne( (TDocument)( new Document( o )));
    }

    public void insertOne(Map o, InsertOneOptions options) {
        mongoCollection.insertOne( toDocument(o), options );
    }


    public void insertOne(ClientSession clientSession, Map map) {
        mongoCollection.insertOne( clientSession, toDocument( map ));
    }


    public void insertOne(ClientSession clientSession, Map o, InsertOneOptions options) {
        mongoCollection.insertOne( clientSession, toDocument(o), options );
    }


    public void insertMany(List<Map> list) {
        for ( Map map: list ){
            insertOne( map );
        }
    }

    public void insertMany(Map map) {
        insertOne( map );
    }

    public void insert(Map o) {
        mongoCollection.insertOne( (TDocument)( new Document( o )));
    }

    public void insert(List<Map> list) {
        for ( Map map: list ){
            insertOne( map );
        }
    }


    public void insertMany(List<Map> list, InsertManyOptions options) {
    }


    public void insertMany(ClientSession clientSession, List list) {

    }


    public void insertMany(ClientSession clientSession, List list, InsertManyOptions options) {

    }


    public DeleteResult deleteOne(Map filter) {
        return mongoCollection.deleteOne( toBson( filter ));
    }


    public DeleteResult deleteOne(Map filter, DeleteOptions options) {
        return mongoCollection.deleteOne( toBson(filter), options);
    }


    public DeleteResult deleteOne(ClientSession clientSession, Map filter) {
        return mongoCollection.deleteOne( clientSession, toBson(filter));
    }


    public DeleteResult deleteOne(ClientSession clientSession, Map filter, DeleteOptions options) {
        return mongoCollection.deleteOne( clientSession, toBson(filter), options );
    }


    public DeleteResult deleteMany(Map filter) {
        return mongoCollection.deleteMany( toBson(filter));
    }


    public DeleteResult deleteMany(Map filter, DeleteOptions options) {
        return mongoCollection.deleteMany( toBson(filter), options);
    }


    public DeleteResult deleteMany(ClientSession clientSession, Map filter) {
        return mongoCollection.deleteMany( clientSession, toBson( filter ));
    }


    public DeleteResult deleteMany(ClientSession clientSession, Map filter, DeleteOptions options) {
        return mongoCollection.deleteMany( clientSession, toBson(filter), options );
    }


    public UpdateResult replaceOne(Map filter, Map replacement) {
        return mongoCollection.replaceOne( toBson(filter), toDocument(replacement) );
    }


    public UpdateResult replaceOne(Bson filter, Object replacement, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult replaceOne(Bson filter, Object replacement, ReplaceOptions replaceOptions) {
        return null;
    }


    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Object replacement) {
        return null;
    }


    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Object replacement, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Object replacement, ReplaceOptions replaceOptions) {
        return null;
    }


    public UpdateResult updateOne(Map filter, Map update) {
        return mongoCollection.updateOne( toBson( filter), toBson( update ));
    }


    public UpdateResult updateOne(Map filter, Map update, UpdateOptions updateOptions) {
        return mongoCollection.updateOne( toBson(filter), toBson(update), updateOptions);
    }


    public UpdateResult updateOne(ClientSession clientSession, Bson filter, Bson update) {
        return null;
    }


    public UpdateResult updateOne(ClientSession clientSession, Bson filter, Bson update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateOne(Bson filter, List update) {
        return null;
    }


    public UpdateResult updateOne(Bson filter, List update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateOne(ClientSession clientSession, Bson filter, List update) {
        return null;
    }


    public UpdateResult updateOne(ClientSession clientSession, Bson filter, List update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateMany(Map filter, Map update) {
        return mongoCollection.updateMany( toBson(filter), toBson(update));
    }


    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateMany(ClientSession clientSession, Bson filter, Bson update) {
        return null;
    }


    public UpdateResult updateMany(ClientSession clientSession, Bson filter, Bson update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateMany(Map filter, List update) {
        return mongoCollection.updateMany( toBson(filter), update);
    }


    public UpdateResult updateMany(Bson filter, List update, UpdateOptions updateOptions) {
        return null;
    }


    public UpdateResult updateMany(ClientSession clientSession, Bson filter, List update) {
        return null;
    }


    public UpdateResult updateMany(ClientSession clientSession, Bson filter, List update, UpdateOptions updateOptions) {
        return null;
    }


    public Object findOneAndDelete(Map filter) {
        return mongoCollection.findOneAndDelete( toBson( filter ));
    }


    public Object findOneAndDelete(Map filter, FindOneAndDeleteOptions options) {
        return mongoCollection.findOneAndDelete( toBson(filter), options );
    }


    public Object findOneAndDelete(ClientSession clientSession, Bson filter) {
        return null;
    }


    public Object findOneAndDelete(ClientSession clientSession, Bson filter, FindOneAndDeleteOptions options) {
        return null;
    }


    public Object findOneAndReplace(Bson filter, Object replacement) {
        return null;
    }


    public Object findOneAndReplace(Bson filter, Object replacement, FindOneAndReplaceOptions options) {
        return null;
    }


    public Object findOneAndReplace(ClientSession clientSession, Bson filter, Object replacement) {
        return null;
    }


    public Object findOneAndReplace(ClientSession clientSession, Bson filter, Object replacement, FindOneAndReplaceOptions options) {
        return null;
    }


    public Object findOneAndUpdate(Bson filter, Bson update) {
        return null;
    }


    public Object findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return null;
    }


    public Object findOneAndUpdate(ClientSession clientSession, Bson filter, Bson update) {
        return null;
    }


    public Object findOneAndUpdate(ClientSession clientSession, Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return null;
    }


    public Object findOneAndUpdate(Bson filter, List update) {
        return null;
    }


    public Object findOneAndUpdate(Bson filter, List update, FindOneAndUpdateOptions options) {
        return null;
    }


    public Object findOneAndUpdate(ClientSession clientSession, Bson filter, List update) {
        return null;
    }


    public Object findOneAndUpdate(ClientSession clientSession, Bson filter, List update, FindOneAndUpdateOptions options) {
        return null;
    }


    public void drop() {
        mongoCollection.drop();
    }


    public void drop(ClientSession clientSession) {
        mongoCollection.drop( clientSession );
    }


    public String createIndex(Map keys) {
        return mongoCollection.createIndex( toBson( keys ));
    }


    public String createIndex(Map keys, IndexOptions indexOptions) {
        return mongoCollection.createIndex( toBson( keys ), indexOptions );
    }

    private final String PARTIAL_FILTER_EXPRESSION_KEY = "partialFilterExpression";
    private final String NAME_KEY = "name";
    private final String SPARSE_KEY = "sparse";
    private final String UNIQUE_KEY = "unique";
    private final String EXPIRE_AFTER_SECONDS_KEY = "expireAfterSeconds";


    public String createIndex(Map keys, Map options ) {
        IndexOptions indexOptions = new IndexOptions();
        if ( options.containsKey( NAME_KEY)) indexOptions.name( options.get(NAME_KEY).toString() );
        if ( options.containsKey(PARTIAL_FILTER_EXPRESSION_KEY) ) indexOptions.partialFilterExpression( toBson(options.get( "partialFilterExpression")));
        if ( options.containsKey(SPARSE_KEY) && options.get( SPARSE_KEY) instanceof Boolean ) indexOptions.sparse( (Boolean) options.get(SPARSE_KEY));
        if ( options.containsKey(UNIQUE_KEY) && options.get( UNIQUE_KEY) instanceof Boolean ) indexOptions.sparse( (Boolean) options.get(UNIQUE_KEY));
        if ( options.containsKey(EXPIRE_AFTER_SECONDS_KEY) && options.get( EXPIRE_AFTER_SECONDS_KEY) instanceof Number ) indexOptions.expireAfter( ((Number) options.get(EXPIRE_AFTER_SECONDS_KEY)).longValue(), TimeUnit.SECONDS );
        return mongoCollection.createIndex( toBson( keys ), indexOptions );
    }

    public String createIndex(ClientSession clientSession, Bson keys) {
        return null;
    }


    public String createIndex(ClientSession clientSession, Bson keys, IndexOptions indexOptions) {
        return null;
    }


    public List<String> createIndexes(List indexes) {
        return mongoCollection.createIndexes( indexes );
    }


    public List<String> createIndexes(List indexes, CreateIndexOptions createIndexOptions) {
        return null;
    }


    public List<String> createIndexes(ClientSession clientSession, List indexes) {
        return null;
    }


    public List<String> createIndexes(ClientSession clientSession, List indexes, CreateIndexOptions createIndexOptions) {
        return null;
    }


    public ListIndexesIterable<Document> listIndexes() {
        return mongoCollection.listIndexes();
    }


    public ListIndexesIterable listIndexes(Class aClass) {
        return mongoCollection.listIndexes(aClass);
    }


    public ListIndexesIterable<Document> listIndexes(ClientSession clientSession) {
        return mongoCollection.listIndexes( clientSession );
    }


    public ListIndexesIterable listIndexes(ClientSession clientSession, Class aClass) {
        return mongoCollection.listIndexes( clientSession, aClass);
    }


    public void dropIndex(String indexName) {
        mongoCollection.dropIndex( indexName );
    }


    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions) {
        mongoCollection.dropIndex( indexName, dropIndexOptions );
    }


    public void dropIndex(Map keys) {
        mongoCollection.dropIndex( toBson( keys ));
    }


    public void dropIndex(Map keys, DropIndexOptions dropIndexOptions) {
        mongoCollection.dropIndex( toBson(keys), dropIndexOptions );
    }


    public void dropIndex(ClientSession clientSession, String indexName) {

    }


    public void dropIndex(ClientSession clientSession, Bson keys) {

    }


    public void dropIndex(ClientSession clientSession, String indexName, DropIndexOptions dropIndexOptions) {

    }


    public void dropIndex(ClientSession clientSession, Bson keys, DropIndexOptions dropIndexOptions) {

    }


    public void dropIndexes() {
        mongoCollection.dropIndexes();
    }


    public void dropIndexes(ClientSession clientSession) {

    }


    public void dropIndexes(DropIndexOptions dropIndexOptions) {

    }


    public void dropIndexes(ClientSession clientSession, DropIndexOptions dropIndexOptions) {

    }


    public void renameCollection(MongoNamespace newCollectionNamespace) {
        mongoCollection.renameCollection( newCollectionNamespace );
    }


    public void renameCollection(MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {

    }


    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace) {

    }


    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {

    }
}
