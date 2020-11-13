package com.dbschema;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class GraalIssue {

    public void find( Object obj){
        System.out.println("here");
    }

    public static void main(String[] args ){
        Context context = Context.newBuilder("js").allowAllAccess(true).build();
        boolean dbIsSet = false;
        Value bindings = context.getBindings("js");
        bindings.putMember("db", new GraalIssue());
        context.eval( "js", "db.find({ $and:[{'name':'Lulu','age':21}, {'name':'Bubu','age':22}]})" );
    }
}
