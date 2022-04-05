var ObjectId = function( oid ) {
  return new org.bson.types.ObjectId( oid );
}

var DBRef = function( colName, oid ) {
  return new com.mongodb.DBRef( colName, oid );
}

var ISODate = function( str ) {
    var formats = [
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm.ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd' 'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd" ];

    for (i = 0; i < formats.length; i++)  {
        try {
            return new java.text.SimpleDateFormat( formats[i] ).parse(str);
        } catch (error) { }
    }
    throw new java.text.ParseException("Un-parsable ISO date: " + str + " Configured formats: " + formats, 0);
    return null;
};

var Date = function( str ) {
    var formats = [
    "yyyy-MM-dd",
    "dd-MM-yyyy",
    "dd.MM.yyyy",
    "d.MM.yyyy",
    "dd/MM/yyyy",
    "yyyy.MM.dd",
    "M/d/yyyy" ];

    for (i = 0; i < formats.length; i++)  {
        try {
            return new java.text.SimpleDateFormat( formats[i] ).parse(str);
        } catch (error) { }
    }
    throw new java.text.ParseException("Un-parsable date: " + str + " Configured formats: " + formats, 0);
    return null;
}

var use = function(str){
    globalThis.db = client.getDatabase( String(str) );
}
