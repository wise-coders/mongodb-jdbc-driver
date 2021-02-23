package com.dbschema.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Wise Coders Gmbh. BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaObject extends MetaField {

    static final int TYPE_OBJECT = 4999544;
    static final int TYPE_ARRAY = 4999545;

    public final List<MetaField> fields = new ArrayList<MetaField>();

    MetaObject(MetaObject parentObject, String name, int type ){
        super( parentObject, name, ( type == TYPE_ARRAY ? "array" : "object" ), type );
    }

    MetaField createField(String name, String typeName, int type, boolean mandatoryIfNew ){
        for ( MetaField column : fields){
            if ( column.name.equals( name )) return column;
        }
        final MetaField field = new MetaField( this, name, typeName, type );
        field.setMandatory(mandatoryIfNew);
        fields.add( field );
        return field;
    }

    MetaObject createJsonObjectField(String name, boolean mandatoryIfNew){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name, TYPE_OBJECT);
        fields.add( json );
        json.setMandatory( mandatoryIfNew );
        return json;
    }

    MetaObject createJsonArrayField(String name, boolean mandatoryIfNew){
        for ( MetaField field : fields){
            if ( field instanceof MetaObject && field.name.equals( name )) return (MetaObject)field;
        }
        MetaObject json = new MetaObject( this, name, TYPE_ARRAY);
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

    public void setTypeArray(){

    }

}
