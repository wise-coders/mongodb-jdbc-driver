package com.dbschema;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {

    public static Bson toBson(Object obj ){
        if ( obj instanceof Map){
            return new BasicDBObject(doMapConversions((Map)obj) );
        } else {
            String json = new Gson().toJson(obj);
            return obj != null ? BasicDBObject.parse(json) : null;
        }
    }

    private static Map doMapConversions(Map map){
        for (Object key : map.keySet()){
            Object value = map.get( key );
            if ( value instanceof Map ){
                doMapConversions((Map) value);
            }
            if ( value instanceof Map && canConvertMapToArray( (Map)value )){
                map.put( key, convertMapToArray((Map) value));
            }
        }
        return map;
    }


    private static boolean canConvertMapToArray( Map map ) {
        boolean isArray = true;
        for (int i = 0; i < map.size(); i++) {
            if (!map.containsKey("" + i)) isArray = false;
        }
        return isArray;
    }

    private static List convertMapToArray(Map map ) {
        ArrayList array = new ArrayList();
        for ( int i = 0; i < map.size(); i++ ){
            array.add(map.get("" + i));
        }
        return array;
    }


    public static List toBsonList(List list ){
        if ( list != null ){
            ArrayList<Object> ret = new ArrayList<>();
            for ( Object obj : list ) {
                ret.add( toBson( obj ));
            }
            return ret;
        }
        return list;
    }

}
