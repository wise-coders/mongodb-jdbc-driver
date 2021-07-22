package com.dbschema.structure;


import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Wise Coders GmbH. Free to use. Changes allowed only as push requests into https://bitbucket.org/dbschema/mongodb-jdbc-driver.
 */
public class MetaField {

    public final MetaObject parentObject;
    public final String name, typeName;
    public final List<ObjectId> objectIds = new ArrayList<ObjectId>();
    public final int type;
    public final List<MetaReference> references = new ArrayList<MetaReference>();
    private boolean mandatory = true;
    public String options;
    private String description;


    MetaField(final MetaObject parentObject, final String name, final String typeName, int type ){
        this.parentObject = parentObject;
        this.name = name;
        this.typeName = typeName;
        this.type = type;
        if ( "bike_colour".equals(name))
            System.out.println("Here");
    }

    void addObjectId(ObjectId objectId){
        if ( objectIds.size() < 4 ){
            objectIds.add( objectId );
        }
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
        if ( !objectIds.isEmpty() ){
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
}
