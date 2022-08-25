package com.wisecoders.dbschema.mongodb.structure;

import com.google.gson.GsonBuilder;
import com.mongodb.DBRef;
import com.wisecoders.dbschema.mongodb.Util;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Types;
import java.util.*;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with  <a href="https://dbschema.com">DbSchema Database Designer</a>
 * Free to use by everyone, code modifications allowed only to the  <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public repository</a>
 */
public class MetaObject extends MetaField {

    public static final int TYPE_OBJECT = 4999544;
    public static final int TYPE_ARRAY = 4999545;

    public final List<MetaField> fields = new ArrayList<>();

    MetaObject(MetaObject parentObject, String name ){
        super( parentObject, name );
    }

    public MetaField getField( String name ){
        for ( MetaField field : fields){
            if ( field.name.equals( name )) return field;
        }
        return null;
    }

    public MetaField createField(String name){
        final MetaField field = new MetaField( this, name );
        fields.add( field );
        Collections.sort( fields, FIELDS_COMPARATOR );
        return field;
    }

    public MetaField createField(String name, String typeName, int javaType, boolean mandatory ){
        for ( MetaField field : fields){
            if ( field.name.equals( name )) return field;
        }
        final MetaField field = new MetaField( this, name );
        field.setTypeName( typeName );
        field.setJavaType( javaType );
        field.setMandatory( mandatory );
        fields.add( field );
        Collections.sort( fields, FIELDS_COMPARATOR );
        return field;
    }

