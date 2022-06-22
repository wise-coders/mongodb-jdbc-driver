package com.wisecoders.dbschema.mongodb.wrappers;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.wisecoders.dbschema.mongodb.GraalConvertor;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with DbSchema Database Designer https://dbschema.com
 * Free to use by everyone, code modifications allowed only to
 * the public repository https://github.com/wise-coders/mongodb-jdbc-driver
 */
public class WrappedMongoCollection<TDocument> {

    public final WrappedMongoDatabase wrappedMongoDatabase;
    private final MongoCollection<TDocument> mongoCollection;


    WrappedMongoCollection(WrappedMongoDatabase wrappedMongoDatabase, MongoCollection<TDocument> mongoCollection ){
        this.wrappedMongoDatabase = wrappedMongoDatabase;
        this.mongoCollection = mongoCollection;
    }

    private static final long SCAN_FIRST_LAST = 100;




    private TDocument toDocument( Map map ){
        return (TDocument)( new Document( GraalConvertor.convertMap(map) ));
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
        return mongoCollection.countDocuments( GraalConvertor.toBson(filter));
    }

    public long count(Map filter, CountOptions options) {
        return mongoCollection.countDocuments( GraalConvertor.toBson( filter), options );
    }


    public long count(ClientSession clientSession) {
        return mongoCollection.countDocuments( clientSession );
    }


    public long count(ClientSession clientSession, Map filter) {
        return mongoCollection.countDocuments(clientSession, GraalConvertor.toBson( filter ));
    }


    public long count(ClientSession clientSession, Map filter, CountOptions options) {
        return mongoCollection.countDocuments( clientSession, GraalConvertor.toBson(filter), options );
    }


    public long countDocuments() {
        return mongoCollection.countDocuments();
    }


    public long countDocuments(Map filter) {
        return mongoCollection.countDocuments( GraalConvertor.toBson( filter ));
    }


    public long countDocuments(Map filter, CountOptions options) {
        return mongoCollection.countDocuments( GraalConvertor.toBson( filter ), options);
    }


    public long countDocuments(ClientSession clientSession) {
        return mongoCollection.countDocuments( clientSession );
    }


    public long countDocuments(ClientSession clientSession, Map filter) {
        return mongoCollection.countDocuments( clientSession, GraalConvertor.toBson(filter) );
    }


    public long countDocuments(ClientSession clientSession, Map filter, CountOptions options) {
        return mongoCollection.countDocuments( clientSession, GraalConvertor.toBson(filter), options);
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
        return mongoCollection.distinct( fieldName, GraalConvertor.toBson( filter ), aClass);
    }


    public DistinctIterable distinct(ClientSession clientSession, String fieldName, Class aClass) {
        return mongoCollection.distinct( clientSession, fieldName, aClass );
    }


    public DistinctIterable distinct(ClientSession clientSession, String fieldName, Map filter, Class aClass) {
        return mongoCollection.distinct( clientSession, fieldName, GraalConvertor.toBson(filter), aClass);
    }


    public WrappedFindIterable find() {
        return new WrappedFindIterable(mongoCollection.find());
    }


    public WrappedFindIterable find(Class aClass) {
        return new WrappedFindIterable(mongoCollection.find( aClass ));
    }


    public WrappedFindIterable find(Map filter) {
        return new WrappedFindIterable( mongoCollection.find(GraalConvertor.toBson(filter)));
    }

    public WrappedFindIterable find(Map filter, Map projection) {
        return new WrappedFindIterable( mongoCollection.find(GraalConvertor.toBson(filter)).projection( GraalConvertor.toBson(projection) ));
    }


