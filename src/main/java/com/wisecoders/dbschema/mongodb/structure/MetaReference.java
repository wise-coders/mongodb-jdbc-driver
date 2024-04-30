package com.wisecoders.dbschema.mongodb.structure;


/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/deed.en">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/mongodb-jdbc-driver">public GIT repository</a>.
 */
public class MetaReference {

    public final MetaField fromField;
    public final MetaCollection pkCollection;

    public MetaReference( MetaField fromField, MetaCollection pkCollection ){
        this.fromField = fromField;
        this.pkCollection = pkCollection;
    }

}