    public MetaObject createObjectField(String name, boolean mandatory ){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name );
        json.setTypeName("object");
        json.setJavaType( TYPE_OBJECT );
        fields.add( json );
        Collections.sort( fields, FIELDS_COMPARATOR );
        json.setMandatory( mandatory );
        return json;
    }

    static final Comparator<MetaField> FIELDS_COMPARATOR = (o1, o2) -> {
        if (o1.equals(o2)) {
            return 0;
        } else if ("_id".equals(o1.name)) {
            return -1;
        } else if ( "_id".equals(o2.name)) {
            return 1;
        }
        return o1.name.compareTo(o2.name);
    };

    public MetaObject createArrayField(String name, String typeName, boolean mandatoryIfNew){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name );
        json.setTypeName( typeName );
        json.setJavaType( TYPE_ARRAY );
        json.setMandatory( mandatoryIfNew);
        fields.add( json );
        return json;
    }

    @Override
    public void collectFieldsWithObjectId(List<MetaField> unsolvedFields) {
        super.collectFieldsWithObjectId(unsolvedFields);
        for ( MetaField field : fields ){
            field.collectFieldsWithObjectId(unsolvedFields);
        }
    }

    MetaField findField( String name ){
        for ( MetaField other : fields ){
            if ( name.startsWith( other.getNameWithPath())){
                MetaField found = null;
                if ( other instanceof MetaObject){
                    found = ((MetaObject)other).findField(  name );
                }
                return found != null ? found : other;
            }
        }
        return null;
    }


    public void visitValidatorNode(String name, boolean mandatory, Document bsonDefinition ) {
        List<Object> enumValues = null;
        try { enumValues = bsonDefinition.getList("enum", Object.class); } catch ( Throwable ex ){}
        if (enumValues == null) {
            String bsonType = Util.getBsonType( bsonDefinition );
            switch (bsonType) {
                case "object": {
                    final MetaObject intoObject = (name != null) ? createObjectField(name, mandatory) : this;
                    intoObject.visitValidatorFields((Document) bsonDefinition.get("properties"), bsonDefinition.getList("required", String.class));
                    intoObject.setDescription( bsonDefinition.getString("description"));
                }
                break;
                case "array": {
                    final Document itemsDefinition = (Document) bsonDefinition.get("items");
                    if ( itemsDefinition != null ) {
                        String itemType = Util.getBsonType(itemsDefinition);

                        MetaObject intoObject = (name != null) ? createArrayField(name, "array[" + itemType + "]", mandatory) : this;
                        intoObject.setDescription(bsonDefinition.getString("description"));
                        final Document objDefinition = (Document) itemsDefinition.get("properties");
                        if (objDefinition != null) {
                            intoObject.visitValidatorFields(objDefinition, bsonDefinition.getList("required", String.class));
                        }
                    } else if ( bsonDefinition.get("properties") != null ){
                        MetaObject intoObject = (name != null) ? createArrayField(name, "array[object]", mandatory) : this;
                        intoObject.visitValidatorFields((Document) bsonDefinition.get("properties"), bsonDefinition.getList("required", String.class));
                        intoObject.setDescription( bsonDefinition.getString("description"));
                    }
                }
                break;
                default: {
                    final MetaField metaField = createField(name );
                    metaField.setTypeName( bsonType );
                    metaField.setMandatory( mandatory );
                    metaField.setDescription( bsonDefinition.getString("description") );
                    if ( bsonDefinition.containsKey("pattern"))metaField.addOption("pattern:'" + bsonDefinition.get("pattern")  +"'");
                    if ( bsonDefinition.containsKey("minimum"))metaField.addOption("minimum:" + bsonDefinition.get("minimum"));
                    if ( bsonDefinition.containsKey("maximum"))metaField.addOption("maximum:" + bsonDefinition.get("maximum"));
                }
                break;
            }
        } else {
            final MetaField field = createField(name);
            field.setTypeName( "enum");
            field.setJavaType( Types.ARRAY );
            field.setMandatory( mandatory );
            String anEnum = new GsonBuilder().create().toJson(bsonDefinition.getList("enum", Object.class));
            if ( anEnum.startsWith("[") && anEnum.endsWith("]")){
                anEnum = anEnum.substring( 1, anEnum.length()-1);
            }
            field.addOption("enum:" + anEnum);
            field.setDescription( bsonDefinition.getString("description") );
        }
    }


    private void visitValidatorFields(Document document, List<String> requiredFields) {
        if ( document != null ) {
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                if ( entry.getValue() != null ) {
                    boolean mandatory = requiredFields != null && requiredFields.contains(entry.getKey());
                    visitValidatorNode(entry.getKey(), mandatory, (Document) entry.getValue());
                }
            }
        }
    }


    private boolean isFirstDiscover = true;

    protected void scanDocument(Object objDocument){
        if ( objDocument instanceof Map){
            Map document = (Map)objDocument;
            for ( Object key : document.keySet() ){
                final Object value = document.get( key );
                if ( value instanceof Map ) {
                    Map subMap = (Map)value;
                    // "suburbs":[ { name: "Scarsdale" }, { name: "North Hills" } ] WOULD GENERATE SUB-ENTITIES 0,1,2,... FOR EACH LIST ENTRY. SKIP THIS
                    if ( Util.allKeysAreNumbers( subMap )){
                        final MetaObject childrenMap = createArrayField(key.toString(), "array[int]", isFirstDiscover );
                        for ( Object subKey : subMap.keySet() ) {
                            childrenMap.scanDocument(subMap.get( subKey ));
                        }
                    } else {
                        final MetaObject childrenMap = createObjectField(key.toString(), isFirstDiscover );
                        childrenMap.scanDocument( value);
                    }
                } else if ( value instanceof List){
                    if ( (Util.isListOfDocuments(value))  ) {
                        final MetaObject subDocument = createArrayField(key.toString(), "array[object]", isFirstDiscover );
                        for ( Object child : (List)value ){
                            subDocument.scanDocument( child);
                        }
                    } else {
                        createField( (String)key, "array", MetaObject.TYPE_ARRAY, isFirstDiscover  );
                    }
                } else {
                    MetaField field = getField( (String)key );
                    if ( field == null ){
                        field = createField( (String)key );
                        field.setMandatory( isFirstDiscover );
                    }
                    field.setTypeFromValue( value );
                    // VALUES WHICH ARE OBJECTID AND ARE NOT _id IN THE ROOT MAP
                    if ( value instanceof ObjectId && !"_id".equals( field.getNameWithPath() ) ){
                        field.setObjectId((ObjectId) value);
                    }
                    if ( value instanceof DBRef ){
                        DBRef ref = (DBRef)value;
                        MetaCollection targetCollection = getMetaCollection().metaDatabase.getMetaCollection(ref.getCollectionName());
                        if ( targetCollection != null ) {
                            field.createReferenceTo(targetCollection);
                        }
                    }
                }
            }
            for ( MetaField field: fields){
                if ( !document.containsKey( field.name )){
                    field.setMandatory( false );
                }
            }
        }
        isFirstDiscover = false;
    }

    public MetaCollection getMetaCollection(){
        MetaObject _obj = this;
        do {
            if ( _obj instanceof MetaCollection ){
                return (MetaCollection) _obj;
            }
            _obj = _obj.parentObject;
        } while ( _obj != null );
        return null;
    }

    public int getFieldCount(){
        int count = 0;
        for ( MetaField field : fields ){
            count += field.getFieldCount();
        }
        return count;
    }
}