    public WrappedFindIterable find(Map filter, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( GraalConvertor.toBson(filter), aClass));
    }


    public WrappedFindIterable find(ClientSession clientSession) {
        return new WrappedFindIterable( mongoCollection.find(clientSession));
    }


    public WrappedFindIterable find(ClientSession clientSession, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, aClass ));
    }


    public WrappedFindIterable find(ClientSession clientSession, Map filter) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, GraalConvertor.toBson(filter) ));
    }


    public WrappedFindIterable find(ClientSession clientSession, Map filter, Class aClass) {
        return new WrappedFindIterable( mongoCollection.find( clientSession, GraalConvertor.toBson(filter), aClass ));
    }

    //

    public TDocument findOne() {
        return new WrappedFindIterable<>(mongoCollection.find()).first();
    }


    public TDocument findOne(Class aClass) {
        return new WrappedFindIterable<TDocument>(mongoCollection.find( aClass )).first();
    }


    public TDocument findOne(Map filter) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find(GraalConvertor.toBson(filter))).first();
    }

    public TDocument findOne(Map filter, Map projection) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find(GraalConvertor.toBson(filter)).projection( GraalConvertor.toBson(projection) )).first();
    }


    public TDocument findOne(Map filter, Class aClass) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find( GraalConvertor.toBson(filter), aClass)).first();
    }


    public TDocument findOne(ClientSession clientSession) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find(clientSession)).first();
    }


    public TDocument findOne(ClientSession clientSession, Class aClass) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find( clientSession, aClass )).first();
    }


    public TDocument findOne(ClientSession clientSession, Map filter) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find( clientSession, GraalConvertor.toBson(filter) )).first();
    }


    public TDocument findOne(ClientSession clientSession, Map filter, Class aClass) {
        return new WrappedFindIterable<TDocument>( mongoCollection.find( clientSession, GraalConvertor.toBson(filter), aClass )).first();
    }

    public AggregateIterable aggregate(List pipeline) {
        return mongoCollection.aggregate(GraalConvertor.toList(pipeline));
    }

    public AggregateIterable aggregate(Object object) {
        List list = new ArrayList();
        list.add( GraalConvertor.toBson(object) );
        return mongoCollection.aggregate(list);
    }

    public AggregateIterable aggregate(Object obj1, Object obj2) {
        List list = new ArrayList();
        list.add( GraalConvertor.toBson(obj1) );
        list.add( GraalConvertor.toBson(obj2));
        return mongoCollection.aggregate(list);
    }

    public AggregateIterable aggregate(Object obj1, Object obj2, Object obj3) {
        List list = new ArrayList();
        list.add( GraalConvertor.toBson(obj1) );
        list.add( GraalConvertor.toBson(obj2));
        list.add( GraalConvertor.toBson(obj3));
        return mongoCollection.aggregate(list);
    }

    public AggregateIterable aggregate(Object obj1, Object obj2, Object obj3, Object obj4) {
        List list = new ArrayList();
        list.add( GraalConvertor.toBson(obj1) );
        list.add( GraalConvertor.toBson(obj2));
        list.add( GraalConvertor.toBson(obj3));
        list.add( GraalConvertor.toBson(obj4));
        return mongoCollection.aggregate(list);
    }



    public AggregateIterable aggregate(List pipeline, Class aClass) {
        return mongoCollection.aggregate( GraalConvertor.toList(pipeline), aClass );
    }


    public AggregateIterable aggregate(ClientSession clientSession, List pipeline) {
        return mongoCollection.aggregate( clientSession, GraalConvertor.toList(pipeline) );
    }


    public AggregateIterable aggregate(ClientSession clientSession, List pipeline, Class aClass) {
        return mongoCollection.aggregate( clientSession, GraalConvertor.toList(pipeline), aClass );
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

    public MapReduceIterable mapReduce(Object mapFunction, Object reduceFunction) {
        return mongoCollection.mapReduce( String.valueOf(mapFunction), String.valueOf(reduceFunction) );
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


    public void insertOne(Map input) {
        mongoCollection.insertOne( toDocument(input ) );
    }

    public void insertOne(Map input, InsertOneOptions options) {
        mongoCollection.insertOne( toDocument(input), options );
    }


    public void insertOne(ClientSession clientSession, Map input) {
        mongoCollection.insertOne( clientSession, toDocument( input ));
    }


    public void insertOne(ClientSession clientSession, Map input, InsertOneOptions options) {
        mongoCollection.insertOne( clientSession, toDocument(input), options );
    }


    public void insertMany(Object[] arr) {
        for ( Object obj: arr ){
            insertOne( (Map)obj );
        }
    }

    public void insertMany(List list) {
        for ( Object obj: list ){
            insertOne( (Map)obj );
        }
    }

    public void insertMany(Object obj) {
        List list = GraalConvertor.toList( obj );
        if ( list != null ) {
            for (Map map1 : (List<Map>) list) {
                insertOne(map1);
            }
        } else if (obj instanceof Map) {
            insertOne((Map) obj);
        }
    }

    public void insert(Map input) {
        mongoCollection.insertOne( toDocument(input));
    }

    /*
    public void insertMany(List<Map> list) {
        for ( Map map: list ){
            insertOne( map );
        }
    }
    public void insert(List<Map> list) {
        for ( Map map: list ){
            insertOne( map );
        }
    }*/


    public void insertMany(List<Map> list, InsertManyOptions options) {
    }


    public void insertMany(ClientSession clientSession, List list) {
    }


    public void insertMany(ClientSession clientSession, List list, InsertManyOptions options) {
    }


    public DeleteResult deleteOne(Map filter) {
        return mongoCollection.deleteOne( GraalConvertor.toBson( filter ));
    }


    public DeleteResult deleteOne(Map filter, DeleteOptions options) {
        return mongoCollection.deleteOne( GraalConvertor.toBson(filter), options);
    }


    public DeleteResult deleteOne(ClientSession clientSession, Map filter) {
        return mongoCollection.deleteOne( clientSession, GraalConvertor.toBson(filter));
    }


    public DeleteResult deleteOne(ClientSession clientSession, Map filter, DeleteOptions options) {
        return mongoCollection.deleteOne( clientSession, GraalConvertor.toBson(filter), options );
    }

    public DeleteResult remove(Map filter){
        return mongoCollection.deleteMany( GraalConvertor.toBson( filter));
    }

    public DeleteResult deleteMany(Map filter) {
        return mongoCollection.deleteMany( GraalConvertor.toBson(filter));
    }


    public DeleteResult deleteMany(Map filter, DeleteOptions options) {
        return mongoCollection.deleteMany( GraalConvertor.toBson(filter), options);
    }


    public DeleteResult deleteMany(ClientSession clientSession, Map filter) {
        return mongoCollection.deleteMany( clientSession, GraalConvertor.toBson( filter ));
    }


    public DeleteResult deleteMany(ClientSession clientSession, Map filter, DeleteOptions options) {
        return mongoCollection.deleteMany( clientSession, GraalConvertor.toBson(filter), options );
    }


    public UpdateResult replaceOne(Map filter, Map replacement) {
        return mongoCollection.replaceOne( GraalConvertor.toBson(filter), toDocument(replacement) );
    }


    public UpdateResult replaceOne(Bson filter, Map replacement, ReplaceOptions updateOptions) {
        return mongoCollection.replaceOne( GraalConvertor.toBson(filter), toDocument(replacement), updateOptions );
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
        return mongoCollection.updateOne( GraalConvertor.toBson( filter), GraalConvertor.toBson( update ));
    }


    public UpdateResult updateOne(Map filter, Map update, UpdateOptions updateOptions) {
        return mongoCollection.updateOne( GraalConvertor.toBson(filter), GraalConvertor.toBson(update), updateOptions);
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
        return mongoCollection.updateMany( GraalConvertor.toBson(filter), GraalConvertor.toBson(update));
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
        return mongoCollection.updateMany( GraalConvertor.toBson(filter), update);
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
        return mongoCollection.findOneAndDelete( GraalConvertor.toBson( filter ));
    }


    public Object findOneAndDelete(Map filter, FindOneAndDeleteOptions options) {
        return mongoCollection.findOneAndDelete( GraalConvertor.toBson(filter), options );
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


    public Object update(Map filter, Map update) {
        return updateMany( filter, update);
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


    public String ensureIndex(Map keys) {
        return createIndex( keys);
    }

    public String createIndex(Map keys) {
        return mongoCollection.createIndex( GraalConvertor.toBson( keys ));
    }


    public String createIndex(Map keys, IndexOptions indexOptions) {
        return mongoCollection.createIndex( GraalConvertor.toBson( keys ), indexOptions );
    }

    private static final String PARTIAL_FILTER_EXPRESSION_KEY = "partialFilterExpression";
    private static final String NAME_KEY = "name";
    private static final String SPARSE_KEY = "sparse";
    private static final String UNIQUE_KEY = "unique";
    private static final String EXPIRE_AFTER_SECONDS_KEY = "expireAfterSeconds";


    public String createIndex(Map keys, Map options ) {
        IndexOptions indexOptions = new IndexOptions();
        if ( options.containsKey( NAME_KEY)) indexOptions.name( options.get(NAME_KEY).toString() );
        if ( options.containsKey(PARTIAL_FILTER_EXPRESSION_KEY) ) indexOptions.partialFilterExpression( GraalConvertor.toBson(options.get( "partialFilterExpression")));
        if ( options.containsKey(SPARSE_KEY) && options.get( SPARSE_KEY) instanceof Boolean ) indexOptions.sparse( (Boolean) options.get(SPARSE_KEY));
        if ( options.containsKey(UNIQUE_KEY) && options.get( UNIQUE_KEY) instanceof Boolean ) indexOptions.sparse( (Boolean) options.get(UNIQUE_KEY));
        if ( options.containsKey(EXPIRE_AFTER_SECONDS_KEY) && options.get( EXPIRE_AFTER_SECONDS_KEY) instanceof Number ) indexOptions.expireAfter( ((Number) options.get(EXPIRE_AFTER_SECONDS_KEY)).longValue(), TimeUnit.SECONDS );
        return mongoCollection.createIndex( GraalConvertor.toBson( keys ), indexOptions );
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

    public ListIndexesIterable<Document> getIndexes(){
        return mongoCollection.listIndexes();
    }

    public Document getIndexSpecs(){
        return mongoCollection.listIndexes().first();
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
        mongoCollection.dropIndex( GraalConvertor.toBson( keys ));
    }


    public void dropIndex(Map keys, DropIndexOptions dropIndexOptions) {
        mongoCollection.dropIndex( GraalConvertor.toBson(keys), dropIndexOptions );
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

    public void renameCollection(String newName) {
        mongoCollection.renameCollection( new MongoNamespace( getNamespace().getDatabaseName(), newName));
    }


    public void renameCollection(MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {

    }


    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace) {

    }


    public void renameCollection(ClientSession clientSession, MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {

    }


}
