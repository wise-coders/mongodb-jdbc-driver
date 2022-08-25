package com.wisecoders.dbschema.mongodb.structure;


import com.wisecoders.dbschema.mongodb.Util;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Wise Coders GmbH. The MongoDB JDBC driver is build to be used with  <a href="https://dbschema.com">DbSchema Database Designer</a>
 * Free to use by everyone, code modifications allowed only to the  <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public repository</a>
 */
public class MetaField {

    public final MetaObject parentObject;
    public final String name;
    private Class<?> typeClass;
    private String typeName;
    private int javaType = Integer.MIN_VALUE;
    public ObjectId objectId;
    public final List<MetaReference> references = new ArrayList<>();
    private boolean mandatory = true;
    public String options;
    private String description;


    MetaField(final MetaObject parentObject, final String name ){
        this.parentObject = parentObject;
        this.name = name;
    }

    void setObjectId(ObjectId objectId){
        if ( objectId != null ){
            this.objectId = objectId;
        }
    }

    public ObjectId getObjectId(){
        return objectId;
    }

    public String getNameWithPath(){
        return ( parentObject != null && !(parentObject instanceof MetaCollection ) ? parentObject.getNameWithPath() + "." + name : name );
    }

    public String getPkColumnName(){
        String pkColumnName = name;
        final int idx = pkColumnName.lastIndexOf('.');
        if ( idx > 0 ){
            pkColumnName = pkColumnName.substring( idx + 1 );
        }
        return pkColumnName;
    }

    public MetaReference createReferenceTo(MetaCollection pkCollection){
        MetaReference ifk = new MetaReference( this, pkCollection );
        references.add( ifk );
        return ifk;
    }

    public MetaCollection getMetaCollection(){
        MetaField field = this;
        while ( field != null && !( field instanceof MetaCollection )){
            field = field.parentObject;
        }
        return (MetaCollection)field;
    }

    public void collectFieldsWithObjectId(List<MetaField> unsolvedFields) {
        if ( objectId != null ){
            unsolvedFields.add(this);
        }
    }

    @Override
    public String toString() {
        return getNameWithPath();
    }
    void setMandatory( boolean mandatory ){
        this.mandatory = mandatory;
    }

    public boolean isMandatory(){
        return mandatory;
    }

    public void addOption(String options){
        if ( this.options == null ) {
            this.options = options;
        } else {
            this.options += ", " + options;
        }
    }

    public String getOptions(){
        return options;
    }

    public void setDescription( String description ){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public int getFieldCount(){
        return 1;
    }

    public void setTypeFromValue( Object value ){
        if ( value != null ) {
            Class<?> valueCls = value.getClass();
            if ( typeClass == null ) {
                typeClass = valueCls;
            } else {
                if ( typeClass != valueCls ) {
                    // valueCls is superclass or typeClass
                    if ( valueCls.isAssignableFrom( typeClass )) typeClass = valueCls;
                    else if ( !typeClass.isAssignableFrom( valueCls )) typeClass = Object.class;
                }
            }
        }
    }

    public String getTypeName(){
        if ( typeName != null ) {
            return typeName;
        }
        if ( typeClass != null ) {
            String type = typeClass.getName();
            if (type.lastIndexOf('.') > 0) type = type.substring(type.lastIndexOf('.') + 1);
            return type;
        }
        return "string";
    }

    public void setTypeName( String typeName ){
        this.typeName = typeName;
    }

    public void setTypeClass( Class<?> typeClass ){
        this.typeClass = typeClass;
    }

    public void setJavaType( int javaType ){
        this.javaType = javaType;
    }

    public int getJavaType(){
        if ( javaType != Integer.MIN_VALUE ){
            return javaType;
        }
        return Util.getJavaType( getTypeName() );
    }
}
