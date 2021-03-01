package com.dbschema.structure;

import com.dbschema.Util;
import com.google.gson.GsonBuilder;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaObject extends MetaField {

    static final int TYPE_OBJECT = 4999544;
    static final int TYPE_ARRAY = 4999545;

    public final List<MetaField> fields = new ArrayList<MetaField>();

    MetaObject(MetaObject parentObject, String name, String typeName, int type ){
        super( parentObject, name, typeName, type );
    }

    public MetaField createField(String name, String typeName, int type, boolean mandatoryIfNew ){
        for ( MetaField column : fields){
            if ( column.name.equals( name )) return column;
        }
        final MetaField field = new MetaField( this, name, typeName, type );
        field.setMandatory(mandatoryIfNew);
        fields.add( field );
        return field;
    }

    public MetaObject createObjectField(String name, boolean mandatoryIfNew){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name, "object",  TYPE_OBJECT);
        fields.add( json );
        json.setMandatory( mandatoryIfNew );
        return json;
    }

    public MetaObject createArrayField(String name, String typeName, boolean mandatoryIfNew){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name, typeName, TYPE_ARRAY);
        json.setMandatory( mandatoryIfNew);
        fields.add( json );
        return json;
    }

    public MetaField getColumn ( String name ){
        for ( MetaField column : fields){
            if ( column.name.equals( name ) ) return column;
        }
        return null;
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
        List<Object> enumValues = bsonDefinition.getList("enum", Object.class);
        if (enumValues == null) {
            String bsonType = Util.getBsonType( bsonDefinition );
            switch (bsonType) {
                case "object": {
                    MetaObject intoObject = (name != null) ? createObjectField(name, mandatory) : this;
                    intoObject.visitValidatorFields((Document) bsonDefinition.get("properties"), bsonDefinition.getList("required", String.class));
                    intoObject.setDescription( bsonDefinition.getString("description"));
                }
                break;
                case "array": {
                    Document itemsDefinition = (Document) bsonDefinition.get("items");
                    String itemType = Util.getBsonType( itemsDefinition );
                    Document objDefinition = (Document) itemsDefinition.get("properties");

                    MetaObject intoObject = (name != null) ? createArrayField(name, "array[" + itemType + "]", mandatory) : this;
                    intoObject.setDescription( bsonDefinition.getString("description"));
                    if ( objDefinition != null ) {
                        intoObject.visitValidatorFields(objDefinition, bsonDefinition.getList("required", String.class));
                    }
                }
                break;
                default: {
                    MetaField metaField = createField(name, bsonType, Util.getJavaType(bsonType), mandatory);
                    metaField.setDescription( bsonDefinition.getString("description") );
                    if ( bsonDefinition.containsKey("pattern"))metaField.setOptions("pattern:'" + bsonDefinition.get("pattern")  +"'");
                    else if ( bsonDefinition.containsKey("minimum"))metaField.setOptions("minimum:" + bsonDefinition.get("minimum"));
                    else if ( bsonDefinition.containsKey("maximum"))metaField.setOptions("maximum:" + bsonDefinition.get("maximum"));
                }
                break;
            }
        } else {
            MetaField field = createField(name, "enum", Types.ARRAY, mandatory);
            String anEnum = new GsonBuilder().create().toJson(bsonDefinition.getList("enum", Object.class));
            if ( anEnum.startsWith("[") && anEnum.endsWith("]")){
                anEnum = anEnum.substring( 1, anEnum.length()-1);
            }
            field.setOptions("enum:" + anEnum);
            field.setDescription( bsonDefinition.getString("description") );
        }
    }


    private void visitValidatorFields(Document document, List<String> requiredFields) {
        if ( document != null ) {
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                String fieldName = entry.getKey();
                Document fieldDefinition = (Document) entry.getValue();
                boolean mandatory = requiredFields != null && requiredFields.contains(fieldName);
                visitValidatorNode(fieldName, mandatory, fieldDefinition);
            }
        }
    }


    private boolean isFirstDiscover = true;

    protected void scanDocument(Object objDocument){
        if ( objDocument instanceof Map){
            Map document = (Map)objDocument;
            for ( Object key : document.keySet() ){
                final Object keyValue = document.get( key );
                String type =( keyValue != null ? keyValue.getClass().getName() : "String" );
                if ( type.lastIndexOf('.') > 0 ) type = type.substring( type.lastIndexOf('.') + 1 );
                if ( keyValue instanceof Map ) {
                    Map subMap = (Map)keyValue;
                    // "suburbs":[ { name: "Scarsdale" }, { name: "North Hills" } ] WOULD GENERATE SUB-ENTITIES 0,1,2,... FOR EACH LIST ENTRY. SKIP THIS
                    if ( Util.allKeysAreNumbers( subMap )){
                        final MetaObject childrenMap = createArrayField(key.toString(), "array[integer]", isFirstDiscover );
                        for ( Object subKey : subMap.keySet() ) {
                            childrenMap.scanDocument(subMap.get( subKey ));
                        }
                    } else {
                        final MetaObject childrenMap = createObjectField(key.toString(), isFirstDiscover );
                        childrenMap.scanDocument( keyValue);
                    }
                } else if ( keyValue instanceof List){
                    final List list = (List)keyValue;
                    if ( (list.isEmpty() || Util.isListOfDocuments(keyValue))  ) {
                        final MetaObject subDocument = createArrayField(key.toString(), "array[object]", isFirstDiscover );
                        for ( Object child : (List)keyValue ){
                            subDocument.scanDocument( child);
                        }
                    } else {
                        createField((String) key, "array", MetaObject.TYPE_ARRAY, isFirstDiscover );
                    }
                } else {
                    MetaField field = createField((String) key, type, Util.getJavaType( keyValue ), isFirstDiscover );
                    // VALUES WHICH ARE OBJECTID AND ARE NOT _id IN THE ROOT MAP
                    if ( keyValue instanceof ObjectId && !"_id".equals( field.getNameWithPath() ) ){
                        field.addObjectId((ObjectId) keyValue);
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
}
