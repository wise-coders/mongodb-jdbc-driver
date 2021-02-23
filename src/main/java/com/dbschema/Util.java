package com.dbschema;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Util {

    public static Bson toBson(Object obj ){
        if ( obj instanceof Map){
            return new BasicDBObject(convert((Map)obj) );
        } else {
            String json = new Gson().toJson(obj);
            return obj != null ? BasicDBObject.parse(json) : null;
        }
    }

    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");

    public static Map convert(Map map){
        for (Object key : map.keySet()){
            Object value = map.get( key );
            if ( value instanceof Map ) {
                if (mapIsArray((Map) value)) {
                    map.put(key, mapToArray((Map) value));
                } else {
                    convert((Map) value);
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
                convert( (Map)obj );
            }
            array.add( obj );
            i++;
        }
        return array;
    }


    public static List toBsonList(Object source ){
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

}
