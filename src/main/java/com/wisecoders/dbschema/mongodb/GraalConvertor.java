package com.wisecoders.dbschema.mongodb;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */

public class GraalConvertor {

    public static Bson toBson(Object obj ){
        if ( obj instanceof Map){
            return new BasicDBObject(convertMap((Map)obj) );
        } else {
            String json = new Gson().toJson(obj);
            return obj != null ? BasicDBObject.parse(json) : null;
        }
    }

    public static List toList(Object source ){
        if ( source instanceof Map && mapIsArray((Map)source)){
            Map sourceMap = (Map)source;
            ArrayList array = new ArrayList();
            for ( int i = 0; i < sourceMap.size(); i++ ){
                array.add( toBson(sourceMap.get("" + i)));
            }
            return array;
        }
        if ( source instanceof List ){
            ArrayList<Object> ret = new ArrayList<>();
            for ( Object obj : (List)source ) {
                ret.add( toBson( obj ));
            }
            return ret;
        }
        return null;
    }


    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");

    public static Map convertMap( Map map ){
        for (Object key : map.keySet()){
            Object value = map.get( key );
            if ( value instanceof Map ) {
                if (mapIsArray((Map) value)) {
                    map.put(key, mapToArray((Map) value));
                } else {
                    convertMap((Map) value);
                }
            }
            if ( "_id".equals(key) && value instanceof String && HEXADECIMAL_PATTERN.matcher((String)value).matches() ){
                map.put( "_id", new ObjectId((String)value));
            }
        }
        return map;
    }

    private static boolean mapIsArray(Map map ) {
        return map.isEmpty() && map.get("0") != null;
    }

    private static List<Object> mapToArray(Map map ) {
        final ArrayList<Object> array = new ArrayList<>();
        int i = 0;
        Object obj;
        while ( ( obj = map.get("" + i) ) != null ){
            if ( obj instanceof Map ){
                convertMap( (Map)obj );
            }
            array.add( obj );
            i++;
        }
        return array;
    }



}
