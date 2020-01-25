package com.dbschema.wrappers;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class WrappedFindIterable<TResult> implements com.mongodb.client.MongoIterable<TResult> {

    private final FindIterable<TResult> findIterable;

    private TResult toDocument( Map map ){
        return (TResult)( new Document( map ));
    }

    public WrappedFindIterable(FindIterable<TResult> findIterable ){
        this.findIterable = findIterable;
    }

    public WrappedFindIterable filter(String str) {
        findIterable.filter( BasicDBObject.parse(str) );
        return this;
    }

    public WrappedFindIterable filter(Map map) {
        findIterable.filter( WrappedMongoCollection.toBson( map ) );
        return this;
    }

    public WrappedFindIterable projection(String str) {
        findIterable.projection(BasicDBObject.parse(str));
        return this;
    }

    public WrappedFindIterable projection(Map map) {
        findIterable.projection( WrappedMongoCollection.toBson( map ) );
        return this;
    }

    public WrappedFindIterable sort(String str) {
        findIterable.sort(BasicDBObject.parse(str));
        return this;
    }

    public WrappedFindIterable sort(Map map) {
        findIterable.sort( WrappedMongoCollection.toBson( map ) );
        return this;
    }

    public long count(){
        long cnt=0;
        for( TResult res : findIterable ){
            cnt++;
        }
        return cnt;
    }

    //---------------------------------------------------------------

    public WrappedFindIterable filter(Bson bson) {
        findIterable.filter( bson );
        return this;
    }

    public WrappedFindIterable limit(int i) {
        findIterable.limit( i );
        return this;
    }

    public WrappedFindIterable skip(int i) {
        findIterable.skip( i );
        return this;
    }

    public WrappedFindIterable maxTime(long l, TimeUnit timeUnit) {
        findIterable.maxTime( l, timeUnit);
        return this;
    }

    public WrappedFindIterable projection(Bson bson) {
        findIterable.projection( bson );
        return this;
    }

    public WrappedFindIterable sort(Bson bson) {
        findIterable.sort( bson );
        return this;
    }

    public WrappedFindIterable noCursorTimeout(boolean b) {
        findIterable.noCursorTimeout( b );
        return this;
    }

    public WrappedFindIterable oplogReplay(boolean b) {
        findIterable.oplogReplay( b );
        return this;
    }

    public WrappedFindIterable partial(boolean b) {
        findIterable.partial( b );
        return this;
    }

    public WrappedFindIterable cursorType(CursorType cursorType) {
        findIterable.cursorType( cursorType );
        return this;
    }

    public WrappedFindIterable batchSize(int i) {
        findIterable.batchSize( i );
        return this;
    }

    public MongoCursor iterator() {
        return findIterable.iterator();
    }

    public TResult first() {
        return findIterable.first();
    }

    public <U> MongoIterable<U> map(Function<TResult, U> tResultUFunction) {
        return findIterable.map( tResultUFunction);
    }

    public <A extends Collection<? super TResult>> A into(A a) {
        return findIterable.into( a );
    }

    public void forEach(Block block) {
        findIterable.forEach( block );
    }

    @Override
    public MongoCursor<TResult> cursor() {
        return findIterable.cursor();
    }

    public TResult explain(){
        return findIterable.modifiers(new Document("$explain", true)).first();
    }
}
